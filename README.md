# s3-flink-s3-poc
S3-Flink-S3 data pipeline

## Run local cluster 
Download the flink from this \
Unzip it \
Navigate to unzipped flink folder \
execute ./bin/start-cluster.sh \
Go to localhost:8081 -> you can see flink dashboard UI, where you can submit jobs \
to stop the cluster execute ./bin/stop-cluster.sh

## Submitting job with params
We default values in default-flink-params.yml \
filesource_input_path should be always /AWSLogs/825286309336/elasticloadbalancing/us-east-1 \
To override these, pass the params in format --key value --key2 value2 --key3 value3 \
Example \
--checkpoint_storage s3://civicscience-shan-dwf-poc/checkpoints --filesource_bucket_name civicscience-shan-dwf-poc --filesink_bucket_name civicscience-shan-dwf-poc

# Prometheus Pushgateway
Run this locally using docker image. It will be on localhost:9091 \
docker pull prom/pushgateway \
docker run -d -p 9091:9091 prom/pushgateway \
In flink-conf.yml add following \
metrics.reporter.promgateway.factory.class: org.apache.flink.metrics.prometheus.PrometheusPushGatewayReporterFactory \
metrics.reporter.promgateway.hostUrl: http://127.0.0.1:9091 \
metrics.reporter.promgateway.jobName: DataStreamJob \
metrics.reporter.promgateway.randomJobNameSuffix: false \
metrics.reporter.promgateway.deleteOnShutdown: false \
metrics.reporter.promgateway.interval: '60 SECONDS' \


# FileSystem - s3
We are using Flink’s Filesystem to read the files from s3. \
This need to copy the opt/flink-s3-fs-hadoop-1.15.2.jar into plugins/s3/ \
We need to add fs.allowed-fallback-filesystems:s3a to conf -> flink-conf.yaml \
These steps must be performed in EMR before spinning up a cluster. \
To run in local cluster add s3 access keys to local flink-conf.yml \
s3.access-key: ******** \
s3.secret-key: ***************** \
Don't do this in EMR, since the IAM role has access to S3 \


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
