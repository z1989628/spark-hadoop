package p1


import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._

object Kmeans{
  
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

  def closestCenter(p: Array[Double], centers: Array[ Array[Double]]): Int = {
    var bestIndex = 0
    var bestDist = squaredDist(p,centers(0))

    for (i <- 1 until centers.length) {
      val dist = squaredDist(p,centers(i))
      if (dist < bestDist) {
        bestDist = dist
        bestIndex = i
      }
    }
    return bestIndex
  }
  
  def squaredDist(p1: Array[Double], p2: Array[Double]):Double = {
    var dist=0.0
    var sum=0.0
    for(i <- 0 to 9){
      sum+=(p1(i)-p2(i))*(p1(i)-p2(i))
    }
    dist=Math.sqrt(sum)
    dist   
  }
  def main(args: Array[String]) {
    val time1 = System.currentTimeMillis()

    //val conf = new SparkConf().setAppName("k-means").setMaster("local[4]")
    val conf = new SparkConf().setAppName("k-means").setMaster("spark://192.168.1.2:7077")
    val sc = new SparkContext(conf)
    val lines = sc.textFile("/home/spark/xujing/snumbers.txt")
    val points = lines.map(toArrayDouble(_)).cache()
    val dimensions = 10
    val k = 5
    val iterations = 10
    
    

    var centers = Array.ofDim[Double](k,dimensions)
    
    centers(0) = Array(8485,9952,7672,8198,5087,9399,1783,2458,6149,1442)
    centers(1) = Array(5205,8041,1570,410,1663,9088,7808,8569,2215,9626)
    centers(2) = Array(3848,8999,9759,2542,984,6989,5838,2210,1071,7514)
    centers(3) = Array(4061,9086,456,6355,3473,3724,6640,7733,6229,7819)
    centers(4) = Array(469,2167,8739,8050,7870,1787,847,4465,1585,1155)
    
    for(i <- 0 to 4){
      println("初始中心点为：" + centers(i).mkString("<",",",">"))
    }
    
    for (i <- 1 to iterations) {
      println("第" + i+"次迭代开始")
 
      val mappedPoints = points.map { p => (closestCenter(p, centers), (p, 1)) }
 
      val newCenters = mappedPoints.reduceByKey {
        case ((sum1, count1), (sum2, count2)) => (addTwoArray(sum1,sum2), count1 + count2) 
      }.map { 
        case (id, (sum, count)) => (id, divisionArray(sum , count))
      }.collect
 
      // 更新中心节点
      for ((id, value) <- newCenters) {
        centers(id) = value
      }
      
      val time2 = System.currentTimeMillis()
      
      println("第"+i+"次迭代后中心点为：")
      for(i <- 0 to 4){
      println(centers(i).mkString("<",",",">"))
      }
      println("经过时间为："+(time2-time1)/(1000)+"秒")
    }
  }
}