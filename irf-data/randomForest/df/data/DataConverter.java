package randomForest.df.data;

import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mahout.math.DenseVector;

import com.google.common.base.Preconditions;

public class DataConverter {

	private static final Pattern COMMA_SPACE = Pattern.compile("[, ]");
	
	private final Dataset dataset;
	
	public DataConverter(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public Instance convert(CharSequence string) {
		// all attributes (categorical, numerical, label), ignored
		int nball = dataset.nbAttributes() + dataset.getIgnored().length;
		
		String[] tokens = COMMA_SPACE.split(string);
		Preconditions.checkArgument(tokens.length == nball, "Wrong number of attributes in the string: " + tokens.length + ". Must be " + nball);
		
		int nbattrs = dataset.nbAttributes();
		DenseVector vector = new DenseVector(nbattrs);
		
		int aId = 0;
		for (int attr = 0; attr < nball; attr++) {
			if (!ArrayUtils.contains(dataset.getIgnored(), attr)) {
				String token = tokens[attr].trim();
				
				if ("?".equals(token)) {
					// missing value
					return null;
				}
				
				if (dataset.isNumerical(aId)) {
					vector.set(aId++, Double.parseDouble(token));
				} else {
					// CATEGORICAL
					vector.set(aId, dataset.valueOf(aId, token));
					aId ++;
				}
			}
		}
		
		return new Instance(vector);
	}
}
