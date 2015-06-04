package com.java.zcl;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class KnnByHadoop {
	
	public static class MyMap extends Mapper<Object,Text,Text,Text>{
		private Path[] filepath;
		private Path[] localArchives;
		private URI[] uris;
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			// TODO Auto-generated method stub
			//super.setup(context);
			Configuration conf = context.getConfiguration();
			filepath = DistributedCache.getLocalCacheFiles(conf);
			localArchives=DistributedCache.getLocalCacheArchives(conf);
			uris = DistributedCache.getCacheFiles(conf);
		}

		public void map(Object key,Text value,Context context) throws IOException,InterruptedException{
			String line = value.toString().trim();
			TestPoint point = null;
			if(null!=line){
				point = new TestPoint(line);
			}
		  Text point_text = new Text(line);
		  FileReader reader = null;
        BufferedReader buff =null;
        if(filepath!=null){
			reader = new FileReader(filepath[0].toString());
	        buff=new BufferedReader(reader);
	        String ss =null;
	        TrainPoint trainpoint;
	        double distance=0;
	        while((ss=buff.readLine())!=null){
	        	 trainpoint = new TrainPoint(ss);
	        	 distance = KnnMath.CalcEuclideanDistance(point, trainpoint);
	        	 context.write(point_text, new Text(distance+" "+trainpoint.y));
			  }
        	}
		}

	}
	
	public static class MyReduce extends Reducer<Text,Text,Text,IntWritable>{
		
		//private Text word_number =0;
		public void reduce(Text key,Iterable<Text> values,Context context) 
		throws IOException,InterruptedException{
			ArrayList<Double> list = new ArrayList<Double>();
			ArrayList<Integer> list_type = new ArrayList<Integer>();
			String[] line=null;
			for(Text val:values){
				line = val.toString().split(" ");
				list.add(Double.parseDouble(line[0]));
				list_type.add(Integer.parseInt(line[1]));
			}
			int type = 0;
			type=KnnMath.FindKMax(Constants.K, list, list_type);
			context.write(key,new IntWritable(type));
		}
	}
	public static class MyCombine extends Reducer<Text,Text,Text,Text>{
		public void reduce(Text key,Iterable<Text> values,Context context) 
		throws IOException,InterruptedException{
			ArrayList<Double> list = new ArrayList<Double>();
			ArrayList<Integer> list_type = new ArrayList<Integer>();
			String[] line=null;
			for(Text val:values){
				line = val.toString().split(" ");
				list.add(Double.parseDouble(line[0]));
				list_type.add(Integer.parseInt(line[1]));
			}
			double[] distinct = new double[Constants.K];
			int[] type = new int[Constants.K];
			KnnMath.FindKMax(Constants.K, list, list_type,distinct,type);
			for(int i=0;i<Constants.K;i++){
				context.write(key,new Text(distinct[i]+" "+type[i]));
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
			Configuration conf = new Configuration();
			//DistributedCache.createSymlink(conf);
			DistributedCache.addCacheFile(new Path("/chenglongz/KNN/train_big.txt").toUri(),conf);
			
		  Job job = Job.getInstance(conf, "KnnByHadoop");
        job.setJarByClass(KnnByHadoop.class);
        job.setMapperClass(MyMap.class);
        job.setCombinerClass(MyCombine.class);
        job.setReducerClass(MyReduce.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
	
	

}

