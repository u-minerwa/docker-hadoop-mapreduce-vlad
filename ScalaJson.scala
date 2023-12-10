import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._


object WeatherStreamingApp {
  def main(args: Array[String]): Unit = {
    // Создаем объект SparkConf и устанавливаем имя приложения
    val sparkConf = new SparkConf().setAppName("WeatherStreamingApp")

    // Создаем StreamingContext с интервалом 1 секунда
    val ssc = new StreamingContext(sparkConf, Seconds(1))

    // Указываем директорию, где будут появляться новые файлы
    val inputDirectory = "/../SparkStreamingProj/jsons" 

    // Создаем DStream, который будет принимать данные из файлов
    val jsonStream = ssc.textFileStream(inputDirectory)

    // Создаем SparkSession для работы с DataFrame
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    // Пример: агрегация средней температуры за каждый день
    val jsonDFStream: DStream[DataFrame] = jsonStream.map { jsonString =>
      // Парсим JSON и создаем DataFrame
      val jsonDF = spark.read.json(Seq(jsonString).toDS())
      jsonDF
    }

    val resultStream: DStream[Row] = jsonDFStream
      .filter("temperature is not null") // фильтруем записи без информации о температуре
      .withColumn("date", to_date($"timestamp")) // извлекаем дату из временной метки
      .groupBy($"date")
      .agg(avg("temperature").alias("average_temperature"))

    // Выводим результат в консоль
    resultStream.print()

    // Сохраняем результат в файл
    resultStream.foreachRDD { rdd =>
      val resultDF = rdd.toDF()
      resultDF.write.mode("append").json("/scalasaves/prefix2") 
    }

    // Запускаем стриминг
    ssc.start()

    // Ожидаем завершения работы
    ssc.awaitTermination()
  }
}

