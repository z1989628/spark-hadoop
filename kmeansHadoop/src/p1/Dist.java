package p1;

public class Dist {
	public int closestCenter(double[] num,double[][] centers){
		int index=0;
		double bestDist=squaredDist(num,centers[0]);
		double dist=0;
		
		for(int i=1;i<centers.length;i++){
			dist=squaredDist(num,centers[i]);
			if(dist<bestDist){
				bestDist = dist;
				index=i;
			}
		}
		return index;
	}
	
	public double squaredDist(double[] num1,double[] center){
		double dist=0;
		double sum=0;
		for(int i=0;i<10;i++){
			sum+=(num1[i]-center[i])*(num1[i]-center[i]);
		}
		dist=Math.sqrt(sum);
		return dist;
	}

}
