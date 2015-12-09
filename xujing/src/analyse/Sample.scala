package analyse

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._

object Sample { 
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
    .set("spark.executor.memory","4g")
   // val conf = new SparkConf().setAppName("analyse").setMaster("spark://192.168.1.2:7077")
    //.set("spark.executor.memory","4g")
    
    val sc = new SparkContext(conf)
    
    val lines = sc.textFile("dns.txt")
    val ips = sc.textFile("ip.txt")
    //val lines = sc.textFile("/home/spark/xujing/dns.txt");
   // val ips =sc.textFile("/home/spark/xujing/ip.txt")
    
    val ip1_ip2_country=ips.map(_.split("\\s+"))
    val localdata=ip1_ip2_country.map{p => (p(0).toLong,p(1).toLong,p(2))}.collect()
    
    val ss = lines.map(_.split("\\|",-1))
    ss.collect().foreach(p=>println(p(0)+" "+p(1)+" "+p(2)+" "+p(3)+" "+p(4)+" "+p(5)+" "+p(6)))
    
    //提取出域名和ttl
    val domain_ttl=ss.map{p => (p(3),p(5))}.reduceByKey((a,b) => a + ","+b).map(p => (p._1,p._2.split(',').map(p =>changeDouble(p))))
    domain_ttl.collect().foreach(p=> println(p._1+" "+p._2.mkString(" ")))   
    //提取出域名和ip
    val domain_ips=ss.map{p => (p(3),p(6))}.reduceByKey((a,b) => a+ ","+b) .map(p =>(p._1,p._2.split(',').map(a =>changeIp(p._1,a))))
    domain_ips.collect().foreach(p=> println(p._1+" "+p._2.mkString(" ")))
    //提取域名和国家数

        //提取出域名和ip
    val domain_ips1=ss.map{p => (p(3),p(6))}.reduceByKey((a,b) => a+ ","+b) .map(p =>(p._1,p._2.split(',').map(a =>changeIp1(p._1,a))))
    domain_ips1.collect().foreach(p=> println(p._1+" "+p._2.mkString(" ")))
    //提取域名和国家数
    val domain_countries=domain_ips1.map{p =>(p._1,countries(p._2,localdata))}
    domain_countries.collect().foreach(p => println("提取域名和国家数"+p._1+ " "+p._2))
    
    //域名对应数字比和ttl4,均值，个数，变化数，标准差
    val domain_digit_ttl4=domain_ttl.map{p =>(p._1,(digit(p._1),average(p._2),numTtl(p._2),change(p._2),deviation(p._2)))}
    domain_digit_ttl4.collect().foreach(p => println(p._1+ "  "+p._2._1+" "+p._2._2+" "+p._2._3+" "+p._2._4+" "+p._2._5)) 
    //域名对应ip数
    val domain_ipnum=domain_ips.map(p => (p._1, numIp(p._2)))   
    domain_ipnum.collect().foreach(p => println(p._1+ " "+p._2))
     
    //域名对应域名数
    val ip_domain=domain_ips.flatMap{p => ipDomain(p)}
    val ipdomains=ip_domain.reduceByKey((a,b) =>a + ","+b).map(p =>(p._2.split(',')))
    val domain_domains=ipdomains.flatMap{p => domainDomains(p)}.reduceByKey((a,b) => a++b)
    val domain_domainCount=domain_domains.map{p => (p._1,p._2.distinct.length)}
    domain_domainCount.collect().foreach(p => println(p._1  + " "+ p._2))
    
    //域名对应域名数和ip数
    val domain_ip2=domain_domainCount.cogroup(domain_ipnum).map{p =>
      val domainCount=p._2._1.toArray
      val ipnum=p._2._2.toArray
      (p._1,(domainCount(0),ipnum(0)))
    }
      
    ip_domain.collect().foreach(p => println(p._1  + " "+ p._2))
    //(域名,(域名数,ip数,数字比,均值，个数，变化数，标准差))
    val domains=domain_ip2.cogroup(domain_digit_ttl4)
    val domains7=domains.map{p =>
      val array1=p._2._1.toArray
      val array2=p._2._2.toArray
    
      (p._1,(array1(0)._1,array1(0)._2,array2(0)._1,array2(0)._2,array2(0)._3,array2(0)._4,array2(0)._5)) 
    }
    
    val domains8=domains7.cogroup(domain_countries).map{p =>
      val array1=p._2._1.toArray
      val array2=p._2._2.toArray
    
      (p._1,(array1(0)._1,array1(0)._2,array1(0)._3,array1(0)._4,array1(0)._5,array1(0)._6,array1(0)._7,array2(0))) 
    }
    domains8.collect().take(10).foreach(p=> println(p._1+" "+p._2._1+" "+ p._2._2+" "+ p._2._3+" "+ p._2._4+" "+ p._2._5+" "+ p._2._6+" "+ p._2._7+" "+ p._2._8))
    
    }

}