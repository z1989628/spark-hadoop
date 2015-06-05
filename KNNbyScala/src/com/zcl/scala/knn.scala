package com.zcl.scala

/**
 * @author lenovo
 */
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.apache.spark.sql.columnar.INT

object knn{
  
  def toArrayDouble(s:String):Array[Double]={
      val array1=s.split(" ")
      val array2=array1.map(_.toDouble)
      array2
  }
  
  def addTwoArray(p1: Array[Double], p2: Array[Double]):Array[Double] = {
      var result=new Array[Double](10)

      for(i <- 0 to 9){
         result(i)=p1(i)+p2(i)
    }
      result
  }
  
  def divisionArray(p: Array[Double],num:Int):Array[Double]={
      var result=p.map(_/num)    
      result
  }

  /**
   * p1:test_point
   * p2:train_point
   * return :(double,int)（距离，类编号）
   */
  def squaredDist(p1: Array[Double], p2: Array[Double]):(Double,Int) = {
    var dist=0.0
    var sum=0.0
    for(i <- 0 to 1){
      sum+=(p1(i)-p2(i+1))*(p1(i)-p2(i+1))
    }
    dist=Math.sqrt(sum)
    (dist,p2(p2.length-1).toInt)   
  }
  
  def main(args: Array[String]) {
    val time1 = System.currentTimeMillis()

    //val conf = new SparkConf().setAppName("k-means").setMaster("local[4]")
    val conf = new SparkConf().setAppName("k-means").setMaster("spark://192.168.1.2:7077")
    val sc = new SparkContext(conf)
    //train path
    val train_lines = sc.textFile("/chenglongz/KNN/traindata.txt")
    //test path
    val test_lines = sc.textFile("/chenglongz/KNN/input/test.txt")
    //read train points
    val train_points = train_lines.map(toArrayDouble(_)).cache()
    //read test points
    val test_points = test_lines.map(toArrayDouble(_))
   
    for(test_point <- test_points){
      val distincts = train_points.map { x =>squaredDist(test_point,x) }
    
      val result = distincts.map(x =>(x._1,x._2)).sortByKey(false)
      var sum =0
      for(i <- 0 to 4){
         sum += 1
      }
      println(test_point+" "+sum)
    }
   
    
    val time2 = System.currentTimeMillis()
    println("经过时间为："+(time2-time1)/(1000)+"秒")
    
  }
}