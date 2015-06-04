package p1;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

public class Kmap extends Mapper<Object,Text,IntWritable,Text>{
	double points[][]=new double[5][10];
	
	protected void setup(Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();

		int i=0;
		String str=null;
		String[] strr=null;

		for(i=0;i<5;i++){
			str=conf.get(i+"");
			if(str.length()>0){
				strr=str.split(" ");
				for(int j=0;j<10;j++){
					points[i][j]=Double.parseDouble(strr[j]);
				}
			}
			
		}
	}
	protected void map(Object key, Text value, Context context) throws IOException,InterruptedException {
		double numbers[]=new double[10];
		String[] s=value.toString().split(" ");
		for(int i=0;i<10;i++){
			numbers[i]=Double.parseDouble(s[i]);
		}
		Dist dist=new Dist();
		int index=dist.closestCenter(numbers, points);
		
		context.write(new IntWritable(index), value);
			
	}
}



