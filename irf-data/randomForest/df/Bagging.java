package randomForest.df;

import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import randomForest.df.builder.TreeBuilder;
import randomForest.df.data.Data;
import randomForest.df.node.Node;


// Builds a tree using bagging
public class Bagging {

	private static final Logger log = LoggerFactory.getLogger(Bagging.class);
	
	private final TreeBuilder treeBuilder;
	
	private final Data data;
	
	private final boolean[] sampled;
	
	public Bagging(TreeBuilder treeBuilder, Data data) {
		this.treeBuilder = treeBuilder;
		this.data = data;
		sampled = new boolean[data.size()];
	}
	
	// Builds one tre
	public Node build(Random rng) {
		log.debug("Bagging...");
		Arrays.fill(sampled, false);
		Data bag = data.bagging(rng, sampled);
		
		log.debug("Building...");
		return treeBuilder.build(rng, bag);
	}
}
