# s3-flink-s3-poc
S3-Flink-S3 proof of concept

## Run local cluster 
Download the flink from this \
Unzip it \
Navigate to unzipped flink folder \
execute ./bin/start-cluster.sh \
Go to localhost:8081 -> you can see flink dashboard UI \
to stop the cluster execute ./bin/stop-cluster.sh

## Run Flink Application
to-do

## Run flink Application on Docker locally
to-do

## Run flink Application on Docker container in dev Instance
to-do

## update JotLog.java
* download avro-tools jar file: https://dlcdn.apache.org/avro/avro-1.11.1/java/avro-tools-1.11.1.jar
* update jot_msg.avsc
* run command to update JotLog.java:
	* java -jar avro-tools-1.11.1.jar compile schema src/main/resources/jot_msg.avsc src/main/java/ 
