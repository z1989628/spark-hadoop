package randomForest.df;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import randomForest.df.data.Data;
import randomForest.df.data.DataUtils;
import randomForest.df.data.Dataset;
import randomForest.df.data.Instance;
import randomForest.df.node.Node;

// Represents a forest of decision trees
public class DecisionForest implements Writable {

	private final Map<Integer, Node> trees;
	
	private double[][][] results;
	
	private Map<Integer, Double> weights;
	private boolean hasWeights = false;
	
	private DecisionForest() {
		trees = Maps.newHashMap();
		weights = Maps.newHashMap();
	}
	
	public DecisionForest(List<Node> trees) {
		Preconditions.checkArgument(trees != null && !trees.isEmpty(), "trees arguments must not be null or empty");
		
		this.trees = Maps.newHashMap();
		int treeId = 0;
		for (Node tree : trees) {
			this.trees.put(treeId++, tree);
		}
	}
	
	public void setWeights(HashMap<Integer, Double> weights) {
		this.weights = weights;
		this.hasWeights = true;
	}
	
	public boolean getHasWeights() {
		return hasWeights;
	}
	
	public String getWeights() {
		StringBuilder weightsString = new StringBuilder();
		for (Map.Entry<Integer, Double> entry : weights.entrySet()) {
			weightsString.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		weightsString.deleteCharAt(weightsString.length()-1);
		weightsString.deleteCharAt(weightsString.length()-1);
		return weightsString.toString();
	}
	
	List<Node> getTrees() {
		return Lists.newArrayList(trees.values());
	}
	
	public double[][][] getResults(Data data, Dataset dataset) {
		if (data.isEmpty()) {
			return null;
		}
		results = new double[trees.size()][data.size()][2];
		for (Map.Entry<Integer, Node> entry : trees.entrySet()) {
			for (int index = 0; index < data.size(); index ++) {
				results[entry.getKey()][index][0] = data.get(index).get(dataset.getLabelId());
				results[entry.getKey()][index][1] = entry.getValue().classify(data.get(index));
			}
		}
		
		return results;
	}
	
	// Classifies the data and calls callback for each classification
	public void classify(Data data, double[][] predictions) {
		Preconditions.checkArgument(data.size() == predictions.length, "predictions.length must be equal to data.size()");
		
		if (data.isEmpty()) {
			return; // nothing to classify
		}
		
		int treeId = 0;
		for (Node tree : Lists.newArrayList(trees.values())) {
			for (int index = 0; index < data.size(); index++) {
				if (predictions[index] == null) {
					predictions[index] = new double[trees.size()];
				}
				predictions[index][treeId] = tree.classify(data.get(index));
			}
			treeId ++;
		}
	}
	
	// predicts the label for the instance
	public double classify(Dataset dataset, Random rng, Instance instance) {
		if (dataset.isNumerical(dataset.getLabelId())) {
			double sum = 0;
			int cnt = 0;
			for (Node tree : Lists.newArrayList(trees.values())) {
				double prediction = tree.classify(instance);
				if (!Double.isNaN(prediction)) {
					sum += prediction;
					cnt ++;
				}
			}
			
			if (cnt > 0) {
				return sum / cnt;
			} else {
				return Double.NaN;
			}
		} else {
			double[] predictions = new double[dataset.nblabels()];
			for (Map.Entry<Integer, Node> entry : trees.entrySet()) {
				double prediction = entry.getValue().classify(instance);
				if (!Double.isNaN(prediction)) {
					if (!hasWeights) {
						predictions[(int) prediction] ++;
					} else {				// Improved Decision Forest
						predictions[(int) prediction] += weights.get(entry.getKey()) * 10;
					}
				}
			}
			
			if (DataUtils.sum(predictions) == 0) {
				return Double.NaN; //  no prediction available
			}
			
			return DataUtils.maxindex(rng, predictions);
		}
	}
	
	// Mean number of nodes per tree
	public long meanNbNodes() {
		long sum = 0;
		
		for (Node node : Lists.newArrayList(trees.values())) {
			sum += node.nbNodes();
		}
		
		return sum / trees.size();
	}
	
	// Total number of nodes in all the trees
	public long nbNodes() {
		long sum = 0;
		
		for (Node tree : Lists.newArrayList(trees.values())) {
			sum += tree.nbNodes();
		}
		
		return sum;
	}
	
	// Mean maximum depth per tree
	public long meanMaxDepth() {
		long sum = 0;
		
		for (Node tree : Lists.newArrayList(trees.values())) {
			sum += tree.maxDepth();
		}
		
		return sum / trees.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DecisionForest)) {
			return false;
		}
		
		DecisionForest rf = (DecisionForest) obj;
		
		return trees.size() == rf.getTrees().size() && Lists.newArrayList(trees.values()).containsAll(rf.getTrees());
	}
	
	@Override
	public int hashCode() {
		return trees.hashCode();
	}
	
	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(trees.size());
		for (Map.Entry<Integer, Node> entry : trees.entrySet()) {
			dataOutput.writeInt(entry.getKey());
			entry.getValue().write(dataOutput);
		}
		dataOutput.writeBoolean(hasWeights);
		if (hasWeights) {
			for (Map.Entry<Integer, Double> entry : weights.entrySet()) {
				dataOutput.writeInt(entry.getKey());
				dataOutput.writeDouble(entry.getValue());
			}
		}
	}
	
	// Reads the trees from the input and adds them to the existing trees
	@Override
	public void readFields(DataInput dataInput) throws IOException {
		int size = dataInput.readInt();
		for (int i = 0; i < size; i++) {
			trees.put(dataInput.readInt(), Node.read(dataInput));
		}
		hasWeights = dataInput.readBoolean();
		int treeID = 0;
		double weight = 0.0;
		if (hasWeights) {
			for (int i = 0; i < size; i++) {
//				weights.put(dataInput.readInt(), dataInput.readDouble());
				treeID = dataInput.readInt();
				weight = dataInput.readDouble();
				weights.put(treeID, weight);
			}
		}
	}
	
	// Read the forest from inputStream
	public static DecisionForest read(DataInput dataInput) throws IOException {
		DecisionForest forest = new DecisionForest();
		forest.readFields(dataInput);
		return forest;
	}
	
	// Load the forest from a single file or a directory of files
	@SuppressWarnings("deprecation")
	public static DecisionForest load(Configuration conf, Path forestPath) throws IOException {
		FileSystem fs = forestPath.getFileSystem(conf);
		Path[] files;
		if (fs.getFileStatus(forestPath).isDir()) {
			files = DFUtils.listOutputFiles(fs, forestPath);
		} else {
			files = new Path[] { forestPath };
		}
		
		DecisionForest forest = null;
		for (Path path : files) {
			FSDataInputStream dataInput = new FSDataInputStream(fs.open(path));
			try {
				if (forest == null) {
					forest = read(dataInput);
				} else {
					forest.readFields(dataInput);
				}
			} finally {
				Closeables.close(dataInput, true);
			}
		}
		
		return forest;
	}
}
