/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.civicscience.datapipeline;

import com.civicscience.entity.JotLog;
import com.civicscience.utils.DataTransformation;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.enumerate.BlockSplittingRecursiveEnumerator;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.util.Collector;

import org.apache.flink.connector.file.src.enumerate.FileEnumerator.Provider;


import java.time.Duration;

/**
 * Flink DataStream Job.
 *To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 * If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJob {

	// Number of days to look up in s3 - 18 months
	// TODO needs to put in env variables for number of days back to look up
	private static final FilePathFilterS3 filePathFilterS3 = new FilePathFilterS3(Duration.ofDays(10));
	public static final String S3_SOURCE_JOTS = "S3-jots";
	public static final int SOURCE_PARALLELISM = 10;

	private final static String PROTOCOL = "s3a://";
	//TODO needs to put in env variables
	private final static String SOURCE_BUCKET_NAME = "civicscience-shan-dwf-poc";

	//TODO needs to put in env variables
	private final static String INPUT_PATH =
			PROTOCOL + SOURCE_BUCKET_NAME + "/jotLog/AWSLogs/825286309336/elasticloadbalancing/us-east-1/";

	//TODO needs to put in env variables
	private final static String SINK_PATH =
			PROTOCOL + "civicscience-dwf-poc" + "/sink";

	public static void main(String[] args) throws Exception {
		// Sets up the execution environment, the main entry point
		// to building Flink applications.

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.enableCheckpointing(10000, CheckpointingMode.EXACTLY_ONCE);
		env.getCheckpointConfig().setCheckpointTimeout(100000);
//		env.getCheckpointConfig().setCheckpointStorage("s3://");

		FileSource<String> source = FileSource.forRecordStreamFormat(
						new TextLineInputFormat("UTF-8"),
						new Path(INPUT_PATH))
				.monitorContinuously(Duration.ofMillis(1000))
				.setSplitAssigner(FileSource.DEFAULT_SPLIT_ASSIGNER)
				.setFileEnumerator(
						(Provider) () -> new BlockSplittingRecursiveEnumerator(filePathFilterS3,
								new String[]{"gz"}))
				.build();

		DataStream<String> stream = env.fromSource(source, WatermarkStrategy.forMonotonousTimestamps(),
				S3_SOURCE_JOTS).setParallelism(SOURCE_PARALLELISM);

		DataTransformation dataTransform = new DataTransformation();

		DataStream<JotLog> jotLogDataStream = stream
				.filter(s -> s.contains("/jot"))
				.flatMap(new FlatMapFunction<String, JotLog>() {
					@Override
					public void flatMap(String s, Collector<JotLog> collector){
						collector.collect(dataTransform.mapToJotLogObject(s));
					}
				});


		final FileSink<JotLog> sink = FileSink
				.forRowFormat(new Path(SINK_PATH), new JotEncoder())
				.withOutputFileConfig(new OutputFileConfig("output-", ".json"))
				.withRollingPolicy(
						DefaultRollingPolicy.builder()
								.withRolloverInterval(Duration.ofMinutes(1))
								.withInactivityInterval(Duration.ofSeconds(20))
								.withMaxPartSize(MemorySize.ofMebiBytes(10))
								.build())
				.build();

		jotLogDataStream.sinkTo(sink);

		env.execute();
	}
}
