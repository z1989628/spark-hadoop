package com.java.zcl;

public class TrainPoint {
	public int y;
	public double x[] = new double[Constants.DIMENSION];
	
	TrainPoint(double x[],int y){
		this.x=x;
		this.y=y;
	}
	TrainPoint(String line){
		String str[]=line.split(" ");
		y = Integer.parseInt(str[0]);
		for(int i =0;i<Constants.DIMENSION;i++){
			x[i] = Double.parseDouble(str[i+1]);
		}
	}
	

}
