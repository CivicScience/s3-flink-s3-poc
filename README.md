# s3-flink-s3-poc
S3-Flink-S3 data pipeline

## Run local cluster 
Download the flink from this \
Unzip it \
Navigate to unzipped flink folder \
execute ./bin/start-cluster.sh \
Go to localhost:8081 -> you can see flink dashboard UI \
to stop the cluster execute ./bin/stop-cluster.sh

# FileSystem - s3
We are using Flink’s Filesystem to read the files from s3. \
This need to copy the opt/flink-s3-fs-hadoop-1.15.2.jar into plugins/s3/ \
We need to add fs.allowed-fallback-filesystems:s3 to conf -> flink-conf.yaml \
These steps must be performed in EMR before spinning up a cluster.

# BootStrap
EMR comes with java 8 as default. But we need Java 11. \
We need to bootstrap java 11 on emr. \
This is done by a bootstrap script. \
Bootstrap script must be located on s3, so that EMR can access it. \
If any changes are made to bootstrap.sh in this repository, please upload updated script to s3 location as well

# Configurations
"Fs.allowed-fallback-filesystems:s3": "s3a" \
"Env.java.home":  "/usr/lib/jvm/java-11-openjdk" \
We will have configurations for metrics as well \
This configuration should be passed to EMR \
configurations.json is a copy of the file from s3. Any changes must be updated in s3 as well
