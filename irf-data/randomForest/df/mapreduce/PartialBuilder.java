package randomForest.df.mapreduce;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import randomForest.df.DFUtils;
import randomForest.df.DecisionForest;
import randomForest.df.builder.TreeBuilder;
import randomForest.df.mapreduce.partial.Step1Mapper;
import randomForest.df.mapreduce.partial.TreeID;
import randomForest.df.node.Node;

// Builds a random forest using partial data. Each mapper uses only the data given by its InputSplit
public class PartialBuilder extends Builder {

	private static final Logger log = LoggerFactory.getLogger(PartialBuilder.class);
	
	public PartialBuilder(TreeBuilder treeBuilder, Path dataPath, Path datasetPath, Long seed) {
		this(treeBuilder, dataPath, datasetPath, seed, new Configuration());
	}
	
	public PartialBuilder(TreeBuilder treeBuilder, 
								 Path dataPath, 
								 Path datasetPath, 
								 Long seed, 
								 Configuration conf) {
		super(treeBuilder, dataPath, datasetPath, seed, conf);
	}
	
	@Override
	protected void configureJob(Job job) throws IOException {
		Configuration conf = job.getConfiguration();
		
		job.setJarByClass(PartialBuilder.class);
		
		FileInputFormat.setInputPaths(job, getDataPath());
		FileOutputFormat.setOutputPath(job, getOutputPath(conf));
		
		job.setOutputKeyClass(TreeID.class);
		job.setOutputValueClass(MapredOutput.class);
		
		job.setMapperClass(Step1Mapper.class);
		job.setNumReduceTasks(0); 
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		// For this implementation to work, mapred.map.tasks needs to be set to the actual
		// number of mappers Hadoop will use:
		TextInputFormat inputFormat = new TextInputFormat();
		List<?> splits = inputFormat.getSplits(job);
		if (splits == null || splits.isEmpty()) {
			log.warn("Unable to compute number of splits?");
		} else {
			int numSplits = splits.size();
			log.info("Setting mapred.map.tasks = {}", numSplits);
			conf.setInt("mapred.map.tasks", numSplits);
		}
	}
	
	@Override
	protected DecisionForest parseOutput(Job job) throws IOException {
		Configuration conf = job.getConfiguration();
		
		int numTrees = Builder.getNbTrees(conf);
		
		Path outputPath = getOutputPath(conf);
		
		TreeID[] keys = new TreeID[numTrees];
		Node[] trees = new Node[numTrees];
		
		processOutput(job, outputPath, keys, trees);
		
		return new DecisionForest(Arrays.asList(trees));
	}

	// Processes the output from the output path.
	protected static void processOutput(JobContext job, 
										Path outputPath,
										TreeID[] keys,
										Node[] trees) throws IOException {
		Preconditions.checkArgument(keys == null && trees == null || keys != null && trees != null,
								 	"if keys is null, trees should also be null");
		Preconditions.checkArgument(keys == null || keys.length == trees.length, "keys.length != trees.length");
		
		Configuration conf = job.getConfiguration();
		FileSystem fs = outputPath.getFileSystem(conf);
		Path[] outfiles = DFUtils.listOutputFiles(fs, outputPath);
		
		// read all the outputs
		int index = 0;
		for (Path path : outfiles) {
			for (Pair<TreeID, MapredOutput> record : new SequenceFileIterable<TreeID, MapredOutput>(path, conf)) {
				TreeID key = record.getFirst();
				MapredOutput value = record.getSecond();
				if (keys != null) {
					keys[index] = key;
				}
				if (trees != null) {
					trees[index] = value.getTree();
				}
				index ++;
			}
		}
		
		// make sure we got all the keys/values
		if (keys != null && index != keys.length) {
			throw new IllegalStateException("Some key/values are missing from the output");
		}
	}
}
