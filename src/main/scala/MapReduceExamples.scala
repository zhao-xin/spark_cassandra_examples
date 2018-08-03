package cloudcomputing

/**
  * use map reduce to perform query.
  * the purpose of these examples to show how to use spark map reduce to achieve the similar functions as spark sql
  */
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.spark.sql.SparkSession

object MapReduceExamples {

  def main(args: Array[String]): Unit = {

    // turn off chatty off for local development. DO NOT do this in production
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val appName = "DataFrameExamples"

    /////////////////// local debug ///////////////////
    val sparkMaster = "local[4]"

    val cassandraHost = "192.168.128.81,192.168.128.82,192.168.128.83" //change cassandra addresses to yours
    //    val cassandraPort = "9042" // default cassandra port can be skipped
    //    val cassandraAuthUsername = "cassandra" //anonymously login can be skipped
    //    val cassandraAuthPassword = "cassandra"

    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", cassandraHost)
    //      .set("spark.cassandra.connection.port", cassandraPort)
    //      .set("spark.cassandra.auth.username", cassandraAuthUsername)
    //      .set("spark.cassandra.auth.password", cassandraAuthPassword)

    val sc = new SparkContext(sparkMaster, appName, conf)
    val spark = SparkSession.builder.appName(sc.appName).master(sc.master).config(sc.getConf).getOrCreate

    /////////////////// init spark ///////////////////
    //    val spark = SparkSession.builder.appName(appName).getOrCreate

    val keySpace = "cloudcomputing"
    val table = "data"

    val dataframe = spark
      .read
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> keySpace, "table" -> table))
      .load()

    dataframe.createOrReplaceTempView(table)

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