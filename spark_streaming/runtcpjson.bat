docker cp tcp-json.scala spark-master:/tcp-json.scala

docker exec -it spark-master spark/bin/spark-shell -i tcp-json.scala