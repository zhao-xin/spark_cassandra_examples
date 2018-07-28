package cloudcomputing

/**
  * use map reduce to perform query.
  * the purpose of these examples to show how to use spark map reduce to achieve the similar functions as spark sql
  */
import com.datastax.spark.connector._
import org.apache.spark._
import org.apache.log4j.Logger
import org.apache.log4j.Level

object MapReduceExamples {

  def main(args: Array[String]): Unit = {

    // turn off chatty off for local development. DO NOT do this in production
//    Logger.getLogger("org").setLevel(Level.OFF)
//    Logger.getLogger("akka").setLevel(Level.OFF)

    val appName = "MapReduceExamples"

    /////////////////// local debug //////////////////
//    val sparkMaster = "local"/
//    val cassandraHost = "cassandra1.zones.eait.uq.edu.au,cassandra2.zones.eait.uq.edu.au,cassandra3.zones.eait.uq.edu.au"
//    val conf = new SparkConf(true)
//      .set("spark.cassandra.connection.host", cassandraHost)
//    val sc = new SparkContext(sparkMaster, appName, conf)

    /////////////////// spark-submit job ///////////////////
    val conf = new SparkConf(true).setAppName(appName)
    /////////////////// init spark cassandra connector ///////////////////
    val keySpace = "cloudcomputing"
    val sc = new SparkContext(conf)

    val table = "data"

    val cassTable = sc.cassandraTable[(String, Int, Float)](keySpace, table).select("date", "sensor_id", "temperature").where("campus = ?", "St Lucia" );

    ///////////////////// map reduce ///////////////////

    // average temperature of each day in campus "St Lucia"
    val rdd3 = cassTable.map{
      case (date, sensor_id, temperature) => (date, (1, temperature))
    }.reduceByKey((a,b) => (a._1+b._1, a._2+b._2))
      .map{case (date, (count, sum)) => (date, sum/count)}

    // all records in campus "St Lucia"
    val rdd1 = cassTable.collect
    println()
    rdd1.foreach(println)
    println()

    // average temperature in campus "St Lucia"
    val rdd2 = cassTable.map{
      case (date, sensor_id, temperature) => (1, temperature)
    }.reduce((a,b) => (a._1+b._1, a._2+b._2))

    println("avg_temp: " + rdd2._2/rdd2._1)
    println()

    rdd3.foreach(println)
    println()

    /////////////////// close spark ///////////////////
    sc.stop()
  }
}