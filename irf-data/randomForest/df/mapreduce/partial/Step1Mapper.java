package randomForest.df.mapreduce.partial;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import randomForest.df.Bagging;
import randomForest.df.data.Data;
import randomForest.df.data.DataConverter;
import randomForest.df.data.Instance;
import randomForest.df.mapreduce.Builder;
import randomForest.df.mapreduce.MapredMapper;
import randomForest.df.mapreduce.MapredOutput;
import randomForest.df.node.Node;

// First step of the Partial Data Builder. Builds the trees using the data available in the InputSplit.
// Predict the oob classes for each tree in its growing partition (input split)
public class Step1Mapper  extends MapredMapper<LongWritable, Text, TreeID, MapredOutput> {

	private static final Logger log = LoggerFactory.getLogger(Step1Mapper.class);
	
	// used to convert input values to data instances
	private DataConverter converter;
	
	private Random rng;
	
	// number of trees to be built by this mapper
	private int nbTrees;
	
	// id of the first tree
	private int firstTreeId;
	
	// mapper's partition
	private int partition;
	
	// will contain all instances if this mapper's split
	private final List<Instance> instances = Lists.newArrayList();
	
	public int getFirstTreeId() {
		return firstTreeId;
	}
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		
		configure(Builder.getRandomSeed(conf), 
				  conf.getInt("mapred.task.partition", -1), 
				  Builder.getNumMaps(conf), 
				  Builder.getNbTrees(conf));
	}
	
	// Useful when testing
	protected void configure(Long seed, int partition, int numMapTasks, int numTrees) {
		converter = new DataConverter(getDataset());
		
		// prepare random-numbers generator
		log.debug("seed : {}", seed);
		if (seed == null) {
			rng = RandomUtils.getRandom();
		} else {
			rng = RandomUtils.getRandom(seed);
		}
	
		// mapper's partition
		Preconditions.checkArgument(partition >= 0, "Wrong partition ID : " + partition + ". Partition must be >= 0!");
		this.partition = partition;
		
		// compute number of trees to build
		nbTrees = nbTrees(numMapTasks, numTrees, partition);
		
		// compute first tree id
		firstTreeId = 0;
		for (int p = 0; p < partition; p++) {
			firstTreeId += nbTrees(numMapTasks, numTrees, p);
		}
		
		log.debug("partition : {}", partition);
		log.debug("nbTrees : {}", nbTrees);
		log.debug("firstTreeId : {}", firstTreeId);
	}
	
	// Compute the number of trees for a given partition. The first partitions may be longer than the rest because of the remainder.
	public static int nbTrees(int numMaps, int numTrees, int partition) {
		int treesPerMapper = numTrees / numMaps;
		int remainder = numTrees - numMaps * treesPerMapper;
		return treesPerMapper + (partition < remainder ? 1 : 0);
	}
	
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		instances.add(converter.convert(value.toString()));
	}
	
	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException {
		// prepare the data
		log.debug("partition: {} numberInstances: {}", partition, instances.size());
		
		Data data = new Data(getDataset(), instances);
		Bagging bagging = new Bagging(getTreeBuilder(), data);
		
		TreeID key = new TreeID();
		
		log.debug("Building {} trees", nbTrees);
		for (int treeId = 0; treeId < nbTrees; treeId++) {
			log.debug("Building tree number : {}", treeId);
			
			Node tree = bagging.build(rng);
			
			key.set(partition, firstTreeId + treeId);
			
			if (isOutput()) {
				MapredOutput emOut = new MapredOutput(tree);
				context.write(key, emOut);
			}
			
			context.progress();
		}
	}
}
