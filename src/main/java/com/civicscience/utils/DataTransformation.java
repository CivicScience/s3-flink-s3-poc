package com.civicscience.utils;

import com.civicscience.entity.JotLog;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTransformation implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(DataTransformation.class);
    public JotLog mapToJotLogObject(String s) {
        JotLog log = new JotLog();
        List<String> list = splitTheString(s);

        //Transform fields to match destination table
        log.setTs(list.get(1));
        log.setIp_v4(list.get(3).split(":")[0]);

        Map<String, List<String>> urlParts = transformURL(list.get(12));
        log.setId(urlParts.get("j").get(0));
        log.setSequence(Integer.valueOf(urlParts.get("n").get(0)));
        log.setSpace(urlParts.get("s").get(0));
        log.setState(urlParts.get("t").get(0));

        UserAgent ua = transformUserAgent(list.get(13));
        log.setUa_device_class(ua.getValue("DeviceClass"));
        log.setUa_device_family(ua.getValue("DeviceName"));
        log.setUa_os_family(ua.getValue("OperatingSystemName"));
        log.setUa_os_version(ua.getValue("OperatingSystemVersion"));
        log.setUa_browser_family(ua.getValue("AgentName"));
        log.setUa_browser_version(ua.getValue("AgentVersion"));
        return log;
    }
    public List<String> splitTheString(String s){
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(s);
        while (m.find())
            list.add(m.group(1));
        return list;
    }
    public Map<String, List<String>> transformURL(String s){
        String[] parts = s.split(" ");
        String url = parts[1];
        Map<String, List<String>> parameters = new QueryStringDecoder(url).parameters();
        return parameters;
    }
    public UserAgent transformUserAgent(String user_agent){
        UserAgentAnalyzer uaa = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
        UserAgent agent = uaa.parse(user_agent);
        System.out.println(agent.toMap());
        return agent;
    }
}
