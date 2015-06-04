package randomForest.df.split;

import randomForest.df.data.Data;


// Computes the best split using the Information Gain measure
public abstract class IgSplit {

	static final double LOG2 = Math.log(2.0);
	
	// Computes the best split for the given attribute
	public abstract Split computeSplit(Data data, int attr);
}
