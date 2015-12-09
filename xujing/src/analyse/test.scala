package analyse

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

object test { 
  def average(s:Array[Double]):Double={
    var result:Double=0
    
    val sum=s.reduceLeft(_ + _)
    val count=s.length
    result=sum/count
    
    result    
  }
  
  def change(s: Array[Double]): Int = {
    var num:Int=1
    if(s.length==1){
      num=1
    }else{
      for(i <- 1 until s.length){
        if(s(i-1)!=s(i)){
          num = num+1
        }
      }
    }
    num
  }

  def deviation(s:Array[Double]):Double={
    var result:Double=0
    val ave=average(s)
    var sum:Double=0
    for(i <- 0 until s.length){
      sum +=(s(i)-ave)*(s(i)-ave)
    }
    result=Math.sqrt(sum/s.length)
    result
  }
  def numTtl(s: Array[Double]): Int = {
    var num: Int = 1
    var b=true
    if (s.length == 1) {
      num = 1
    } else {
      for (i <- 0 until s.length-1) {
        val n = s(i+1)
        b=true
        for (j <- 0 until i) {
         if(n==s(j)){
           b=false
         }
        }
        if(b==true){
          num=num+1         
        }
      }
    }
    num
  }
  
  def digit(s:String):Double={
    val len=s.length()
    var digit:Double=0
    var res:Double=0
    for(i <- 0 to len-1){
      if(s(i)>=48&&s(i)<= 57){
        digit=digit+1
      }
    }
    if(digit!=0){
      res=digit/len
    }
    res
  }
  
  def changeDouble(s:String):Double={
    var res:Double=0
    if(s==""){
      res=0
    }else{
      res=s.toDouble
    }
    res
  }
  
  def changeIp(domain:String,s:String):String={
    var res:String="0"
    if(s==""){
      res="-"+domain
    }else{
      res=s
    }
    res
  }
  
  def numIp(s:Array[String]):Int={
     var num: Int = 1
     var b=true
     if (s.length == 1) {
      num = 1
    } else {
      for (i <- 0 until s.length-1) {
        val n = s(i+1)
        b=true
        for (j <- 0 until i) {
         if(n.equals(s(j))){
           b=false
         }
        }
        if(b==true){
          num=num+1          
        }
      }
    }
    num
  }
  
  def ipDomain(s:(String,Array[String])):Array[(String,String)]={
    var ss=new Array[(String,String)](s._2.length)
    for(i <- 0 until s._2.length){
      ss(i)=(s._2(i),s._1)
    }
    ss    
  }
  
  def domainDomains(s:Array[String]):Array[(String,Array[String])]={
    var ss=new Array[(String,Array[String])](s.length)
    for(i <- 0 until s.length){
      ss(i)=(s(i),s)
    }
    ss
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
  
  def findCountry(l:Long,b:RDD[Array[String]]):String={
    var s:String=null
    val bb=b.map { p => (p(0).toLong,p(1).toLong,p(2)) }
    val ss=bb.map{p =>
      if(p._1<=l&&l<=p._2){
        s=p._3
      } 
     }
    s=ss.toString()
    s
  }
  def countries(ips:Array[String],b:RDD[Array[String]]):Int={
    var count:Int=0
    var l:Long=0
    var ss=new Array[String](ips.length)
    if(ips(0)(0)=='-'){
      count=1
    }else{
      for(i <- 0 until ips.length){
      l=ipToLong(ips(i))
      ss(i)=findCountry(l,b)
    }
    count=ss.distinct.length       
    }    
    count
  }
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("analyse").setMaster("local[4]")
    val sc = new SparkContext(conf)
    
    val lines = sc.textFile("dns.txt")
    val ips = sc.textFile("ip.txt")
    val ip1_ip2_country=ips.map(_.split("\\s+"))
    
    val ss = lines.map(_.split("\\|",-1))
    ss.collect().foreach(p=>println(p(0)+" "+p(1)+" "+p(2)+" "+p(3)+" "+p(4)+" "+p(5)+" "+p(6)))
    
    //提取出域名和ttl
    val domain_ttl=ss.map{p => (p(3),p(5))}.reduceByKey((a,b) => a + ","+b).map(p => (p._1,p._2.split(',').map(p =>changeDouble(p))))
    domain_ttl.collect().foreach(p=> println(p._1+" "+p._2.mkString(" ")))   
    //提取出域名和ip
    val domain_ips=ss.map{p => (p(3),p(6))}.reduceByKey((a,b) => a+ ","+b) .map(p =>(p._1,p._2.split(',').map(a =>changeIp(p._1,a))))
    domain_ips.collect().foreach(p=> println(p._1+" "+p._2.mkString(" ")))
    //提取域名和国家数
    val domain_countries=domain_ips.map{p =>(p._1,countries(p._2,ip1_ip2_country))}
    domain_countries.collect().foreach(p => println("提取域名和国家数"+p._1+ " "+p._2))
    
    //域名对应数字比和ttl
    val domain_digit_ttl4=domain_ttl.map{p =>(p._1,(digit(p._1),average(p._2),numTtl(p._2),change(p._2),deviation(p._2)))}
    domain_digit_ttl4.collect().foreach(p => println(p._1+ "  "+p._2._1+" "+p._2._2+" "+p._2._3+" "+p._2._4+" "+p._2._5)) 
    //域名对应ip数
    val domain_ip3=domain_ips.map(p => (p._1, numIp(p._2)))   
    domain_ip3.collect().foreach(p => println(p._1+ " "+p._2))
     
    //域名对应域名数
    val ip_domain=domain_ips.flatMap{p => ipDomain(p)}
    val ipdomains=ip_domain.reduceByKey((a,b) =>a + ","+b).map(p =>(p._2.split(',')))
    val domain_domains=ipdomains.flatMap{p => domainDomains(p)}.reduceByKey((a,b) => a++b)
    val domain_domainCount=domain_domains.map{p => (p._1,p._2.distinct.length)}
    domain_domainCount.collect().foreach(p => println(p._1  + " "+ p._2))
      
    ip_domain.collect().foreach(p => println(p._1  + " "+ p._2))
    
    val domains=domain_ip3.cogroup(domain_digit_ttl4)
    domains.collect().foreach(p=> println(p._1+" "+p._2._1.toList+" "+ p._2._2.mkString(" ")))
   
  }

}