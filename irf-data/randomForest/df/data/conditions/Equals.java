package randomForest.df.data.conditions;

import randomForest.df.data.Instance;

public class Equals extends Condition {

	private final int attr;
	
	private final double value;
	
	public Equals(int attr, double value) {
		this.attr = attr;
		this.value = value;
	}
	
	@Override
	public boolean isTrueFor(Instance instance) {
		return instance.get(attr) == value;
	}

}
