package com.civicscience.metrics;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;

/**
 * JotLogFilterMetricsMapper extends the RichMapFunction from Flink
 * Overrides the open and map methods
 * This class implements a custom metric
 * We are counting number of Jot logs filtered from original ALB log
 * Name of the metric is TotalJotLogsFiltered
 */
public class JotLogFilterMetricsMapper extends RichMapFunction<String, String> {

  private transient Counter eventCounter;

  @Override
  public void open(Configuration parameters) {
    eventCounter = getRuntimeContext().getMetricGroup().counter("TotalJotLogsFiltered");
  }

  @Override
  public String map(String value) {
    eventCounter.inc();
    return value;
  }
}
