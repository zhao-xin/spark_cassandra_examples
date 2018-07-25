package cloudcomputing

/**
  * use map reduce to perform query.
  * the purpose of these examples to show how to use spark map reduce to achieve the similar functions as spark sql
  */
import org.apache.spark._
import com.datastax.spark.connector._
import org.apache.log4j.{Level, Logger}


object MapReduceExamples {

  def main(args: Array[String]): Unit = {

    // turn off chatty off for local development. DO NOT do this in production
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    /////////////////// configurations ///////////////////
    val sparkMaster = "local[4]"  //can override spark master address when submitting spark jobs

    // cassandra address and credentials should be managed by tools like k8s, here we use hardcode for simplicity.
    val cassandraHost = "192.168.128.81,192.168.128.82,192.168.128.83" //change cassandra addresses to yours
//    val cassandraPort = "9042" // default cassandra port can be skipped
//    val cassandraAuthUsername = "cassandra" //anonymously login can be skipped
//    val cassandraAuthPassword = "cassandra"

    /////////////////// init spark ///////////////////
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", cassandraHost)
//      .set("spark.cassandra.connection.port", cassandraPort)
//      .set("spark.cassandra.auth.username", cassandraAuthUsername)
//      .set("spark.cassandra.auth.password", cassandraAuthPassword)

    val appName = "DataFrameExamples"
    val sc = new SparkContext(sparkMaster, appName, conf)

    /////////////////// init spark cassandra connector ///////////////////
    val keySpace = "cloudcomputing"
    val table = "data"

    val cassTable = sc.cassandraTable[(String, Int, Float)](keySpace, table).select("date", "sensor_id", "temperature").where("campus = ?", "St Lucia" );

    ///////////////////// map reduce ///////////////////

    // all records in campus "St Lucia"
    val rdd1 = cassTable.collect

    rdd1.foreach(println)
    println()

    // average temperature in campus "St Lucia"
    val rdd2 = cassTable.map{
      case (date, sensor_id, temperature) => (1, temperature)
    }.reduce((a,b) => (a._1+b._1, a._2+b._2))

    println("avg_temp: " + rdd2._2/rdd2._1)
    println()

    // average temperature of each day in campus "St Lucia"
    val rdd3 = cassTable.map{
      case (date, sensor_id, temperature) => (date, (1, temperature))
    }.reduceByKey((a,b) => (a._1+b._1, a._2+b._2))
      .map{case (date, (count, sum)) => (date, sum/count)}

    rdd3.foreach(println)

    /////////////////// close spark ///////////////////
    sc.stop()
  }
}