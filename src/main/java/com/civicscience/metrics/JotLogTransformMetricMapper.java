package com.civicscience.metrics;

import com.civicscience.entity.JotLog;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;

public class JotLogTransformMetricMapper extends RichMapFunction<JotLog, JotLog> {

  private transient Counter eventCounter;

  @Override
  public void open(Configuration parameters) {
    eventCounter = getRuntimeContext().getMetricGroup().counter("TotalJotLogsTransformed");
  }

  @Override
  public JotLog map(JotLog value) {
    eventCounter.inc();
    return value;
  }
}
