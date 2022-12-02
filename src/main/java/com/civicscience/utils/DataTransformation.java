package com.civicscience.utils;

import com.civicscience.entity.JotLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTransformation implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(DataTransformation.class);

    public JotLog mapToJotLogObject(String s) {
        JotLog log = new JotLog();
        List<String> list = splitTheString(s);

        log.setTs(list.get(1));
        log.setIp_v4(list.get(3).split(":")[0]);

        Map<String, List<String>> urlParts = transformURL(list.get(12));
        log.setId(urlParts.containsKey("j")?urlParts.get("j").get(0):null);
        log.setSequence(urlParts.containsKey("n")?Integer.valueOf(urlParts.get("n").get(0)):null);
        log.setSpace(urlParts.containsKey("s")?urlParts.get("s").get(0):null);
        log.setState(urlParts.containsKey("t")?urlParts.get("t").get(0):null);
        if(urlParts.containsKey("d")){
            Map<String, Object> dMap = extractDFields(urlParts.get("d").get(0));
            log.setTarget_id(dMap.containsKey("target") ? dMap.get("target").toString(): null);
            log.setAge(dMap.containsKey("a") ? Integer.valueOf(dMap.get("a").toString()): null);
            log.setGender(dMap.containsKey("g") ? Integer.valueOf(dMap.get("g").toString()): null);
            log.set_container_seen(dMap.containsKey("isContainerSeen") ? dMap.get("isContainerSeen").toString().equals(
                    "true"): false);
            log.setNatures(dMap.containsKey("natures") ? dMap.get("natures").toString(): null);
            log.setUser_alias(dMap.containsKey("alias") ? dMap.get("alias").toString(): null);
            log.setPlatform(dMap.containsKey("platform") ? dMap.get("platform").toString(): null);
            log.setUser_session(dMap.containsKey("session") ? dMap.get("session").toString(): null);
            log.setLocale(dMap.containsKey("locale") ? dMap.get("locale").toString() : null);
            log.setAskable(dMap.containsKey("askable") ? dMap.get("askable").toString(): null);
            log.setUsage(dMap.containsKey("usage") ? dMap.get("usage").toString(): null);
            log.setPosition(dMap.containsKey("position") ? Integer.valueOf(dMap.get("position").toString()) : null);
            log.setContext(dMap.containsKey("context") ? dMap.get("context").toString(): null);
        }

        UserAgent ua = transformUserAgent(list.get(13));
        if(ua != null){
            log.setUa_device_class(ua.getValue("DeviceClass"));
            log.setUa_device_family(ua.getValue("DeviceName"));
            log.setUa_os_family(ua.getValue("OperatingSystemName"));
            log.setUa_os_version(ua.getValue("OperatingSystemVersion"));
            log.setUa_browser_family(ua.getValue("AgentName"));
            log.setUa_browser_version(ua.getValue("AgentVersion"));
            log.setUa_is_mobile(ua.getValue("OperatingSystemClass").equals("Mobile"));
            log.setUa_is_bot(ua.getValue("DeviceClass").equals("Robot"));
        }
        return log;
    }

    public List<String> splitTheString(String s) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(s);
        while (m.find())
            list.add(m.group(1));
        return list;
    }

    public Map<String, List<String>> transformURL(String s) {
        String[] parts = s.split(" ");
        String url = parts[1];
        Map<String, List<String>> parameters = new QueryStringDecoder(url).parameters();
        return parameters;
    }

    public UserAgent transformUserAgent(String user_agent) {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
        UserAgent agent = uaa.parse(user_agent);
        return agent;
    }

    public Map<String, Object> extractDFields(String d_element) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map;
        try {
            map = mapper.readValue(d_element, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
