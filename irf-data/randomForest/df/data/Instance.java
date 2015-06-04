package randomForest.df.data;

import org.apache.mahout.math.Vector;


//Represents one data instance.
public class Instance {

	//attributes, except LABEL and IGNORED
	private final Vector attrs;
	
	public Instance(Vector attrs) {
		this.attrs = attrs;
	}
	
	//Return the attribute at the specified position
	public double get(int index) {
		return attrs.getQuick(index);
	}
	
	//Set the value at given index
	public void set(int index, double value) {
		attrs.set(index, value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Instance)) {
			return false;
		}
		
		Instance instance = (Instance) obj;
		
		return /*id == instance.id &&*/ attrs.equals(instance.attrs);
	}
	
	@Override
	public int hashCode() {
		return /*id +*/ attrs.hashCode();
	}
}
