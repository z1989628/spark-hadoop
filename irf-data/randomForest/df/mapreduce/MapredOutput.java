package randomForest.df.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import randomForest.df.DFUtils;
import randomForest.df.node.Node;

// Used by various implementation to return the result of a build.
// Contains a grown tree and its oob predictions
public class MapredOutput implements Writable, Cloneable {

	private Node tree;
	
	private int[] predictions;
	
	public MapredOutput() { }
	
	public MapredOutput(Node tree, int[] predictions) {
		this.tree = tree;
		this.predictions = predictions;
	}
	
	public MapredOutput(Node tree) {
		this(tree, null);
	}
	
	public Node getTree() {
		return tree;
	}
	
	public int[] getPredictions() {
		return predictions;
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		boolean readTree = in.readBoolean();
		if (readTree) {
			tree = Node.read(in);
		}
		
		boolean readPredictions = in.readBoolean();
		if (readPredictions) {
			predictions = DFUtils.readIntArray(in);
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeBoolean(tree != null);
		if (tree != null) {
			tree.write(out);
		}
		
		out.writeBoolean(predictions != null);
		if (predictions != null) {
			DFUtils.writeArray(out, predictions);
		}
	}
}
