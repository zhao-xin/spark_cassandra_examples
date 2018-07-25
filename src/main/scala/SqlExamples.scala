package cloudcomputing

/**
  * use sql text to perform query
  * sql text is the most simple method to perform query
  */
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.spark.sql.SparkSession

object SqlExamples {

  def main(args: Array[String]): Unit = {

    // turn off chatty off for local development. DO NOT do this in production
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    /////////////////// configurations ///////////////////
    val sparkMaster = "local[4]"  //can override spark master address when submitting spark jobs

    // cassandra address and credentials should be managed by tools like k8s, here we use hardcode for simplicity.
      val cassandraHost = "192.168.128.81,192.168.128.82,192.168.128.83" //change cassandra addresses to yours
//      val cassandraPort = "9042" // default cassandra port can be skipped
//      val cassandraAuthUsername = "cassandra" //anonymously login can be skipped
//      val cassandraAuthPassword = "cassandra"

    /////////////////// init spark ///////////////////
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", cassandraHost)
//      .set("spark.cassandra.connection.port", cassandraPort)
//      .set("spark.cassandra.auth.username", cassandraAuthUsername)
//      .set("spark.cassandra.auth.password", cassandraAuthPassword)

    val appName = "SqlExamples"
    val sc = new SparkContext(sparkMaster, appName, conf)

    /////////////////// init spark sql ///////////////////
    val keySpace = "cloudcomputing"
    val table = "data"
    val spark = SparkSession.builder.appName(sc.appName).master(sc.master).config(sc.getConf).getOrCreate

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
    sc.stop()
  }
}