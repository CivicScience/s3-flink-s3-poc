package com.civicscience.datapipeline;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.flink.api.common.io.FilePathFilter;
import org.apache.flink.core.fs.Path;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePathFilterS3 extends FilePathFilter {
    Pattern datePartsFromPath = Pattern.compile("\\/(?<year>\\d{4})\\/?(?<month>\\d{2})?\\/?(?<day>\\d{2})?");

    private final Duration ageLimit;

    public FilePathFilterS3(Duration ageLimit) {
        this.ageLimit=ageLimit;
    }

    @Override
    public boolean filterPath(Path path) {

        Matcher matcher = datePartsFromPath.matcher(path.toString());

        if (matcher.find()) {
            ZonedDateTime limit = ZonedDateTime.now(ZoneId.of("UTC")).minus(ageLimit);

            int year = NumberUtils.toInt(matcher.group("year"));
            int month = NumberUtils.toInt(matcher.group("month"), limit.getMonthValue());
            int day = NumberUtils.toInt(matcher.group("day"), limit.getDayOfMonth());

            if (year != limit.getYear()) {
                return year < limit.getYear();
            }

            if (month != limit.getMonthValue()) {
                return month < limit.getMonthValue();
            }

            if (day != limit.getDayOfMonth()) {
                return day < limit.getDayOfMonth();
            }

        }

        return true;
    }
}
