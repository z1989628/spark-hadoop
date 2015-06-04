package randomForest.classifier;


// Result of a document classification. The label and the associated score (usually probability)
public class ClassifierResult {

	private String label;
	private double score;
	private double logLikelyhood = Double.MAX_VALUE;
	
	public ClassifierResult() { }
	
	public ClassifierResult(String label, double score) {
		this.label = label;
		this.score = score;
	}
	
	public ClassifierResult(String label) {
		this.label = label;
	}
	
	public ClassifierResult(String label, double score, double logLikelihood) {
		this.label = label;
		this.score = score;
		this.logLikelyhood = logLikelihood;
	}
	
	public double getLogLikelihood() {
		return logLikelyhood;
	}
	
	public void setLogLikelihood(double logLikelihood) {
		this.logLikelyhood = logLikelihood;
	}
	
	public String getLabel() {
		return label;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "ClassifierResult{" + "category='" + label + '\'' + ", score=" + score + '}';
	}
}

