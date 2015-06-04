package randomForest.df;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import randomForest.df.data.Data;
import randomForest.df.data.DataUtils;
import randomForest.df.data.Dataset;
import randomForest.df.data.Instance;
import randomForest.df.node.Node;

// Represents a forest of decision trees
public class CopyOfDecisionForest implements Writable {

	private final List<Node> trees;
	
	private CopyOfDecisionForest() {
		trees = Lists.newArrayList();
	}
	
	public CopyOfDecisionForest(List<Node> trees) {
		Preconditions.checkArgument(trees != null && !trees.isEmpty(), "trees arguments must not be null or empty");
		
		this.trees = trees;
	}
	
	List<Node> getTrees() {
		return trees;
	}
	
	// Classifies the data and calls callback for each classification
	public void classify(Data data, double[][] predictions) {
		Preconditions.checkArgument(data.size() == predictions.length, "predictions.length must be equal to data.size()");
		
		if (data.isEmpty()) {
			return; // nothing to classify
		}
		
		int treeId = 0;
		for (Node tree : trees) {
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
			for (Node tree : trees) {
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
			int[] predictions = new int[dataset.nblabels()];
			for (Node tree : trees) {
				double prediction = tree.classify(instance);
				if (!Double.isNaN(prediction)) {
					predictions[(int) prediction] ++;
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
		
		for (Node node : trees) {
			sum += node.nbNodes();
		}
		
		return sum / trees.size();
	}
	
	// Total number of nodes in all the trees
	public long nbNodes() {
		long sum = 0;
		
		for (Node tree : trees) {
			sum += tree.nbNodes();
		}
		
		return sum;
	}
	
	// Mean maximum depth per tree
	public long meanMaxDepth() {
		long sum = 0;
		
		for (Node tree : trees) {
			sum += tree.maxDepth();
		}
		
		return sum / trees.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CopyOfDecisionForest)) {
			return false;
		}
		
		CopyOfDecisionForest rf = (CopyOfDecisionForest) obj;
		
		return trees.size() == rf.getTrees().size() && trees.containsAll(rf.getTrees());
	}
	
	@Override
	public int hashCode() {
		return trees.hashCode();
	}
	
	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(trees.size());
		for (Node tree : trees) {
			tree.write(dataOutput);
		}
	}
	
	// Reads the trees from the input and adds them to the existing trees
	@Override
	public void readFields(DataInput dataInput) throws IOException {
		int size = dataInput.readInt();
		for (int i = 0; i < size; i++) {
			trees.add(Node.read(dataInput));
		}
	}
	
	// Read the forest from inputStream
	public static CopyOfDecisionForest read(DataInput dataInput) throws IOException {
		CopyOfDecisionForest forest = new CopyOfDecisionForest();
		forest.readFields(dataInput);
		return forest;
	}
	
	// Load the forest from a single file or a directory of files
	@SuppressWarnings("deprecation")
	public static CopyOfDecisionForest load(Configuration conf, Path forestPath) throws IOException {
		FileSystem fs = forestPath.getFileSystem(conf);
		Path[] files;
		if (fs.getFileStatus(forestPath).isDir()) {
			files = DFUtils.listOutputFiles(fs, forestPath);
		} else {
			files = new Path[] { forestPath };
		}
		
		CopyOfDecisionForest forest = null;
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
