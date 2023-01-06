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
import com.civicscience.metrics.JotLogFilterMetricsMapper;
import com.civicscience.metrics.MetricsMapper;
import com.civicscience.model.Profiles.Profile;
import com.civicscience.utils.DataTransformation;
import com.civicscience.utils.ParametersReader;
import java.time.Duration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.enumerate.BlockSplittingRecursiveEnumerator;
import org.apache.flink.connector.file.src.enumerate.FileEnumerator.Provider;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.util.Collector;

/**
 * Flink DataStream Job. To package your application into a JAR file for execution, run 'mvn clean
 * package' on the command line. If you change the name of the main class (with the public static
 * void main(String[] args)) method, change the respective entry in the POM.xml file (simply search
 * for 'mainClass').
 */
public class DataStreamJob {

  public static final String S3_SOURCE_JOTS = "S3-jots";

  /**
   * To specify args please follow this format --key value --key2 value2 --key3 value3
   * <p>
   * Specifying args will override the default all supported parameters see in
   * default-flink-params.yml and POJO {@link Profile} When job submitted you may find all
   * parameters in flink job manager UI: Click on running job->Configuration tab-> User
   * configuration section in that tab
   */
  public static void main(String[] args) throws Exception {

    ImmutablePair<Profile, ParameterTool> profilePair = ParametersReader.readParametersToPOJO(args);

    Profile profile = profilePair.getKey();
    // Number of days to look up in s3 - 18 months
    FilePathFilterS3 filePathFilterS3 = new FilePathFilterS3(
        Duration.ofDays(profile.getFileSourceLookupDaysAgo()));

    // Sets up the execution environment, the main entry point
    // to building Flink applications.
    // FileSystem.initialize(GlobalConfiguration.loadConfiguration(System.getenv("FLINK_CONF_DIR")));
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    env.getConfig().setGlobalJobParameters(profilePair.getValue());
    env.enableCheckpointing(profile.getCheckpointInterval(), CheckpointingMode.EXACTLY_ONCE);
    env.getCheckpointConfig().setCheckpointTimeout(profile.getCheckpointTimeout());
    env.getCheckpointConfig().setCheckpointStorage(profile.getCheckpointStorage());

    final String fullSourceInputPath =
        profile.getProtocol() + profile.getFileSourceBucketName()
            + profile.getFileSourceInputPath();

    FileSource<String> source = FileSource.forRecordStreamFormat(
            new TextLineInputFormat("UTF-8"),
            new Path(fullSourceInputPath))
        .monitorContinuously(Duration.ofMillis(profile.getFileSourceMonitorInterval()))
        .setSplitAssigner(FileSource.DEFAULT_SPLIT_ASSIGNER)
        .setFileEnumerator(
            (Provider) () -> new BlockSplittingRecursiveEnumerator(filePathFilterS3,
                new String[]{"gz"}))

        .build();

    DataStream<String> stream = env.fromSource(source, WatermarkStrategy.forMonotonousTimestamps(),
        S3_SOURCE_JOTS);

    DataTransformation dataTransform = new DataTransformation();

    DataStream<JotLog> jotLogDataStream = stream
        .filter(s -> s.contains(profile.getFileSourceFilter())).map(new JotLogFilterMetricsMapper())
        .flatMap(new FlatMapFunction<String, JotLog>() {
          @Override
          public void flatMap(String s, Collector<JotLog> collector) {
            collector.collect(dataTransform.mapToJotLogObject(s));
          }
        }).map(new MetricsMapper());

    final String fullSinkPath =
        profile.getProtocol() + profile.getFileSinkBucketName() + profile.getFileSinkInputPath();

    final FileSink<JotLog> sink = FileSink
        .forRowFormat(new Path(fullSinkPath), new JotEncoder())
        .withOutputFileConfig(new OutputFileConfig("output-", ".json"))
        .withRollingPolicy(
            DefaultRollingPolicy.builder()
                .withRolloverInterval(Duration.ofMinutes(1))
                .withInactivityInterval(Duration.ofSeconds(20))
                .withMaxPartSize(MemorySize.ofMebiBytes(profile.getFileSinkFileSize()))
                .build())
        .build();

    jotLogDataStream.sinkTo(sink);

    env.execute();
  }
}
