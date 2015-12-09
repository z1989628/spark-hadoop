package analyse

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

object Sample1 { 
  
  def changeIp1(domain:String,s:String):String={
    var res:String="0"
    if(s==""){
      res="0.0.0.0"
    }else{
      res=s
    }
    res
  }
  
  def ipToLong(s:String):Long={
    val ss=s.split('.')
    var l:Long=0
    l+=ss(0).toLong<<24
    l+=ss(1).toLong<<16
    l+=ss(2).toLong<<8
    l+=ss(3).toLong
    l
  }
  
  def findCountry(l:Long,localdata: Array[(Long, Long, String)]):String={
    var s:String=null
    localdata.map{p =>
      if(p._1<=l&&l<=p._2){
        s=p._3
      } 
     }
    s
  }
  def countries(ips: Array[String], localdata: Array[(Long, Long, String)]): Int = {
    var count: Int = 0
    var l: Long = 0
    var ss = new Array[String](ips.length)

    for (i <- 0 until ips.length) {
      l = ipToLong(ips(i))
      ss(i) = findCountry(l, localdata)
    }
    count = ss.distinct.length
    count
  }

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("analyse").setMaster("local[4]")
    val sc = new SparkContext(conf)
    
    val lines = sc.textFile("dns.txt")
    val ips = sc.textFile("ip.txt")
    val ip1_ip2_country=ips.map(_.split("\\s+"))
    ip1_ip2_country.collect().foreach(p => println(p.mkString("|")))
    
    val localdata=ip1_ip2_country.map{p => (p(0).toLong,p(1).toLong,p(2))}.collect()
    
    val ss = lines.map(_.split("\\|",-1))
    ss.collect().foreach(p=>println(p(0)+" "+p(1)+" "+p(2)+" "+p(3)+" "+p(4)+" "+p(5)+" "+p(6)))
    
    //提取出域名和ip
    val domain_ips1=ss.map{p => (p(3),p(6))}.reduceByKey((a,b) => a+ ","+b) .map(p =>(p._1,p._2.split(',').map(a =>changeIp1(p._1,a))))
    domain_ips1.collect().foreach(p=> println(p._1+" "+p._2.mkString(" ")))
    //提取域名和国家数
    val domain_countries=domain_ips1.map{p =>(p._1,countries(p._2,localdata))}
    domain_countries.collect().foreach(p => println("提取域名和国家数"+p._1+ " "+p._2))
  }
}