package com.civicscience.datapipeline;


import com.civicscience.entity.JotLog;

import java.io.Serializable;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTransformation implements Serializable {


    public JotLog mapToJotLogObject(String s) {
        s = URLDecoder.decode(s);
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(s);
        while (m.find())
            list.add(m.group(1));
        JotLog log = new JotLog();
        transformURL(list.get(12));
        transformUserAgent(list.get(13));
        log.setTs(list.get(1));
        return log;
    }

    public void transformURL(String url){

    }
    public void transformUserAgent(String user_agent){

    }
}
