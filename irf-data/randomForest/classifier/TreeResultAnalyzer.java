package randomForest.classifier;

import java.util.Collection;

// Captures the classification statistics of a tree and compute the recall
public class TreeResultAnalyzer {

	private final ConfusionMatrix confusionMatrix;
	private int correctlyClassified;
	private int incorrectlyClassified;
	private int truePositive;
	private int falseNegative;
	
	public TreeResultAnalyzer(Collection<String> labelSet, String defaultLabel) {
		confusionMatrix = new ConfusionMatrix(labelSet, defaultLabel);
	}
	
	public boolean addInstance(String correctLabel, ClassifierResult classifiedResult) {
		boolean result = correctLabel.equals(classifiedResult.getLabel());
		if (result) {
			correctlyClassified ++;
			if (correctLabel.equals("1")) 
				truePositive ++;
		} else {
			incorrectlyClassified ++;
			if (correctLabel.equals("1"))
				falseNegative ++;
		}
		confusionMatrix.addInstance(correctLabel, classifiedResult);
		return result;
	}

	public double getRecall() {
		return (double) truePositive / (truePositive + falseNegative);
	}
	
	public double getAccuracy() {
		return (double) correctlyClassified / (correctlyClassified + incorrectlyClassified);
	}
}
