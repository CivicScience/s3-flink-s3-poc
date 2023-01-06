package com.civicscience.datapipeline;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Predicate;
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

  @Override
  public boolean filterPath(Path path) {
    LOG.info("Filtering path: {}", path.toString());
    String[] s = path.toString().split("/");

    ZonedDateTime limit = ZonedDateTime.now(ZoneId.of("UTC")).minus(ageLimit);
      if (s.length == 9 && Integer.parseInt(s[8]) >= limit.getYear()) {
          return true;
      }
    if (s.length == 10) {
        if (Integer.parseInt(s[8]) > limit.getYear()) {
            return true;
        }
        if (Integer.parseInt(s[8]) == limit.getYear()
            && Integer.parseInt(s[9]) >= limit.getMonthValue()) {
            return true;
        }
    }
    if (s.length >= 11) {
        if (Integer.parseInt(s[8]) > limit.getYear()) {
            return true;
        }
        if (Integer.parseInt(s[8]) == limit.getYear()
            && Integer.parseInt(s[9]) >= limit.getMonthValue()
            && Integer.parseInt(s[10]) >= limit.getDayOfMonth()) {
            return true;
        }
    }
    return s.length <= 8;
  }

  @Override
  public boolean test(Path path) {
    return acceptFile(path);
  }

  private boolean acceptFile(Path path) {
    final String name = path.getName();
    return !name.startsWith("_")
        && !name.startsWith(".")
        && filterPath(path);
  }
}
