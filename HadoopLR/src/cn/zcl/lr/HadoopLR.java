package cn.zcl.lr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class HadoopLR {
	//�̳�Mapper�ӿڣ�����map����������Ϊ<Object,Text>
	//�������Ϊ<Text,LongWritable>
	public static double[] W = {0,0,0,0,0,0,0,0,0,0};
	public static class MyMap extends Mapper<Object,Text,IntWritable,DoubleWritable>{
		//��¼����ʱ��
		//private IntWritable iterator =new IntWritable(PARAMENT.ITERATOR_NUM); 
		//��дmap����
		public void map(Object key,Text value,Context context) throws IOException,InterruptedException{
			String line = value.toString().trim();
			//��õ�
			if(null!=line){
				ParsePoint parsepoint =new ParsePoint();
				DataPoint point = parsepoint.call(line);
				//�õ�ÿ������w�Ĺ�ϵ
				double[] gredient = new ComputeGradient(W).call(point);
				//DoubleWritable[] D_gredient=new DoubleWritable[gredient.length];
				for(int i=0;i<gredient.length;i++){
					context.write(new IntWritable(i+1),new DoubleWritable(gredient[i]));
				}
			}
		}

		@Override
		protected void setup(org.apache.hadoop.mapreduce.Mapper.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			Configuration conf = context.getConfiguration();
			String w_value = conf.get("W");
			int i=0;
			for(String e : w_value.trim().split(" ")){
				W[i++]=Double.parseDouble(e);
			}
		}
	}
	
	//�̳�reduce�����࣬����reduce����������Ϊ<Text,long>
	//�������Ϊ<Text,long>
	
	public static class MyReduce extends Reducer<IntWritable,DoubleWritable,IntWritable,DoubleWritable>{
		
		private IntWritable ITERATOR = new IntWritable(PARAMENT.ITERATOR_NUM);
		//private Text word_number =0;
		public void reduce(IntWritable key,Iterable<DoubleWritable> values,Context context) 
		throws IOException,InterruptedException{
			double gredient=0;
			for(DoubleWritable val:values){
				gredient += val.get();
			}
			W[key.get()-1] -= gredient;
			//for test 
			System.out.println("w["+(key.get()-1)+"] is "+gredient);
			//HadoopLR.count++;
			//�ռ����
			context.write(key,new DoubleWritable(W[key.get()-1]));
		}
		@Override
		protected void setup(org.apache.hadoop.mapreduce.Reducer.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			Configuration conf = context.getConfiguration();
			String w_value = conf.get("W");
			int i=0;
			for(String e : w_value.trim().split(" ")){
				W[i++]=Double.parseDouble(e);
			}
		}
	}
	public static class MyCombine extends Reducer<IntWritable,DoubleWritable,IntWritable,DoubleWritable>{
		public void reduce(IntWritable key,Iterable<DoubleWritable> values,Context context) 
		throws IOException,InterruptedException{
			double gredient=0;
			for(DoubleWritable val:values){
				gredient += val.get();
			}
			//�ռ����
			context.write(key,new DoubleWritable(gredient));
		}
	}
	
	public static void main(String[] args) throws Exception{
		//mapreduceµÄµü´úÄ£ÐÍ
		Path input = new Path(args[0]);
		Path output = new Path(args[1]+"_0");
		//int ITERATOR= Integer.getInteger(args[2]);
		int ITERATOR= 10;
		Configuration conf;
		Job job;
		conf = new Configuration();
	    String name ="W";
		for(int i =0;i<ITERATOR;i++){
			String value="";
			System.out.println("job: "+i+" start");
			if(0==i){
				value = "0.4551273600657362 "+"0.36644694351969087 "+"-0.38256108933468047 "+"-0.4458430198517267 "+"0.33109790358914726 "+"0.8067445293443565 "+"-0.2624341731773887 "+"-0.44850386111659524 "+"-0.07269284838169332 "+"0.5658035575800715";
			}
			else{
				//read file from HDFS
		          InputStream in=null;
		          FileSystem hdfs = FileSystem.get(conf);
		          BufferedReader buff =null;
		          Path path = new Path(args[1]+"_"+(i-1)+"/part-r-00000");
		          in=hdfs.open(path);
		          buff=new BufferedReader(new InputStreamReader(in));
		          String ss =null;
		          while((ss=buff.readLine())!=null){
		        	  int j=0;
		        	  while(ss.trim().charAt(j)>='0'&&ss.trim().charAt(j)<='9'){
		        		  j++;
		        	  }
		        	  while(ss.trim().charAt(j)<'0'||ss.trim().charAt(j)>'9'){
		        		  if(ss.trim().charAt(j)=='-')
		        			  break;
		        		  j++;
		        	  }
		        	  System.out.println("*************"+ss.substring(j));
		        	  value=value+ss.substring(j)+" ";
		          }
			}
			System.out.println("@@@@@@@@@@@@@@@@"+value);
			conf.set(name, value);
			job = Job.getInstance(conf, "HadoopLR"+"_"+i);
	        job.setJarByClass(HadoopLR.class);
	        job.setMapperClass(MyMap.class);
	        job.setCombinerClass(MyCombine.class);
	        job.setReducerClass(MyReduce.class);
	        job.setMapOutputKeyClass(IntWritable.class);
	        job.setMapOutputValueClass(DoubleWritable.class);
	        job.setOutputValueClass(DoubleWritable.class);
	        job.setOutputKeyClass(IntWritable.class);
	        FileInputFormat.addInputPath(job, input);
	        FileOutputFormat.setOutputPath(job, output);
	        //System.exit(job.waitForCompletion(true) ? 0 : 1);
	        output = new Path(args[1]+"_"+(i+1));
	        if(!(job.waitForCompletion(true))){
	        	System.out.println("µü´ú"+i+"³ö´í");
	        	System.exit(1);
	        }
	        System.out.println(i+" finished");
	        //for test
		}
		System.exit(0);
        
 
	}
	
	

}
