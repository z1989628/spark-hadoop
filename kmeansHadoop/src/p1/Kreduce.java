package p1;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class Kreduce extends Reducer<IntWritable,Text,IntWritable,Text> {

	public void reduce(IntWritable key,Iterable<Text> values,Context context)throws IOException,InterruptedException{
		String index=key.toString();
		double[] sum={0,0,0,0,0,0,0,0,0,0};
		String[] strr=null;
		int num=0;	
		StringBuilder sb=new StringBuilder("");
		String point=null;
		for(Text value:values){
			strr=value.toString().split(" ");
			for(int i=0;i<10;i++){
				sum[i]+=Double.parseDouble(strr[i]);
			}
			num++;
		}
		
		for(int j=0;j<9;j++){
			sb.append(sum[j]/num);
			sb.append(" ");
		}
		sb.append(sum[9]/num);
		point=sb.toString();

		context.write(key, new Text(point));
	}

}
