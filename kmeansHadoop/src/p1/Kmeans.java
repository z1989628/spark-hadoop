package p1;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Kmeans {
	public static void main(String[] args) throws Exception{
		Date d1=new Date();
		Configuration conf=new Configuration();
		String inPath=null;
		String outPath=null;
		String point=null;
		if(args.length>1){
			inPath=args[0];
			outPath=args[1];
		}
			    
		conf.set("0", "8485 9952 7672 8198 5087 9399 1783 2458 6149 1442");
		conf.set("1","5205 8041 1570 410 1663 9088 7808 8569 2215 9626");
		conf.set("2","3848 8999 9759 2542 984 6989 5838 2210 1071 7514");
		conf.set("3","4061 9086 456 6355 3473 3724 6640 7733 6229 7819");
		conf.set("4","469 2167 8739 8050 7870 1787 847 4465 1585 1155");
		
		int iteration=0;		

		Job job;
		
		while (iteration < 10) {		    	     
		     
		     if(iteration>0){
		    	  InputStream in=null;
		          FileSystem fs = FileSystem.get(conf);
		          BufferedReader buff =null;
		          Path path = new Path(args[1]+"/"+(iteration-1)+"/part-r-00000");
		          in=fs.open(path);
		          buff=new BufferedReader(new InputStreamReader(in));
		          String s =null;
		          String[] ss=null;
		          while((s=buff.readLine())!=null){
		        	  ss=s.split("	");
		        	  conf.set(ss[0],ss[1]);	        	  
		          }
		          System.out.println("第"+iteration+"次迭代后中心点为:");
				  for(int i=0;i<5;i++){
				        point=conf.get(i+"");
				    	if(point.length()>0){
					         System.out.println(point);
					    	 System.out.println("***************************************************");
				    	}
				  }
			  }
		     job = Job.getInstance(conf, "Kmeans"+"_"+iteration);
		     job.setJarByClass(Kmeans.class);
		     
		     job.setMapOutputKeyClass(IntWritable.class);
		     job.setMapOutputValueClass(Text.class);
		     job.setOutputKeyClass(IntWritable.class);
		     job.setOutputValueClass(Text.class);
		      
		     job.setMapperClass(Kmap.class);
		     job.setReducerClass(Kreduce.class);
		      
		     FileInputFormat.addInputPath(job, new Path(inPath));
		     Path clustersOut = new Path(outPath,iteration+"");
		     FileOutputFormat.setOutputPath(job, clustersOut);
		     iteration++;
		     if (!job.waitForCompletion(true)) {
		         throw new InterruptedException("迭代失败:" + iteration );
		     }		    	 		   
		     
		     Date d2=new Date();
		     long diff =d2.getTime()-d1.getTime();
		     long time = diff / (1000*60);
		     long time1=(diff/1000)%60;
		     System.out.println("到第"+iteration+"次迭代经过的时间为："+time+"分"+time1+"秒");
	   }
		
		InputStream in=null;
        FileSystem fs = FileSystem.get(conf);
        BufferedReader buff =null;
        Path path = new Path(args[1]+"/"+(iteration-1)+"/part-r-00000");
        in=fs.open(path);
        buff=new BufferedReader(new InputStreamReader(in));
        String s =null;
        String[] ss=null;
        while((s=buff.readLine())!=null){
      	  ss=s.split("	");
      	  conf.set(ss[0],ss[1]);	        	  
        }
        System.out.println("第"+iteration+"次迭代后中心点为:");
		  for(int i=0;i<5;i++){
		        point=conf.get(i+"");
		    	if(point.length()>0){
			         System.out.println(point);
			    	 System.out.println("***************************************************");
		    	}
		  }
		  
		System.exit(0);
    }
}
