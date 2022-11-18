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

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.core.fs.Path;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;

import org.apache.flink.connector.file.sink.FileSink;

import java.time.Duration;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJob {
	static FilePathFilterS3 filePathFilterS3 = new FilePathFilterS3(Duration.ofDays(10));

	public static void main(String[] args) throws Exception {
		// Sets up the execution environment, the main entry point
		// to building Flink applications.
		TextInputFormat inputFormat = new TextInputFormat(new Path("s3://civicscience-shan-dwf-poc/jotLog/AWSLogs/825286309336/elasticloadbalancing/us-east-1/"));
		inputFormat.setNestedFileEnumeration(true);
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.enableCheckpointing(10000, CheckpointingMode.EXACTLY_ONCE);
//		env.getCheckpointConfig().setCheckpointTimeout(100000);
//		env.getCheckpointConfig().setCheckpointStorage("s3://");


		DataStream<String> stream = env.readFile(inputFormat,"s3://civicscience-shan-dwf-poc/jotLog/AWSLogs/825286309336/elasticloadbalancing/us-east-1/", FileProcessingMode.PROCESS_CONTINUOUSLY,10,filePathFilterS3).setParallelism(10);

		DataTransformation dataTransform = new DataTransformation();
		DataStream<String> jotLogDataStream = stream
				.filter(s -> s.contains("/jot"))
				.flatMap(new FlatMapFunction<String, String>() {
					@Override
					public void flatMap(String s, Collector<String> collector) throws Exception {
						ObjectMapper mapper = new ObjectMapper();
						collector.collect(mapper.writeValueAsString(dataTransform.mapToJotLogObject(s)));
					}
				});

		//jotLogDataStream.writeAsCsv("/Users/sponnada/Desktop/flinkSInk", FileSystem.WriteMode.OVERWRITE,"\n",",");

		final FileSink<String> sink = FileSink
				.forRowFormat(new Path("s3://civicscience-shan-dwf-poc/sinkLog"), new SimpleStringEncoder<String>("UTF-8"))
				.withOutputFileConfig(new OutputFileConfig("flink", ".txt"))
				.withRollingPolicy(
						DefaultRollingPolicy.builder()
								.withRolloverInterval(Duration.ofMinutes(1))
								.withInactivityInterval(Duration.ofSeconds(20))
								.withMaxPartSize(MemorySize.ofMebiBytes(1))
								.build())
				.build();

		jotLogDataStream.sinkTo(sink);

		//jotLogDataStream.writeAsText("/Users/sponnada/Desktop/flinkSInk");
		env.execute();
	}
}
