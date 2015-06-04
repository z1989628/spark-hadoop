package randomForest;

import java.io.IOException;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.common.CommandLineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import randomForest.df.DFUtils;
import randomForest.df.DecisionForest;
import randomForest.df.builder.DecisionTreeBuilder;
import randomForest.df.data.Data;
import randomForest.df.data.DataLoader;
import randomForest.df.data.Dataset;
import randomForest.df.mapreduce.Builder;
import randomForest.df.mapreduce.InMemBuilder;
import randomForest.df.mapreduce.PartialBuilder;



public class BuildForest extends Configured implements Tool {

	private static final Logger log = LoggerFactory.getLogger(BuildForest.class);
	
	private Path dataPath;
	
	private Path datasetPath;
	
	private Path outputPath;
	
	private Integer m; // Number of variables to select at each tree-node
	
	private boolean complemented; // tree is complemented
	
	private Integer minSplitNum; // minimum number for split
	
	private Double minVarianceProportion; // minimum proportion of the total variance for split
	
	private int nbTrees; // Number of trees to grow
	
	private Long seed; // Random seed
	
	private boolean isPartial; // use partial data implementation
	
	@Override
	public int run(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		
		DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
		ArgumentBuilder aBuilder = new ArgumentBuilder();
		GroupBuilder gBuilder = new GroupBuilder();
		
		Option dataOpt = oBuilder.withLongName("data").withShortName("d").withRequired(true)
				.withArgument(aBuilder.withName("path").withMinimum(1).withMaximum(1).create())
				.withDescription("Data path").create();
		
		Option datasetOpt = oBuilder.withLongName("dataset").withShortName("ds").withRequired(true)
				.withArgument(aBuilder.withName("dataset").withMinimum(1).withMaximum(1).create())
				.withDescription("Dataset path").create();
		
		Option selectionOpt = oBuilder.withLongName("selection").withShortName("sl").withRequired(false)
				.withArgument(aBuilder.withName("m").withMinimum(1).withMaximum(1).create())
				.withDescription("Optional, Number of variables to select randomly at each tree-node.\n"
				+ "For classification problem, the default is square root of the number of explanatory variables.\n"
				+ "For regression problem, the default is 1/3 of the number of explanatory variables.").create();
		
		Option noCompleteOpt = oBuilder.withLongName("no-complete").withShortName("nc").withRequired(false)
				.withDescription("Optional, The tree is not complemented").create();
		
		Option minSplitOpt = oBuilder.withLongName("minsplit").withShortName("ms").withRequired(false)
				.withArgument(aBuilder.withName("minsplit").withMinimum(1).withMaximum(1).create())
				.withDescription("Optional, The tree-node is not divided, if the branching data size is "
				+ "smaller than this value.\nThe default is 2").create();
		
		Option minPropOpt = oBuilder.withLongName("minprop").withShortName("mp").withRequired(false)
				.withArgument(aBuilder.withName("minprop").withMinimum(1).withMaximum(1).create())
				.withDescription("Optional, The tree-node is not divided, if the proportion of the " 
				+ "variance of branching data is smaller than this value.\n"
				+ "The default is 1/1000(0.001).").create();
		
		Option seedOpt = oBuilder.withLongName("seed").withShortName("sd").withRequired(false)
				.withArgument(aBuilder.withName("seed").withMinimum(1).withMaximum(1).create())
				.withDescription("Optional, seed value used to initialise the Random number generator").create();
		
		Option partialOpt = oBuilder.withLongName("partial").withShortName("p").withRequired(false)
				.withDescription("Optional, use the Partial Data implementation").create();
		
		Option nbtreesOpt = oBuilder.withLongName("nbtrees").withShortName("t").withRequired(true)
				.withArgument(aBuilder.withName("nbtrees").withMinimum(1).withMaximum(1).create())
				.withDescription("Number of trees to grow").create();
		
		Option outputOpt = oBuilder.withLongName("output").withShortName("o").withRequired(true)
				.withArgument(aBuilder.withName("path").withMinimum(1).withMaximum(1).create())
				.withDescription("Output path, will contain the Decision Forest").create();
		
		Option helpOpt = oBuilder.withLongName("help").withShortName("h")
				.withDescription("Print out help").create();
		
		Group group = gBuilder.withName("Options").withOption(dataOpt).withOption(datasetOpt)
				.withOption(selectionOpt).withOption(noCompleteOpt).withOption(minSplitOpt)
				.withOption(minPropOpt).withOption(seedOpt).withOption(partialOpt).withOption(nbtreesOpt)
				.withOption(outputOpt).withOption(helpOpt).create();
		
		try {
			Parser parser = new Parser();
			parser.setGroup(group);
			CommandLine cmdLine = parser.parse(args);
			
			if (cmdLine.hasOption("help")) {
				CommandLineUtil.printHelp(group);
				return -1;
			}
			
			isPartial = cmdLine.hasOption(partialOpt);
			String dataName = cmdLine.getValue(dataOpt).toString();
			String datasetName = cmdLine.getValue(datasetOpt).toString();
			String outputName = cmdLine.getValue(outputOpt).toString();
			nbTrees = Integer.parseInt(cmdLine.getValue(nbtreesOpt).toString());
			
			if (cmdLine.hasOption(selectionOpt)) {
				m = Integer.parseInt(cmdLine.getValue(selectionOpt).toString());
			}
			complemented = !cmdLine.hasOption(noCompleteOpt);
			if (cmdLine.hasOption(minSplitOpt)) {
				minSplitNum = Integer.parseInt(cmdLine.getValue(minSplitOpt).toString());
			}
			if (cmdLine.hasOption(minPropOpt)) {
				minVarianceProportion = Double.parseDouble(cmdLine.getValue(minPropOpt).toString());
			}
			if (cmdLine.hasOption(seedOpt)) {
				seed = Long.valueOf(cmdLine.getValue(seedOpt).toString());
			}
			
			if (log.isDebugEnabled()) {
				log.debug("data : {}", dataName);
				log.debug("dataset : {}", datasetName);
				log.debug("output : {}", outputName);
				log.debug("m : {}", m);
				log.debug("complemented : {}", complemented);
				log.debug("minSplitNum : {}", minSplitNum);
				log.debug("minVarianceProportion : {}", minVarianceProportion);
				log.debug("seed : {}", seed);
				log.debug("nbtrees : {}", nbTrees);
				log.debug("isPartial : {}", isPartial);
			}
			
			dataPath = new Path(dataName);
			datasetPath = new Path(datasetName);
			outputPath = new Path(outputName);
			
		} catch (OptionException e) {
			log.error("Exception", e);
			CommandLineUtil.printHelp(group);
			return -1;
		}
		
		buildForest();
		
		return 0;
	}
	
