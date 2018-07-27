package cloudcomputing

/**
  * use dataframe to perform query
  * compared with sql, dataframe query has stronger compiler error check
  * dataframe is the most practical method to perform query
  */
import org.apache.spark._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.log4j.Logger
import org.apache.log4j.Level

object DataFrameExamples {

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
    val df1 = dataframe.filter($"campus" === "St Lucia") //one time one partition, more than one partition cause full table scan
    df1.show()

    // average temperature in campus "St Lucia"
    val df2 = dataframe.filter($"campus" === "St Lucia").groupBy($"campus").agg(avg($"temperature").as("avg_temp"))
    df2.show()

    // average temperature of each day in campus "St Lucia"
    val df3 = dataframe.filter($"campus" === "St Lucia").groupBy($"date").agg(avg($"temperature").as("avg_temp_day"))
    df3.show()

    /////////////////// close spark ///////////////////
    spark.stop()
  }
}