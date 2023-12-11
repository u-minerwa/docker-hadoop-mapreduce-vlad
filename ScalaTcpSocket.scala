import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types._
import spark.implicits._


object HourlyMaxTemperatureApp {
  def main(args: Array[String]): Unit = {
    // Создаём объект Spark и устанавливаем имя приложения, создаём SparkSession для работы с DataFrame: 
    val spark = SparkSession.builder.appName("WeatherStreaming").getOrCreate()
    // Создаём StreamingContext с интервалом 35 секунд: 
    val ssc = new StreamingContext(spark.sparkContext, Seconds(35))
    // Указываем хост и порт для прослушивания данных:
    val host = "localhost"
    val port = 9999
    // Создаём jsonStream, который будет принимать данные из сетевого источника: 
    val jsonStream = ssc.socketTextStream(host, port)
    
    val schema = StructType(Seq(
      StructField("city", StringType, true),
      StructField("temp", FloatType, true)
    ))

    // Пример: агрегация средней температуры за определённое время: 
    jsonStream.foreachRDD { rdd =>
      if (!rdd.isEmpty()) {
        // Парсим JSON и создаём DataFrame:
        val weatherDF = spark.read.schema(schema).json(rdd)
        // Создаём таблицу: 
        weatherDF.createOrReplaceTempView("weather")
        // Задаём SQL-запрос и аггрегируем данные:
        val aggregatedDF = spark.sql("SELECT city, AVG(temp) as avg_temperature FROM weather GROUP BY city")
        aggregatedDF.show()
        aggregatedDF.write.mode("append").csv("/output/aggregated_results")
      }
    }

    // Запускаем стриминг
    ssc.start()
  }
}