	private void buildForest() throws IOException, ClassNotFoundException, InterruptedException {
		// make sure that output path does not exist
		FileSystem ofs = outputPath.getFileSystem(getConf());
		if (ofs.exists(outputPath)) {
			log.error("Output path already exists");
			return;
		}
		
		DecisionTreeBuilder treeBuilder = new DecisionTreeBuilder();
		if (m != null) {
			treeBuilder.setM(m);
		} 
		treeBuilder.setComplemented(complemented);
		if (minSplitNum != null) {
			treeBuilder.setMinSplitNum(minSplitNum);
		}
		if (minVarianceProportion != null) {
			treeBuilder.setMinVarinaceProportion(minVarianceProportion);
		}
		
		Builder forestBuilder;
		
		if (isPartial) {
			log.info("Partial Mapred implementation");
			forestBuilder = new PartialBuilder(treeBuilder, dataPath, datasetPath, seed, getConf());
		} else {
			log.info("InMem Mapred implementation");
			forestBuilder = new InMemBuilder(treeBuilder, dataPath, datasetPath, seed, getConf());
		}
		
		forestBuilder.setOutputDirName(outputPath.getName());
		
		log.info("Building the forest...");
		long time = System.currentTimeMillis();
		
		DecisionForest forest = forestBuilder.build(nbTrees);
		if (forest == null) {
			return;
		}
		
		time = System.currentTimeMillis() - time;
		log.info("Build Time: {}", DFUtils.elapsedTime(time));
		log.info("Forest num Nodes: {}", forest.nbNodes());
		log.info("Forest mean num Nodes: {}", forest.meanNbNodes());
		log.info("Forest mean max Depth: {}", forest.meanMaxDepth());
		
		// store the decision forest in the output path
		Path forestPath = new Path(outputPath, "forest.seq");
		log.info("Storing the forest in: {}", forestPath);
		DFUtils.storeWritable(getConf(), forestPath, forest);
	}
	
	protected static Data loadData(Configuration conf, Path dataPath, Dataset dataset) throws IOException {
		log.info("Loading the data...");
		FileSystem fs = dataPath.getFileSystem(conf);
		Data data = DataLoader.loadData(dataset, fs, dataPath);
		log.info("Data Loaded");
		
		return data;
	}
	
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Configuration(), new BuildForest(), args);
	}
}
