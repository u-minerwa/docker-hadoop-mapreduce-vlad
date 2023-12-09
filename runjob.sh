 #!/bin/bash
   
ls

hdfs dfs -rmr /output
rm part-r-00000
hadoop jar mr.jar /input /output

hdfs dfs -get /output/part-r-00000

exit   