package cloudcomputing

/**
  * baic cassandra query
  */
import org.apache.spark._
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

object CassandraQuery {

  def main(args: Array[String]): Unit = {
    //    val sparkMaster = "spark://*.*.*.*:7077"
    val sparkMaster = "local[4]"

    val cassandraHost = "cassandra1,cassandra2,cassandra3"

    //    val cassandraPort = "9042"
    //    val cassandraAuthUsername = "cassandra"
    //    val cassandraAuthPassword = "cassandra"

    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", cassandraHost)
    //      .set("spark.cassandra.connection.port", cassandraPort)
    //      .set("spark.cassandra.auth.username", cassandraAuthUsername)
    //      .set("spark.cassandra.auth.password", cassandraAuthPassword)

    val keySpace = "cloudcomputing"
    val table = "data"

    val appName = "BasicQuery"

    val sc = new SparkContext(sparkMaster, appName, conf)
    val sparkSession = SparkSession.builder.appName(sc.appName).master(sc.master).config(sc.getConf).getOrCreate

    sparkSession
      .read
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> keySpace, "table" -> table))
      .load()
      .createOrReplaceTempView("data")

    val dataframe = sparkSession.sql("select * from data where sensor_id = 1") //sql style

    dataframe.show

    sc.stop()
  }
}