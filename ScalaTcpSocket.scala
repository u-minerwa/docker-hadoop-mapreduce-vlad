import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._


object HourlyMaxTemperatureApp {
  def main(args: Array[String]): Unit = {
    // Создаем объект SparkConf и устанавливаем имя приложения
    val sparkConf = new SparkConf().setAppName("HourlyMaxTemperatureApp")

    // Создаем StreamingContext с интервалом 1 секунда
    val ssc = new StreamingContext(sparkConf, Seconds(1))

    // Указываем хост и порт для прослушивания данных
    val host = "localhost"
    val port = 9999

    // Создаем DStream, который будет принимать данные из сетевого источника
    val jsonStream = ssc.socketTextStream(host, port)

    // Создаем SparkSession для работы с DataFrame
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    // Пример: агрегация максимальной температуры за каждый час
    val jsonDFStream: DStream[DataFrame] = jsonStream.map { jsonString =>
      // Парсим JSON и создаем DataFrame
      val jsonDF = spark.read.json(Seq(jsonString).toDS())
      jsonDF
    }

    val resultStream: DStream[Row] = jsonDFStream
      .filter("temperature is not null") // фильтруем записи без информации о температуре
      .groupBy(window($"date_time_now", "1 hour"))      // timestamp
      .agg(max("temp").alias("max_temperature"))     // temperature

    // Выводим результат в консоль
    resultStream.print()

    // Сохраняем результат в файл
    resultStream.foreachRDD { rdd =>
      val resultDF = rdd.toDF()
      resultDF.write.mode("append").json("/scalasaves/prefix1") 
    }

    // Запускаем стриминг
    ssc.start()

    // Ожидаем завершения работы
    ssc.awaitTermination()
  }
}

