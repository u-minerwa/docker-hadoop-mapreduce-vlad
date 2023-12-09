cd mapred
cd mapred
start build.bat

TIMEOUT /T 25

docker cp target/mapred-jar-with-dependencies.jar namenode:/mr.jar

docker exec -it namenode bin/bash ./runjob.sh
cd ..
docker cp namenode:/part-r-00000 output.txt
TIMEOUT /-1