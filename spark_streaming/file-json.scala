import spark.implicits._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger

val schema = new StructType()
    .add("name", StringType)
    .add("float", FloatType)
    .add("qualityValue", StringType)
    .add("marketplaceName", StringType)
    .add("marketplaceUrl", StringType)
    .add("USDPrice", IntegerType)

val skins = spark.readStream.schema(schema).json("./jsons/")

val aggregatedFloat = skins.groupBy("name")
    .agg(avg("float").as("Average Float"))

val query = aggregatedFloat
  .writeStream
  .format("parquet")
  .outputMode("append")
  .option("checkpointLocation", "/spark") 
  .option("path", "/avgfloat") 
  .trigger(Trigger.ProcessingTime("10 seconds"))
  .start()

query.awaitTermination()