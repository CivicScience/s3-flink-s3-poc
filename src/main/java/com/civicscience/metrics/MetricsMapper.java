package com.civicscience.metrics;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MetricsMapper extends RichMapFunction<String, String> {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsMapper.class);
    private transient Counter eventCounter;
    @Override
    public void open(Configuration parameters) throws IOException {
        LOG.debug("Inside metric mapper");
        eventCounter = getRuntimeContext().getMetricGroup().counter("events");
    }

    @Override
    public String map(String value) throws Exception {
        LOG.info("Inside map");
        eventCounter.inc();
        return value;
    }
}
