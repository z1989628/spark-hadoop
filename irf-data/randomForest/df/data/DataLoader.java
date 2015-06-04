package randomForest.df.data;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import randomForest.df.data.Dataset.Attribute;

// Converts the input data to a Vector Array using the information given by the Dataset
// Generates for each line a Vector that contains :
//  double parsed value for  NUMBERICAL attributes
//  int value for CATEGORICAL and LABEL attributes
// adds an IGNORED first attribute that will contain a unique id for each instance, which is the line number of the instance in the input data
public final class DataLoader {

	private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
	
	private static final Pattern SEPARATORS = Pattern.compile("[, ]");
	
	private DataLoader() { }
	
	// Converts a comma-separated String to a Vector.
	private static boolean parseString(Attribute[] attrs, Set<String>[] values, CharSequence String, boolean regression) {
		String[] tokens = SEPARATORS.split(String);
		Preconditions.checkArgument(tokens.length == attrs.length, "Wrong number of attributes in the string: " + tokens.length + ". Must be:" + attrs.length);
		
		// extract tokens and check is there is any missing value
		for (int attr = 0; attr < attrs.length; attr++) {
			if (!attrs[attr].isIgnored() && "?".equals(tokens[attr])) {
				return false; // missing value
			} 
		}
		
		for (int attr = 0; attr < attrs.length; attr++) {
			if (!attrs[attr].isIgnored()) {
				String token = tokens[attr];
				if (attrs[attr].isCategorical() || (!regression && attrs[attr].isLabel())) {
					// update values
					if (values[attr] == null) {
						values[attr] = Sets.newHashSet();
					}
					values[attr].add(token);
				} else {
					try {
						Double.parseDouble(token);
					} catch (NumberFormatException e) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	// Loads the data from a file
	public static Data loadData(Dataset dataset, FileSystem fs, Path fpath) throws IOException {
		FSDataInputStream input = fs.open(fpath);
		Scanner scanner = new Scanner(input, "UTF-8");
		
		List<Instance> instances = Lists.newArrayList();
		
		DataConverter converter = new DataConverter(dataset);
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (!line.isEmpty()) {
				Instance instance = converter.convert(line);
				if (instance != null) {
					instances.add(instance);
				} else {
					// missing values found
					log.warn("{}: missing values", instances.size());
				}
			} else {
				log.warn("{}: empty string", instances.size());
			}
		}
		
		scanner.close();
		return new Data(dataset, instances);
	}
	
	// Loads the data from multiple paths specified by pathes
	public static Data loadData(Dataset dataset, FileSystem fs, Path[] pathes) throws IOException {
		List<Instance> instances = Lists.newArrayList();
		
		for (Path path : pathes) {
			Data loadedData = loadData(dataset, fs, path);
			for (int index = 0; index <= loadedData.size(); index++) {
				instances.add(loadedData.get(index));
			}
		}
		return new Data(dataset, instances);
	}
	
	// Loads the data from a String array
	public static Data loadData(Dataset dataset, String[] data) {
		List<Instance> instances = Lists.newArrayList();
		
		DataConverter converter = new DataConverter(dataset);
		
		for (String line : data) {
			if (!line.isEmpty()) {
				Instance instance = converter.convert(line);
				if (instance != null) {
					instances.add(instance);
				} else {
					// missing values found
					log.warn("{}: missing values", instances.size());
				}
			} else {
				log.warn("{}: empty string", instances.size());
			}
		}
		
		return new Data(dataset, instances);
	}
	
	// Generates the Dataset by parsing the entire data
	public static Dataset generateDataset(CharSequence descriptor, boolean regression,
										  FileSystem fs, Path path) throws DescriptorException, IOException {
		Attribute[] attrs = DescriptorUtils.parseDescriptor(descriptor);
		
		FSDataInputStream input = fs.open(path);
		Scanner scanner = new Scanner(input, "UTF-8");
		
		// used to convert CATEGORICAL attribute to Integer
		@SuppressWarnings("unchecked")
		Set<String>[] valsets = new Set[attrs.length];
		
		int size = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (!line.isEmpty()) {
				if (parseString(attrs, valsets, line, regression)) {
					size ++;
				}
			}
		}
		
		scanner.close();
		
		@SuppressWarnings("unchecked")
		List<String>[] values = new List[attrs.length];
		for (int i = 0; i < valsets.length; i++) {
			if (valsets[i] != null) {
				values[i] = Lists.newArrayList(valsets[i]);
			}
		}
		
		return new Dataset(attrs, values, size, regression);
	}
}
