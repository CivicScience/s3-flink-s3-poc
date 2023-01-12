package com.civicscience.datapipeline;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.flink.api.common.io.FilePathFilter;
import org.apache.flink.core.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePathFilterS3 extends FilePathFilter implements Predicate<Path>, Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(FilePathFilterS3.class);
  private static final Pattern pattern = Pattern.compile(".*/(\\d{4}/\\d{2}/\\d{2})/.*");

  private static final AmazonS3 amazonS3Client = AmazonS3ClientBuilder.defaultClient();

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
    ZonedDateTime limit = ZonedDateTime.now(ZoneId.of("UTC")).minus(ageLimit);
    System.out.println(limit);
    Matcher matcher = pattern.matcher(path.toString());
    if (matcher.matches()) {
      LocalDate d = LocalDate.parse(matcher.group(1), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      if (d.getYear() < limit.getYear()) {
        return false;
      } else {
        if (d.getMonth().getValue() < limit.getMonth().getValue()) {
          return false;
        } else {
          if (d.getDayOfMonth() < limit.getDayOfMonth()) {
            return false;
          } else {
            if(amazonS3Client.getObjectMetadata(path.getParent().toString().substring(6),path.getName()).getContentLength() == 0 ) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean test(Path path) {
    return acceptFile(path);
  }

  /**
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
