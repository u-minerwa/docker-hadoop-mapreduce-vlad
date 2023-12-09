import spark.implicits._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming._
val schema = new StructType()
    .add("name", StringType)
    .add("float", FloatType)
    .add("qualityValue", StringType)
    .add("marketplaceName", StringType)
    .add("marketplaceUrl", StringType)
    .add("USDPrice", IntegerType)
    

val lines = spark.readStream
  .format("socket")
  .option("host", "172.18.0.10")
  .option("port", 9999)
  .load()

val parsedDF = lines
  .select(from_json(col("value"), schema))


val skins = parsedDF
  .select("from_json(value).name", "from_json(value).float", "from_json(value).qualityValue", "from_json(value).marketplaceName", "from_json(value).marketplaceUrl", "from_json(value).USDPrice")

val skinsTimestamp = skins.withColumn("timestamp", current_timestamp())


val windowedCounts = skinsTimestamp
  .withWatermark("timestamp", "1 minute")  
  .groupBy(window($"timestamp", "1 minute"),  $"name")
  .agg(avg("USDPrice").as("Average_Price"))
  .select("window.start", "window.end", "name", "Average_Price")


val query = windowedCounts
  .writeStream
  .outputMode("append")  
  .format("parquet") 
  .option("checkpointLocation", "/spark")
  .option("path", "/timestampParquet")
  .trigger(Trigger.ProcessingTime("1 minute")) 
  .start()

query.awaitTermination()

