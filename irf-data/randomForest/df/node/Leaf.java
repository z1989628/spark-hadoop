package randomForest.df.node;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import randomForest.df.data.Instance;

//Represents a Leaf node
public class Leaf extends Node {

	private static final double EPSILON = 1.0e-6;
	
	private double label;
	
	Leaf() { }
	
	public Leaf(double label) {
		this.label = label;
	}

	@Override
	public double classify(Instance instance) {
		return label;
	}

	@Override
	public long nbNodes() {
		return 1;
	}

	@Override
	public long maxDepth() {
		return 1;
	}

	@Override
	protected Type getType() {
		return Type.LEAF;
	}

	@Override
	protected String getString() {
		return "";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Leaf)) {
			return false;
		}
		
		Leaf leaf = (Leaf) obj;
		
		return Math.abs(label - leaf.label) < EPSILON;
	}
	
	@Override
	public int hashCode() {
		long bits = Double.doubleToLongBits(label);
		return (int) (bits ^ (bits >>> 32));
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		label = in.readDouble();
	}
	
	@Override
	protected void writeNode(DataOutput out) throws IOException {
		out.writeDouble(label);
	}

}
