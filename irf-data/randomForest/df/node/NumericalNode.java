package randomForest.df.node;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import randomForest.df.data.Instance;

public class NumericalNode extends Node {

	private int attr;	// numerical attribute to split for
	
	private double split;	// split value
	
	private Node loChild;	// child node when attribute's value < split value
	
	private Node hiChild;	// child node when attribute's value >= split value
	
	public NumericalNode() { }
	
	public NumericalNode(int attr, double split, Node loChild, Node hiChild) {
		this.attr = attr;
		this.split = split;
		this.loChild = loChild;
		this.hiChild = hiChild;
	}

	@Override
	public double classify(Instance instance) {
		if (instance.get(attr)< split) {
			return loChild.classify(instance);
		} else {
			return hiChild.classify(instance);
		}
	}

	@Override
	public long nbNodes() {
		return 1 + loChild.nbNodes() + hiChild.nbNodes();
	}

	@Override
	public long maxDepth() {
		return 1 + Math.max(loChild.maxDepth(), hiChild.maxDepth());
	}

	@Override
	protected Type getType() {
		return Type.NUMERICAL;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NumericalNode)) {
			return false;
		}
		
		NumericalNode node = (NumericalNode) obj;
		
		return attr == node.attr & split == node.split && loChild.equals(node.loChild) && hiChild.equals(node.hiChild);
	}
	
	@Override 
	public int hashCode() {
		return attr + (int) Double.doubleToLongBits(split) + loChild.hashCode() + hiChild.hashCode();
	}
	
	@Override
	protected String getString() {
		return loChild.toString() + ',' + hiChild.toString();
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		attr = in.readInt();
		split = in.readDouble();
		loChild = Node.read(in);
		hiChild = Node.read(in);
	}

	@Override
	protected void writeNode(DataOutput out) throws IOException {
		out.writeInt(attr);
		out.writeDouble(split);
		loChild.write(out);
		hiChild.write(out);
	}
}
