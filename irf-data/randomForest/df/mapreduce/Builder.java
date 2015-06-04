package randomForest.df.mapreduce;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import randomForest.df.DecisionForest;
import randomForest.df.builder.TreeBuilder;
import randomForest.df.data.Dataset;


// Base class for Mapred DecisionForest builders. Takes care of storing the parameters common to the mapred
// implementations.
// The child classes must implement at least:
//  void configureJob(Job) : to further configure the job before its launch; and
//  DecisionForest parseOutput(Job, PredictionCallback) : in order to convert the job outputs into a 
// DecisionForest and its corresponding oob predictions
@SuppressWarnings("deprecation")
public abstract class Builder {

	private static final Logger log = LoggerFactory.getLogger(Builder.class);
	
	private final TreeBuilder treeBuilder;
	private final Path dataPath;
	private final Path datasetPath;
	private final Long seed;
	private final Configuration conf;
	private String outputDirName = "output";
	
	protected Builder(TreeBuilder treeBuilder, Path dataPath, Path datasetPath, Long seed, Configuration conf) {
		this.treeBuilder = treeBuilder;
		this.dataPath = dataPath;
		this.datasetPath = datasetPath;
		this.seed = seed;
		this.conf = new Configuration(conf);
	}
	
	protected Path getDataPath() {
		return dataPath;
	}
	
	// Return the value of "mapred.map.tasks".
	public static int getNumMaps(Configuration conf) {
		return conf.getInt("mapred.map.tasks", -1);
	}
	
	// Used only for DEBUG purposes. if false, the mappers doesn't output anything, so the buuilder has nothing to process
	protected static boolean isOutput(Configuration conf) {
		return conf.getBoolean("debug.mahout.rf.output", true);
	}
	
	// Returns the random seed
	public static Long getRandomSeed(Configuration conf) {
		String seed = conf.get("mahout.rf.random.seed");
		if (seed == null) {
			return null;
		}
		
		return Long.valueOf(seed);
	}
	
	// Sets the random seed value
	private static void setRandomSeed(Configuration conf, long seed) {
		conf.setLong("mahout.rf.random.seed", seed);
	}
	
	public static TreeBuilder getTreeBuilder(Configuration conf) {
		String string = conf.get("mahout.rf.treebuilder");
		if (string == null) {
			return null;
		}
		
		return StringUtils.fromString(string);
	}
	
	private static void setTreeBuilder(Configuration conf, TreeBuilder treeBuilder) {
		conf.set("mahout.rf.treebuilder", StringUtils.toString(treeBuilder));
	}
	
	// Get the number of trees for the map-reduce job
	public static int getNbTrees(Configuration conf) {
		return conf.getInt("mahout.rf.nbtrees", -1);
	}
	
	// Set the number of trees to grow fro the map-reduce job
	public static void setNbTrees(Configuration conf, int nbTrees) {
		Preconditions.checkArgument(nbTrees > 0, "nbTrees should be greater than 0");
		
		conf.setInt("mahout.rf.nbtrees", nbTrees);
	}
	
	// Sets the Output directory name, will be creating in the working directory
	public void setOutputDirName(String name) {
		outputDirName = name;
	}
	
	// Output Directory name
	protected Path getOutputPath(Configuration conf) throws IOException {
		// the output directory is accessed only by this class, so use the default file system
		FileSystem fs = FileSystem.get(conf);
		return new Path(fs.getWorkingDirectory(), outputDirName);
	}
	
	// Helper method. Get a path from the DistributedCache
	public static Path getDistributedCacheFile(Configuration conf, int index) throws IOException {
		Path[] files = HadoopUtil.getCachedFiles(conf);
		
		if (files.length <= index) {
			throw new IOException("path not found in the DistributedCache");
		}
		
		return files[index];
	}
	
	// Helper method. Load a Dataset stored in the DistributedCache
	public static Dataset loadDataset(Configuration conf) throws IOException {
		Path datasetPath = getDistributedCacheFile(conf, 0);
		
		return Dataset.load(conf, datasetPath);
	}
	
	// Used by the inheriting classes to configure the job
	protected abstract void configureJob(Job job) throws IOException;
	
	// Sequential implementation should override this method to simulate the job execution
	protected boolean runJob(Job job) throws ClassNotFoundException, IOException, InterruptedException {
		return job.waitForCompletion(true);
	}
	
	// Parse the output files to extract the trees and pass the predictions to the callback
	protected abstract DecisionForest parseOutput(Job job) throws IOException;
	
	public DecisionForest build(int nbTrees) throws IOException, ClassNotFoundException, InterruptedException {
		// int numTrees = getNbTrees(conf);
		
		Path outputPath = getOutputPath(conf);
		FileSystem fs = outputPath.getFileSystem(conf);
		
		// check the output
		if (fs.exists(outputPath)) {
			throw new IOException("Output path already exists : " + outputPath);
		}
		
		if (seed != null) {
			setRandomSeed(conf, seed);
		}
		setNbTrees(conf, nbTrees);
		setTreeBuilder(conf, treeBuilder);
		
		// put the dataset into the DistributedCache
		DistributedCache.addCacheFile(datasetPath.toUri(), conf);
		
		Job job = new Job(conf, "decision forest builder");
		
		log.debug("Configuring the job...");
		configureJob(job);
		
		log.debug("Running the job...");
		if (!runJob(job)) {
			log.error("Job failed!");
			return null;
		}
		
		if (isOutput(conf)) {
			log.debug("Parsing the output...");
			DecisionForest forest = parseOutput(job);
			HadoopUtil.delete(conf, outputPath);
			return forest;
		}
		
		return null;
	}
	
	// sort the splits into order based on size, so that the biggest go first.
	// This is the same code used by Hadoop's JobClient
	public static void sortSplits(InputSplit[] splits) {
		Arrays.sort(splits, new Comparator<InputSplit>() {
			@Override
			public int compare(InputSplit a, InputSplit b) {
				try {
					long left = a.getLength();
					long right = b.getLength();
					if (left == right) {
						return 0;
					} else if (left < right) {
						return 1;
					} else {
						return -1;
					}
				} catch (IOException ie) {
					throw new IllegalStateException("Problem getting input split size", ie);
				} catch (InterruptedException ie) {
					throw new IllegalStateException("Problem getting input split size", ie);
				}
			}
		});
	}
}
