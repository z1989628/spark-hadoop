package randomForest.df.mapreduce.partial;

import org.apache.hadoop.io.LongWritable;

import com.google.common.base.Preconditions;

// Indicates both the tree and the data partition used to grow the tree
public class TreeID extends LongWritable implements Cloneable {

	public static final int MAX_TREEID = 100000;
	
	public TreeID() { }
	
	public TreeID(int partition, int treeId) {
		Preconditions.checkArgument(partition >= 0, "Wrong partition: " + partition + ". Partition must be >= 0!");
		Preconditions.checkArgument(treeId >= 0,"Wrong treeId: " + treeId + ". TreeId must be >= 0!");
		set(partition, treeId);
	}
	
	public void set(int partition, int treeId) {
		set((long) partition * MAX_TREEID + treeId);
	}
	
	// Data partition (InputSplit's index) that was used to grow the tree
	public int partition() {
		return (int) (get() / MAX_TREEID);
	}
	
	public int treeId() {
		return (int) (get() % MAX_TREEID);
	}
	
	@Override
	public TreeID clone() {
		return new TreeID(partition(), treeId());
	}
}
