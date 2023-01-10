package com.civicscience.datapipeline;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.io.FilePathFilter;
import org.apache.flink.core.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePathFilterS3 extends FilePathFilter implements Predicate<Path> {

  private static final Logger LOG = LoggerFactory.getLogger(FilePathFilterS3.class);

  private final Duration ageLimit;

  public FilePathFilterS3(Duration ageLimit) {
    this.ageLimit = ageLimit;
  }

  /**
   * @param path - folder path, recursively
   * @return boolean, depending on the ageLimit path looks like
   * s3://civicscience-alb/AWSLogs/825286309336/elasticloadbalancing/us-east-1/2023/01/01/log.gz We
   * look at year, month and date in regard to ageLimit and return if path is within limit
   */
  @Override
  public boolean filterPath(Path path) {
    LOG.info("Filtering path: {}", path.toString());
    String[] s = path.toString().split("/");

    ZonedDateTime limit = ZonedDateTime.now(ZoneId.of("UTC")).minus(ageLimit);
    if(s.length == 8 && !StringUtils.isNumeric(s[7])){
      return false;
    }
    //If path length is 8, we just compare year
    if (s.length == 8 && Integer.parseInt(s[7]) >= limit.getYear()) {
      return true;
    }
    //If the path length is 9, we need to check year and month
    if (s.length == 9) {
      if (Integer.parseInt(s[7]) > limit.getYear()) {
        return true;
      }
      if (Integer.parseInt(s[7]) == limit.getYear()
          && Integer.parseInt(s[8]) >= limit.getMonthValue()) {
        return true;
      }
    }
    //If the path length is 10, we need to check year, month and day
    if (s.length >= 10) {
      if (Integer.parseInt(s[7]) > limit.getYear()) {
        return true;
      }
      if (Integer.parseInt(s[7]) == limit.getYear()
          && Integer.parseInt(s[8]) >= limit.getMonthValue()
          && Integer.parseInt(s[9]) >= limit.getDayOfMonth()) {
        return true;
      }
    }
    return s.length <= 8;
  }

  @Override
  public boolean test(Path path) {
    return acceptFile(path);
  }

  /**
   *
   * @param path - actual file path
   * @return boolean
   */
  private boolean acceptFile(Path path) {
    final String name = path.getName();
    return !name.startsWith("_")
        && !name.startsWith(".")
        && filterPath(path);
  }
}
