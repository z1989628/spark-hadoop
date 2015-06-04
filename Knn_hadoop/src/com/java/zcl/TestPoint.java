package com.java.zcl;

public class TestPoint {
	//public int y;
	public double x[] = new double[Constants.DIMENSION];
	
	TestPoint(double x[]){
		this.x=x;
		//this.y=y;
	}
	TestPoint(String line){
		String str[]=line.split(" ");
		//y = Integer.parseInt(str[0]);
		for(int i =0;i<Constants.DIMENSION;i++){
			x[i] = Double.parseDouble(str[i]);
		}
	}
}
