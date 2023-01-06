package com.civicscience.utils;

import com.civicscience.model.Profiles.Profile;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.flink.api.java.utils.ParameterTool;
import org.junit.Assert;
import org.junit.Test;

public class ParametersReaderTest {

  @Test
  public void testDefaultParametersReadIfNotSpecifiedParams() {
    Assert.assertEquals("testSinkBucket",
        ParametersReader.readParametersToPOJO(new String[]{}).getKey().getFileSinkBucketName());

    Assert.assertEquals("testSourceBucket",
        ParametersReader.readParametersToPOJO(new String[]{}).getKey().getFileSourceBucketName());

    Assert.assertEquals(14,
        ParametersReader.readParametersToPOJO(new String[]{}).getValue().getNumberOfParameters());

  }

  @Test
  public void testOverrideDefaultParametersIfSpecifiedParams() {
    Assert.assertEquals("overrideSourceBucketName",
        ParametersReader.readParametersToPOJO(new String[]{"--filesource_bucket_name", "overrideSourceBucketName"}).getKey().getFileSourceBucketName());

    Assert.assertEquals("overrideSinkBucketName",
        ParametersReader.readParametersToPOJO(new String[]{"--filesink_bucket_name", "overrideSinkBucketName"}).getKey().getFileSinkBucketName());

    Assert.assertEquals(14,
        ParametersReader.readParametersToPOJO(new String[]{}).getValue().getNumberOfParameters());

    //if we specify non existing param
    ImmutablePair<Profile, ParameterTool> result=ParametersReader.readParametersToPOJO(new String[]{"--notExistingParam", "noValue"});
    Assert.assertEquals(15, result.getValue().getNumberOfParameters());


  }

}
