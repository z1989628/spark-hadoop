package randomForest.df.node;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import randomForest.df.DFUtils;
import randomForest.df.data.Instance;

public class CategoricalNode extends Node {

	private int attr;
	private double[] values;
	private Node[] childs;
	
	public CategoricalNode() { }
	
	public CategoricalNode(int attr, double[] values, Node[] childs) {
		this.attr = attr;
		this.values = values;
		this.childs = childs;
	}
	
	@Override
	public double classify(Instance instance) {
		int index = ArrayUtils.indexOf(values, instance.get(attr));
		if (index == -1) {
			//value not available, we cannot predict
			return Double.NaN;
		}
		return childs[index].classify(instance);
	}

	@Override
	public long nbNodes() {
		long nbNodes = 1;
		
		for (Node child : childs) {
			nbNodes += child.nbNodes();
		}
		
		return nbNodes;
	}

	@Override
	public long maxDepth() {
		long max = 0;
		
		for (Node child : childs) {
			long depth = child.maxDepth();
			if (depth > max)
				max = depth;
		}
		
		return max + 1;
	}

	@Override
	protected Type getType() {
		return Type.CATEGORICAL;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CategoricalNode)) {
			return false;
		}
		
		CategoricalNode node = (CategoricalNode) obj;
		
		return attr == node.attr && Arrays.equals(values, node.values) && Arrays.equals(childs, node.childs);
	}
	
	@Override 
	public int hashCode() {
		int hashCode = attr;
		for (double value : values) {
			hashCode = 31 * hashCode + (int) Double.doubleToLongBits(value);
		}
		for (Node node : childs) {
			hashCode = 31 * hashCode + node.hashCode();
		}
		
		return hashCode;
	}
	
	@Override
	protected String getString() {
		StringBuffer buffer = new StringBuffer();
		
		for (Node child : childs) {
			buffer.append(child).append(',');
		}

		return buffer.toString();
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		attr = in.readInt();
		values = DFUtils.readDoubleArray(in);
		childs = DFUtils.readNodeArray(in);
	}
	@Override
	protected void writeNode(DataOutput out) throws IOException {
		out.writeInt(attr);
		DFUtils.writeArray(out, values);
		DFUtils.writeArray(out, childs);
	}
}
