package randomForest.df.data.conditions;


import randomForest.df.data.Instance;

// Condition on Instance
public abstract class Condition {

	// Returns true is the checked instance matches the condition
	public abstract boolean isTrueFor(Instance instance);
	
	// Condition that checks if the given attribute has a value "equal" to the given value
	public static Condition equals(int attr, double value) {
		return new Equals(attr, value);
	}
	
	// Condition that checks if the given attribute has a value "lesser" than the given value
	public static Condition lesser(int attr, double value) {
		return new Lesser(attr, value);
	}
	
	// Condition that checks if the given attribute has a value "greater or equal" than the given value
	public static Condition greaterOrEquals(int attr, double value) {
		return new GreaterOrEquals(attr, value);
	}
}
