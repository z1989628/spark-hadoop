package randomForest.df.builder;


import java.util.Random;

import randomForest.df.data.Data;
import randomForest.df.node.Node;


//Abstract base class for TreeBuilders
public interface TreeBuilder {

	// Builds a Decision tree using the training data
	Node build(Random rng, Data data);
}
