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
    // turn off chatty off for local development. DO NOT do this in production
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val appName = "SqlExamples"

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

    /////////////////// sql (NOT cassandra cql) ///////////////////

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