package randomForest.df.mapreduce;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.mahout.common.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import randomForest.df.Bagging;
import randomForest.df.data.Data;
import randomForest.df.data.DataLoader;
import randomForest.df.data.Dataset;
import randomForest.df.mapreduce.InMemInputFormat.InMemInputSplit;
import randomForest.df.node.Node;

public class InMemMapper extends MapredMapper<IntWritable, NullWritable, IntWritable, MapredOutput> {

	private static final Logger log = LoggerFactory.getLogger(InMemMapper.class);
	
	private Bagging bagging;
	
	private Random rng;
	
	// Load the training data
	private static Data loadData(Configuration conf, Dataset dataset) throws IOException {
		Path dataPath = Builder.getDistributedCacheFile(conf, 1);
		FileSystem fs = FileSystem.get(dataPath.toUri(), conf);
		return DataLoader.loadData(dataset, fs, dataPath);
	}
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		
		Configuration conf = context.getConfiguration();
		
		log.info("Loading the data...");
		Data data = loadData(conf, getDataset());
		log.info("Data loaded : {} instances", data.size());
		
		bagging = new Bagging(getTreeBuilder(), data);
	}
	
	@Override
	protected void map(IntWritable key, NullWritable value, Context context) 
		throws IOException, InterruptedException {
		map(key, context);
	}
	
	void map(IntWritable key, Context context) throws IOException, InterruptedException {
		
		initRandom((InMemInputSplit) context.getInputSplit());
		
		log.debug("Building...");
		Node tree = bagging.build(rng);
		
		if (isOutput()) {
			log.debug("Outputing...");
			MapredOutput mrOut = new MapredOutput(tree);
			
			context.write(key, mrOut);
		}
	}
	
	void initRandom(InMemInputSplit split) {
		if (rng == null) {
			// first execution of this mapper
			Long seed = split.getSeed();
			log.debug("Initialising rng with seed : {}", seed);
			rng = seed == null ? RandomUtils.getRandom() : RandomUtils.getRandom(seed);
		}
	}
}
