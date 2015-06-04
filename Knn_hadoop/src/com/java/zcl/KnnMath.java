package com.java.zcl;

import java.util.List;

public class KnnMath {
	//»ñµÃÅ·ÊÏ¾àÀë
	public static double CalcEuclideanDistance(TestPoint a,TrainPoint b){
		double distance = 0.0;
		for(int i=0;i<Constants.DIMENSION;i++){
			distance +=(a.x[i]-b.x[i])*(a.x[i]-b.x[i]);
		}
		return Math.sqrt(distance);
	}
	//Ñ¡È¡×î´ókÖµ
	public static int FindKMax(int k,List<Double> list,List<Integer>list_type){
		if(k<1){
			System.out.println("k can not below 1");
			return -1;
		}
		double[] array = new double[k];
		int[] type = new int[k];
		for(int i=0;i<k;i++){
			array[i]=list.get(i);
			type[i]=list_type.get(i);
		}
		//½«Êý×éÅÅºÃÐò£¬´ÓÐ¡µ½´ó¡£
		ChooseSort(array,type,k);
		//²åÈëÊý¾Ý£¬ÓëÊý×éµÄ×îÐ¡ÔªËØ±È½Ï£¬Èç¹ûÐ¡ÓÚ×îÐ¡ÔªËØ£¬ÉáÆú¡£·ñÔò½øÐÐ²åÈëÅÅÐò¡£
		for(int i=k;i<list.size();++i){
			if(array[0]<list.get(i)){
				int j=1;
				for(;j<k;j++){
					if(array[j]>list.get(i))
						break;
				}
				//²åÈëÅÅÐò
				for(int m=0;m<j-1;m++){
					array[m]=array[m+1];
					type[m] = type[m+1];
				}
				array[j-1]=list.get(i);
				type[j-1]=list_type.get(i);
						
			}
		}
		for(int i=0;i<k;i++){
			System.out.print("  "+array[i]);
		}
		System.out.println();
		int sum =0;
		for(int i=0;i<k;i++){
			System.out.print("  "+type[i]);
			sum += type[i];
		}
		System.out.println();
		if(sum>k/2){
			return 1;
		}
		else{
			return 0;
		}
		
	}
	//ÅÅÐò´ÓÐ¡µ½´ó
	public static void ChooseSort(double[] array,int[] type,int k){
		for(int i=0;i<k-1;i++){
			double min=array[i];
			int min_next=i;
			for(int j=i+1;j<k;j++){
				if(array[j]<min){
					min=array[j];
					min_next=j;
				}
			}
			min=array[i];
			array[i]=array[min_next];
			array[min_next]=min;
			//±£³ÖÀàÐÍÒ»ÖÂ
			int temp=type[i];
			type[i]=type[min_next];
			type[min_next]=temp;
		}
	}
	
	//Ñ¡È¡×î´ókÖµ
		public static void FindKMax(int k,List<Double> list,List<Integer>list_type,double[] distinct,int[] type_point){
			if(k<1){
				System.out.println("k can not below 1");
				return ;
			}
			double[] array = new double[k];
			int[] type = new int[k];
			for(int i=0;i<k;i++){
				array[i]=list.get(i);
				type[i]=list_type.get(i);
			}
			//½«Êý×éÅÅºÃÐò£¬´ÓÐ¡µ½´ó¡£
			ChooseSort(array,type,k);
			//²åÈëÊý¾Ý£¬ÓëÊý×éµÄ×îÐ¡ÔªËØ±È½Ï£¬Èç¹ûÐ¡ÓÚ×îÐ¡ÔªËØ£¬ÉáÆú¡£·ñÔò½øÐÐ²åÈëÅÅÐò¡£
			for(int i=k;i<list.size();++i){
				if(array[0]<list.get(i)){
					int j=1;
					for(;j<k;j++){
						if(array[j]>list.get(i))
							break;
					}
					//²åÈëÅÅÐò
					for(int m=0;m<j-1;m++){
						array[m]=array[m+1];
						type[m] = type[m+1];
					}
					array[j-1]=list.get(i);
					type[j-1]=list_type.get(i);
							
				}
			}
			for(int i=0;i<k;i++){
				System.out.print("  "+array[i]);
			}
			System.out.println();
			int sum =0;
			for(int i=0;i<k;i++){
				System.out.print("  "+type[i]);
				sum += type[i];
			}
			System.out.println();
			for(int i=0;i<distinct.length;i++){
				distinct[i]=array[i];
				type_point[i]=type[i];
			}
			
		}
}
