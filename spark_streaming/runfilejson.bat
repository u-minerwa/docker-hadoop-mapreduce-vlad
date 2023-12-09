docker cp file-json.scala spark-master:/file-json.scala

docker exec -it spark-master spark/bin/spark-shell -i file-json.scala