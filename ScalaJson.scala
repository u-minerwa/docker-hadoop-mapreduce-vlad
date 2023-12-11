import org.apache.spark.SparkConf
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.sql.Row
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.{SparkSession, Row}
import org.apache.spark.sql.types._
import spark.implicits._


object WeatherStreamingApp {
  def main(args: Array[String]): Unit = {
    // Создаём объект SparkSession и устанавливаем имя приложения, создаём SparkSession для работы с DataFrame: 
    val spark = SparkSession.builder.appName("WeatherStreaming").getOrCreate()
    // Создаём StreamingContext с интервалом 35 секунд: 
    val ssc = new StreamingContext(spark.sparkContext, Seconds(35))

    val schema = StructType(Seq(
      StructField("city", StringType, true),
      StructField("temp", FloatType, true),
    ))
    // Указываем директорию, где будут появляться новые файлы:
    val inputDirectory = "jsons" 
    // Создаем jsonStream, который будет принимать данные из файлов:
    val jsonStream = ssc.textFileStream(inputDirectory)

    //ВАРИАНТ С WINDOW
    // val windowedWeatherStream = jsonStream.window(Seconds(35), Seconds(35))

    // Пример: агрегация средней температуры за определённый период времени: 
    jsonStream.foreachRDD { rdd =>
      if (!rdd.isEmpty()) {
        val jsonDF = spark.read.schema(schema).json(rdd)
        val avgTemperatureDF = jsonDF.select("city", "temp").groupBy("city").agg(avg("temp").as("avg_temperature"))
        avgTemperatureDF.show()
        avgTemperatureDF.write.mode("append").csv("/jsonsOutput/average_temperature")
      }
    }

    // Запускаем стриминг:
    ssc.start()
    // Ожидаем завершения работы: 
    ssc.awaitTermination()
  }
}

