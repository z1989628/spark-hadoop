package randomForest.df.data;

import java.util.List;
import java.util.Locale;

import randomForest.df.data.Dataset.Attribute;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

// Contains various methods that deal with descriptor strings
public class DescriptorUtils {
	
	private static final Splitter SPACE = Splitter.on(' ').omitEmptyStrings();
	
	private DescriptorUtils() { }
	
	// Parses a descriptor string and generates the corresponding array of Attributes
	public static Attribute[] parseDescriptor(CharSequence descriptor) throws DescriptorException {
		List<Attribute> attributes = Lists.newArrayList();
		for (String token : SPACE.split(descriptor)) {
			token = token.toUpperCase(Locale.ENGLISH);
			if ("I".equals(token)) {
				attributes.add(Attribute.IGNORED);
			} else if ("N".equals(token)) {
				attributes.add(Attribute.NUMERICAL);
			} else if ("C".equals(token)) {
				attributes.add(Attribute.CATEGORICAL);
			} else if ("L".equals(token)) {
				attributes.add(Attribute.LABEL);
			} else {
				throw new DescriptorException("Bad Token : " + token);
			}
		}
		return attributes.toArray(new Attribute[attributes.size()]);
	}
	
	// Generates a valid descriptor string from a user-friendly representation.
	public static String generateDescriptor(CharSequence description) throws DescriptorException {
		return generateDescriptor(SPACE.split(description));
	}
	
	// Generates a valid descriptor string from a list of tokens
	public static String generateDescriptor(Iterable<String> tokens) throws DescriptorException {
		StringBuilder descriptor = new StringBuilder();
		
		int multiplicator = 0;
		
		for (String token : tokens) {
			try {
				// try to parse an integer
				int number = Integer.parseInt(token);
				 
				if (number <= 0) {
					throw new DescriptorException("Multiplicator (" + number + ") must be > 0");
				}
				if (multiplicator > 0) {
					throw new DescriptorException("A multiplicator cannot be followed by another multiplicator");
				}
				multiplicator = number;
			} catch (NumberFormatException e) {
				// token is not a number
				if (multiplicator == 0) {
					multiplicator = 1;
				}
				
				for (int index = 0; index < multiplicator; index++) {
					descriptor.append(token).append(' ');
				}
				
				multiplicator = 0;
			}
		}
		
		return descriptor.toString().trim();
	}
}
