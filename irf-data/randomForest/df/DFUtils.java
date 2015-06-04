package randomForest.df;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import randomForest.df.node.Node;

//Utility class that contains various helper methods
public class DFUtils {

	private DFUtils() { }
	
	// Writes an Node[] into a DataOutput
	public static void writeArray(DataOutput out, Node[] array) throws IOException {
		out.writeInt(array.length);
		for (Node w : array) {
			w.write(out);
		}
	}
	
	// Reads a Node[] from a DataInput
	public static Node[] readNodeArray(DataInput in) throws IOException {
		int length = in.readInt();
		Node[] nodes = new Node[length];
		for (int index = 0; index < length; index ++) {
			nodes[index] = Node.read(in);
		}
		
		return nodes;
	}
	
	// Writes a double[] into a DataOutput
	public static void writeArray(DataOutput out, double[] array) throws IOException {
		out.writeInt(array.length);
		for (double value : array) {
			out.writeDouble(value);
		}
	}
	
	// Reads a double[] from a DataInput
	public static double[] readDoubleArray(DataInput in) throws IOException {
		int length = in.readInt();
		double[] array = new double[length];
		for (int index = 0; index < length; index ++) {
			array[index] = in.readDouble();
		}
		
		return array;
	}
	
	// Writes an int[] into a DataOutput
	public static void writeArray(DataOutput out, int[] array) throws IOException {
		out.writeInt(array.length);
		for (int value : array) {
			out.writeInt(value);
		}
	}
	
	// Reads an int[] from a DataInput
	public static int[] readIntArray(DataInput in) throws IOException {
		int length = in.readInt();
		int[] array = new int[length];
		for (int index = 0; index < length; index ++) {
			array[index] = in.readInt();
		}
		
		return array;
	}
	
	// Return a list of all files in the output directory
	@SuppressWarnings("deprecation")
	public static Path[] listOutputFiles(FileSystem fs, Path outputPath) throws IOException {
		List<Path> outputFiles = Lists.newArrayList();
		for (FileStatus s : fs.listStatus(outputPath, PathFilters.logsCRCFilter())) {
			if (!s.isDir() && !s.getPath().getName().startsWith("_")) {
				outputFiles.add(s.getPath());
			}
		}
		if (outputFiles.isEmpty()) {
			throw new IOException("No output found !");
		}
		return outputFiles.toArray(new Path[outputFiles.size()]);
		
	}
	
	// Formats a time interval in milliseconds to a String in the form "hours:minutes:seconds:mills"
	public static String elapsedTime(long milli) {
		long seconds = milli / 1000;
		milli %= 1000;
		
		long minutes = seconds / 60;
		seconds %= 60;
		
		long hours = minutes / 60;
		minutes %= 60;
		
		return hours + "h " + minutes + "m " + seconds + "s " + milli;
	}
	
	public static void storeWritable(Configuration conf, Path path, Writable writable) throws IOException {
		FileSystem fs = path.getFileSystem(conf);
		
		FSDataOutputStream out = fs.create(path);
		try {
			writable.write(out);
		} finally {
			Closeables.close(out, false);
		}
	}
	
	// Writes a string to a path
	public static void storeString(Configuration conf, Path path, String string) throws IOException {
		DataOutputStream out = null;
		try {
			out = path.getFileSystem(conf).create(path);
			out.write(string.getBytes(Charset.defaultCharset()));
		} finally {
			Closeables.close(out, false);
		}
	}
}
