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