package randomForest.df.node;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import randomForest.df.data.Instance;



//Represents an abstract node of a decision tree
public abstract class Node implements Writable {

	protected enum Type {
		LEAF,
		NUMERICAL,
		CATEGORICAL
	}
	
//	predicts the label for the instance
	public abstract double classify(Instance instance);

//	return the total number of nodes of the tree
	public abstract long nbNodes();
	
//	return the maximum depth of the tree
	public abstract long maxDepth();
	
	protected abstract Type getType();
	
	public static Node read(DataInput in) throws IOException {
		Type type = Type.values()[in.readInt()];
		Node node;
		
		switch (type) {
		case LEAF:
			node = new Leaf();
			break;
		case NUMERICAL:
			node = new NumericalNode();
			break;
		case CATEGORICAL:
			node = new CategoricalNode();
			break;
		default:
			throw new IllegalStateException(
					"This implementation is not currently supported");
		}
		
		node.readFields(in);
		
		return node;
	}
	
	public final String toString() {
		return getType() + ":" + getString() + ';';
	}
	
	protected abstract String getString();
	
	public final void write(DataOutput out) throws IOException {
		out.writeInt(getType().ordinal());
		writeNode(out);
	}
	
	protected abstract void writeNode(DataOutput out) throws IOException;
}
