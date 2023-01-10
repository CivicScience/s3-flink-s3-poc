package com.civicscience.metrics;

import com.civicscience.entity.JotLog;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;

/**
 * JotLogTransformMetricMapper extends the RichMapFunction from Flink
 * Overrides the open and map methods
 * This class implements a custom metric
 * We are counting number of Jot logs transformed after filtering them.
 * Name of the metric is TotalJotLogsTransformed
 */
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
