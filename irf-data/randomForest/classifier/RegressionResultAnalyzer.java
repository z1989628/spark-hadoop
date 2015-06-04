package randomForest.classifier;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;


// ResultAnalyzer captures the classification statistics and displays in a tabular manner
public class RegressionResultAnalyzer {

	private static class Result {
		private final double actual;
		private final double result;
		Result(double actual, double result) {
			this.actual = actual;
			this.result = result;
		}
		double getActual() {
			return actual;
		}
		double getResult() {
			return result;
		}
	}
	
	private List<Result> results;
	
	public void addInstance(double actual, double result) {
		if (results == null) {
			results = Lists.newArrayList();
		}
		results.add(new Result(actual, result));
	}
	
	public void setInstances(double[][] results) {
		for (double[] res : results) {
			addInstance(res[0], res[1]);
		}
	}
	
	@Override
	public String toString() {
		double sumActual = 0.0;
		double sumActualSquared = 0.0;
		double sumResult = 0.0;
		double sumResultSquared = 0.0;
		double sumActualResult = 0.0;
		double sumAbsolute = 0.0;
		double sumAbsoluteSquared = 0.0;
		int predictable = 0;
		int unpredictable = 0;
		
		for (Result res : results) {
			double actual = res.getActual();
			double result = res.getResult();
			if (Double.isNaN(result)) {
				unpredictable ++;
			} else {
				sumActual += actual;
				sumActualSquared += actual * actual;
				sumResult += result;
				sumResultSquared += result * result;
				sumActualResult += actual * result;
				double absolute = Math.abs(actual - result);
				sumAbsolute += absolute;
				sumAbsoluteSquared += absolute * absolute;
				predictable ++;
			}
		}
		
		StringBuilder returnString = new StringBuilder();
		
		returnString.append("==============================================================\n");
		returnString.append("Summary\n");
		returnString.append("--------------------------------------------------------------\n");
		
		if (predictable > 0) {
			double varActual = sumActualSquared - sumActual * sumActual / predictable;
			double varResult = sumResultSquared - sumResult * sumResult / predictable;
			double varCo = sumActualResult - sumActual * sumResult / predictable;
			
			double correlation;
			if (varActual * varResult <= 0) {
				correlation = 0.0;
			} else {
				correlation = varCo / Math.sqrt(varActual * varResult);
			}Locale.setDefault(Locale.US);
			NumberFormat decimalFormatter = new DecimalFormat("0.####");
			
			returnString.append(StringUtils.rightPad("Correlation coefficient", 40)).append(": ")
						.append(StringUtils.leftPad(decimalFormatter.format(correlation), 10)).append('\n');
			returnString.append(StringUtils.rightPad("Mean absolute error", 40)).append(": ")
						.append(StringUtils.leftPad(decimalFormatter.format(sumAbsolute / predictable), 10)).append('\n');
			returnString.append(StringUtils.rightPad("Root mean squared error", 40)).append(":")
						.append(StringUtils.leftPad(decimalFormatter.format(Math.sqrt(sumAbsoluteSquared / predictable)), 10)).append('\n');
		}
		returnString.append(StringUtils.rightPad("Predictable Instances", 40)).append(": ")
					.append(StringUtils.leftPad(Integer.toString(predictable), 10)).append('\n');
		returnString.append(StringUtils.rightPad("Unpredictable Instances", 40)).append(": ")
					.append(StringUtils.leftPad(Integer.toString(unpredictable), 10)).append('\n');
		returnString.append(StringUtils.rightPad("Total Regression Instances", 40)).append(": ")
					.append(StringUtils.leftPad(Integer.toString(results.size()), 10)).append('\n');

		return returnString.toString();
	}
}
