package randomForest.df.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import com.google.common.base.Preconditions;

import randomForest.df.builder.TreeBuilder;
import randomForest.df.data.Dataset;

// Base class for Mapred mappers. Loads common parameters from the job
public class MapredMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

	private boolean noOutput;
	
	private TreeBuilder treeBuilder;
	
	private Dataset dataset;
	
	// return whether mapper does estimate and output predictions
	protected boolean isOutput() {
		return !noOutput;
	}
	
	protected TreeBuilder getTreeBuilder() {
		return treeBuilder;
	}
	
	protected Dataset getDataset() {
		return dataset;
	}
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		
		Configuration conf = context.getConfiguration();
		
		configure(!Builder.isOutput(conf), Builder.getTreeBuilder(conf), Builder.loadDataset(conf));
	}
	
	// Useful for testing
	protected void configure(boolean noOutput, TreeBuilder treeBuilder, Dataset dataset) {
		Preconditions.checkArgument(treeBuilder != null, "TreeBuilder not found in the Job parameters");
		this.noOutput = noOutput;
		this.treeBuilder = treeBuilder;
		this.dataset = dataset;
	}
}
