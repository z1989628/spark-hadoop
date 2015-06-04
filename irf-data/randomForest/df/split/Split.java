package randomForest.df.split;

import java.util.Locale;


// Contains enough information to identify each split
public final class Split {

	private final int attr;
	private final double ig;
	private final double split;
	
	public Split(int attr, double ig, double split) {
		this.attr = attr;
		this.ig = ig;
		this.split = split;
	}
	
	public Split(int attr, double ig) {
		this(attr, ig, Double.NaN);
	}
	
	// return attribute to split for
	public int getAttr() {
		return attr;
	}
	
	// return Information Gain of the split
	public double getIg() {
		return ig;
	}
	
	// return split value for NUMERICAL attributes
	public double getSplit() {
		return split;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "attr: %d, ig: %f, split: %f", attr, ig, split);
	}
}
