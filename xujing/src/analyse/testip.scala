package analyse

object testip {
  def ipToLong(s:String):Long={
    val ss=s.split('.')
    var l:Long=0
    l+=ss(0).toLong<<24
    l+=ss(1).toLong<<16
    l+=ss(2).toLong<<8
    l+=ss(3).toLong
    l
  }
  def main(args:Array[String]){
    val s="0.0.0.0"
    
    val l=ipToLong(s)
    print(l)
    
  }

}