package cloudcomputing

/**
  * use map reduce to perform query.
  * the purpose of these examples to show how to use spark map reduce to achieve the similar functions as spark sql
  */
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

object MapReduceExamples {

  def main(args: Array[String]): Unit = {

    // turn chatty log off for local development
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val appName = "DataFrameExamples"

    /////////////////// local debug ///////////////////
    //    val sparkMaster = "local"
    //    val cassandraHost = "cassandra1.zones.eait.uq.edu.au,cassandra2.zones.eait.uq.edu.au,cassandra3.zones.eait.uq.edu.au"
    //    val conf = new SparkConf(true)
    //      .set("spark.cassandra.connection.host", cassandraHost)
    //    val sc = new SparkContext(sparkMaster, appName, conf)
    //    val spark = SparkSession.builder.appName(sc.appName).master(sc.master).config(sc.getConf).getOrCreate

    /////////////////// spark-submit job ///////////////////
    val spark = SparkSession.builder.appName(appName).getOrCreate

    /////////////////// init spark sql ///////////////////
    val keySpace = "cloudcomputing"
    val table = "data"

    val dataframe = spark
      .read
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> keySpace, "table" -> table))
      .load()

    /////////////////// dataframe query ///////////////////
    import spark.implicits._

    // all records in campus "St Lucia"
    val df1 = dataframe.filter($"campus" === "St Lucia").select($"date", $"sensor_id", $"temperature") //one time one partition, more than one partition cause full table scan
    df1.show()

    // average temperature in campus "St Lucia"
    val rdd2 = df1.as[(String,Int,Float)].map{
      case (date, sensor_id, temperature) => (1, temperature)
    }.reduce{
      (a,b) => (a._1+b._1, a._2+b._2)
    }

    println("avg_temp: " + rdd2._2/rdd2._1)

    // average temperature of each day in campus "St Lucia"
    val rdd3 = df1.as[(String,Int,Float)].map{
      case (date, sensor_id, temperature) => (date, (1, temperature))
    }.rdd.reduceByKey{
      (a,b) => (a._1+b._1, a._2+b._2)
    }.map{
      case (date, (count, sum)) => (date, sum/count)
    }

    rdd3.toDF(Array("date", "ave_temp_date"):_*).show

    /////////////////// close spark ///////////////////
    spark.stop()
  }
}