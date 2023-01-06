package com.civicscience.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@AllArgsConstructor
@Jacksonized
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profiles implements Serializable {

  @JsonProperty("dev")
  private Profile dev;

  @JsonProperty("prod")
  private Profile prod;

  public Profile getActiveProfile(String activeProfile) {
    if (activeProfile.equalsIgnoreCase("dev")) {
      return dev;
    }
    return prod;
  }

  @Data
  @AllArgsConstructor
  @Jacksonized
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Profile implements Serializable {

    @JsonProperty("protocol")
    private String protocol;

    @JsonProperty("checkpoint_interval")
    private int checkpointInterval;

    @JsonProperty("checkpoint_timeout")
    private int checkpointTimeout;

    @JsonProperty("checkpoint_storage")
    private String checkpointStorage;

    @JsonProperty("filesource_parallelism")
    private int fileSourceParallelism;

    @JsonProperty("filesource_bucket_name")
    private String fileSourceBucketName;

    @JsonProperty("filesource_lookup_days_ago")
    private int fileSourceLookupDaysAgo;

    @JsonProperty("filesource_input_path")
    private String fileSourceInputPath;

    @JsonProperty("filesource_filter")
    private String fileSourceFilter;

    @JsonProperty("filesource_monitor_interval")
    private int fileSourceMonitorInterval;

    @JsonProperty("filesink_parallelism")
    private int fileSinkParallelism;

    @JsonProperty("filesink_bucket_name")
    private String fileSinkBucketName;

    @JsonProperty("filesink_input_path")
    private String fileSinkInputPath;

    @JsonProperty("filesink_part_size")
    private int fileSinkFileSize;
  }


}
