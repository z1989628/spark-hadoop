package randomForest.df.data;

import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// Helper methods that deals with data lists and arrays of values
public final class DataUtils {
	private DataUtils() { }
	
	// Computes the sum of the values
	public static int sum(int[] values) {
		int sum = 0;
		for (int value : values) {
			sum += value;
		}
		
		return sum;
	}
	
	public static double sum(double[] values) {
		double sum = 0.0;
		for (double value : values) {
			sum += value;
		}
		
		return sum;
	}
	
	public static void add(int[] array1, int[] array2) {
		Preconditions.checkArgument(array1.length == array2.length, "array1.length != array2.length");
		for (int index = 0; index < array1.length; index ++) {
			array1[index] += array2[index];
		}
	}
	
	public static void dec(int[] array1, int[] array2) {
		Preconditions.checkArgument(array1.length == array2.length, "array1.length != array2.length");
		for (int index = 0; index < array1.length; index ++) {
			array1[index] -= array2[index];
		}
	}
	
	// return the index of the maximum of the array, breaking ties randomly
	public static int maxindex(Random rng, int[] values) {
		int max = 0;
		List<Integer> maxindices = Lists.newArrayList();
		
		for (int index = 0; index < values.length; index++) {
			if (values[index] > max) {
				max = values[index];
				maxindices.clear();
				maxindices.add(index);
			} else if (values[index] == max) {
				maxindices.add(index);
			}
		}
		
		return maxindices.size() > 1 ? maxindices.get(rng.nextInt(maxindices.size())) : maxindices.get(0);
	}
	
	public static int maxindex(Random rng, double[] values) {
		double max = 0;
		List<Integer> maxindices = Lists.newArrayList();
		
		for (int index = 0; index < values.length; index++) {
			if (values[index] > max) {
				max = values[index];
				maxindices.clear();
				maxindices.add(index);
			} else if (values[index] == max) {
				maxindices.add(index);
			}
		}
		
		return maxindices.size() > 1 ? maxindices.get(rng.nextInt(maxindices.size())) : maxindices.get(0);
	}
	
}
