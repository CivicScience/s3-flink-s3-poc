package com.civicscience.utils;

import com.civicscience.entity.JotLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTransformation implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(DataTransformation.class);
    public JotLog mapToJotLogObject(String s) {
        JotLog log = new JotLog();
        List<String> list = splitTheString(s);

        //Transform fields to match destination table
        log.setTs(list.get(1));

        transformURL(list.get(12));
        transformUserAgent(list.get(13));
        return log;
    }
    public List<String> splitTheString(String s){
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(s);
        while (m.find())
            list.add(m.group(1));
        return list;
    }
    public void transformURL(String url){
        URLDecoder.decode(url, StandardCharsets.UTF_8);
    }
    public void transformUserAgent(String user_agent){

    }
}
