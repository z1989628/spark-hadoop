package randomForest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

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
import org.apache.mahout.common.commandline.DefaultOptionCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import randomForest.classifier.ClassifierResult;
import randomForest.classifier.TreeResultAnalyzer;
import randomForest.df.DFUtils;
import randomForest.df.DecisionForest;
import randomForest.df.data.Data;
import randomForest.df.data.DataLoader;
import randomForest.df.data.Dataset;

public class ImproveForest extends Configured implements Tool {

	private static final Logger log = LoggerFactory.getLogger(ImproveForest.class);
	
	private FileSystem dataFS;
	private Path improveDataPath; // improve data path
	
	private Path datasetPath;
	
	private Path modelPath;
	
	private FileSystem outFS;
	private Path outputModelPath;  // path to predictions file, if null do not output the predictions
	
	private int nbTrees;
	
	@Override
	public int run(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		
		DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		ArgumentBuilder abuilder = new ArgumentBuilder();
		GroupBuilder gbuilder = new GroupBuilder();
		
		Option inputOpt = DefaultOptionCreator.inputOption().create();
		
		Option datasetOpt = obuilder.withLongName("dataset").withShortName("ds").withRequired(true)
				.withArgument(abuilder.withName("dataset").withMinimum(1).withMaximum(1).create())
				.withDescription("Dataset path").create();
		
		Option modelOpt = obuilder.withLongName("model").withShortName("m").withRequired(true)
				.withArgument(abuilder.withName("path").withMinimum(1).withMaximum(1).create())
				.withDescription("Path to the Decision Forest").create();
		
		Option outputOpt = obuilder.withLongName("output").withShortName("o").withRequired(true)
				.withArgument(abuilder.withName("path").withMinimum(1).withMaximum(1).create())
				.withDescription("Output path, will contain the Improved Decision Forest").create();
		
		Option nbtreesOpt = obuilder.withLongName("nbtrees").withShortName("t").withRequired(true)
				.withArgument(abuilder.withName("nbtrees").withMinimum(1).withMaximum(1).create())
				.withDescription("Number of trees to grow").create();
		
		Option helpOpt = DefaultOptionCreator.helpOption();
		
		Group group = gbuilder.withName("Options").withOption(inputOpt).withOption(datasetOpt)
				.withOption(modelOpt).withOption(outputOpt).withOption(nbtreesOpt).withOption(helpOpt)
				.create();
		
		try {
			Parser parser = new Parser();
			parser.setGroup(group);
			CommandLine cmdLine = parser.parse(args);
		
			if (cmdLine.hasOption("help")) {
				CommandLineUtil.printHelp(group);
				return -1;
			}
			
			String dataName = cmdLine.getValue(inputOpt).toString();
			String datasetName = cmdLine.getValue(datasetOpt).toString();
			String modelName = cmdLine.getValue(modelOpt).toString();
			nbTrees = Integer.parseInt(cmdLine.getValue(nbtreesOpt).toString());
			String outputName = cmdLine.hasOption(outputOpt) ? cmdLine.getValue(outputOpt).toString() : null;
			
			if (log.isDebugEnabled()) {
				log.debug("input		: {}", dataName);
				log.debug("dataset		: {}", datasetName);
				log.debug("model		: {}", modelName);
				log.debug("output		: {}", outputName);
			}
			
			improveDataPath = new Path(dataName);
			datasetPath = new Path(datasetName);
			modelPath = new Path(modelName);
			outputModelPath = new Path(outputName);
		} catch (OptionException e) {
			log.warn(e.toString(), e);
			CommandLineUtil.printHelp(group);
			return -1;
		}
		
		improveForest();
		
		return 0;
	}
	
	private void improveForest() throws IOException, ClassNotFoundException, InterruptedException {
		
		// make sure the output file does not exist
		outFS = outputModelPath.getFileSystem(getConf());
		if (outFS.exists(outputModelPath)) {
			throw new IllegalArgumentException("Output path already exists");
		}
		
		FileSystem mfs = modelPath.getFileSystem(getConf());
		if (!mfs.exists(modelPath)) {
			throw new IllegalArgumentException("The forest path does not exist");
		}
		
		// make sure the improve data exist
		dataFS = improveDataPath.getFileSystem(getConf());
		if (!dataFS.exists(improveDataPath)) {
			throw new IllegalArgumentException("The improve data path does not exis");
		}
		
		improve();
	}
	
	private void improve() throws ClassNotFoundException, InterruptedException, IOException {
		
		DecisionForest classifier = DecisionForest.load(getConf(), modelPath);
		Dataset dataset = Dataset.load(getConf(), datasetPath);
		Data data = DataLoader.loadData(dataset, 
										improveDataPath.getFileSystem(getConf()), 
										improveDataPath);
		double[][][] results = classifier.getResults(data, dataset);
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		if (results != null) {
			TreeResultAnalyzer[] analyzers = new TreeResultAnalyzer[nbTrees];
			for (int i = 0; i < results.length; i++) {
				analyzers[i] = new TreeResultAnalyzer(Arrays.asList(dataset.labels()), "unknow");
				for (double[] res : results[i]) {
					analyzers[i].addInstance(dataset.getLabelString(res[0]),
							new ClassifierResult(dataset.getLabelString(res[1]), 1.0));
				}
				weights.put(i, analyzers[i].getRecall());
			}
			classifier.setWeights(weights);
		}
		
		log.info("Forest num Nodes: {}", classifier.nbNodes());
		log.info("Forest mean num Nodes: {}", classifier.meanNbNodes());
		log.info("Forest mean max Depth: {}", classifier.meanMaxDepth());
		
		log.info("Forest weights: {}", classifier.getWeights());
		log.info("Forest hasWeights: {}", classifier.getHasWeights());
		
		// store the decision forest in the output path
		Path forestPath = new Path(outputModelPath, "improvedForest.seq");
		log.info("Storing the improved forest in: {}", forestPath);
		DFUtils.storeWritable(getConf(), forestPath, classifier);
	}
	
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Configuration(), new ImproveForest(), args);
	}
}
