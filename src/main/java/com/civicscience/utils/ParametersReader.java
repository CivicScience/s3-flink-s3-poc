package com.civicscience.utils;

import com.civicscience.model.Profiles;
import com.civicscience.model.Profiles.Profile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.flink.api.java.utils.ParameterTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ParametersReader {

  private static final Logger LOG = LoggerFactory.getLogger(ParametersReader.class);

  private final static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  private final static String ACTIVE_PROFILE_PARAM_NAME = "active_profile";
  private final static String ACTIVE_PROFILE_PARAM_VALUE = "dev";

  private final static String DEFAULT_FLINK_PARAMS_PATH = "default-flink-params.yml";

  public static ImmutablePair<Profile, ParameterTool> readParametersToPOJO(String[] args) {
    ParameterTool mergedParams;
    Profile finalProfile;
    try {
      //reading params from flink program
      ParameterTool flinkSpecifiedParams = ParameterTool.fromArgs(args);

      //reading default params from default-flink-params.yml
      InputStream inputStream = IOUtils.toBufferedInputStream(
          getFileFromResourceAsStream(DEFAULT_FLINK_PARAMS_PATH));
      Profiles defaultFlinkParams = mapper.readValue(inputStream, Profiles.class);

      //get active profile based on specified param. In case of null, dev is set by default
      String activeProfile = flinkSpecifiedParams.get(ACTIVE_PROFILE_PARAM_NAME,
          ACTIVE_PROFILE_PARAM_VALUE);
      Profile profile = defaultFlinkParams.getActiveProfile(activeProfile);

      Map<String, String> profilePOJOToMap =
          mapper.convertValue(profile, new TypeReference<Map<String, String>>() {
          });

      ParameterTool flinkDefaultParams = ParameterTool.fromMap(profilePOJOToMap);

      //trying to merge params from what we specified with default.
      mergedParams = flinkDefaultParams.mergeWith(flinkSpecifiedParams);
      finalProfile = mapper.convertValue(mergedParams.getConfiguration().toMap(),
          Profile.class);

      LOG.info("Read parameters: {}", finalProfile);
    } catch (Exception e) {
      String errorMessage = "Can not read parameters";
      LOG.error(errorMessage, e);
      throw new RuntimeException(errorMessage);
    }
    return new ImmutablePair(finalProfile, mergedParams);
  }

  private static InputStream getFileFromResourceAsStream(String fileName) {

    // The class loader that loaded the class
    ClassLoader classLoader = ParametersReader.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);

    if (inputStream == null) {
      throw new IllegalArgumentException("file not found! " + fileName);
    } else {
      return inputStream;
    }

  }


}
