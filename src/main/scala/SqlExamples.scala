package cloudcomputing

/**
  * use sql text to perform query
  * sql text is the most simple method to perform query
  */
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object SqlExamples {

  def main(args: Array[String]): Unit = {
    // turn chatty log off for local development
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val appName = "SqlExamples"

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

    /////////////////// sql (NOT cassandra cql) ///////////////////
    dataframe.createOrReplaceTempView(table)

    // all records in campus "St Lucia"
    val df1 = spark.sql("select * from data where campus = 'St Lucia'") //one time one partition, more than one partition cause full table scan
    df1.show()

    // average temperature in campus "St Lucia"
    val df2 = spark.sql(
      "select campus, avg(temperature) as avg_temp" +
        " from data" +
        " where campus = 'St Lucia'" + //one time one partition, more than one partition cause full table scan
        " group by campus")
    df2.show()

    // average temperature of each day in campus "St Lucia"
    val df3 = spark.sql(
      "select date, avg(temperature) as avg_temp_day" +
        " from data" +
        " where campus = 'St Lucia' " + //one time one partition, more than one partition cause full table scan
        " group by date " +
        " order by date asc")
    df3.show()

    /////////////////// close spark ///////////////////
    spark.stop()
  }
}
