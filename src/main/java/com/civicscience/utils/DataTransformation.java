package com.civicscience.utils;

import com.civicscience.entity.JotLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTransformation implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(DataTransformation.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Pattern pattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

  private static final UserAgentAnalyzer uaa = UserAgentAnalyzer
      .newBuilder()
      .hideMatcherLoadStats()
      .immediateInitialization()
      .withCache(100000)
      .build();

  /**
   * @param s - log line from file Example - h2 2023-01-04T23:55:00.103056Z
   *          app/web-PROD-ALB/05f72ee140d3036c 216.210.87.192:61376 172.18.3.190:80 0.001 0.001
   *          0.000 200 200 527 152 "GET
   *          https://www.civicscience.com:443/jot?j=2453065110.1774237028&n=2&s=poll&t=templates
   *          &d=%7B%22target%22%3A3832%2C%22natures%22%3A%5B%22ui-classic%22%2C%22ui-iframe
   *          %22%2C%22rootbeer-enabled%22%5D%2C%22instance%22%3A%22civsci-id-76398579-AA15U5tP
   *          %22%2C%22isContainerSeen%22%3Afalse%2C%22context%22%3A%22%2F%2Fwww.msn.com%2Fen-us
   *          %2Fmoney%2Fother%2Fshoppers-abandoning-walmart-after-they-implement-new-theft-deterrents
   *          %2Far-AA15U5tP%22%2C%22wx%22%3A0%2C%22wy%22%3A0%2C%22wh%22%3A694%2C%22ww%22%3A414
   *          %2C%22cx%22%3A0%2C%22cy%22%3A3322%2C%22comp%22%3Afalse%2C%22st%22%3A%22EVPP%22%2C
   *          %22stg%22%3A%22EVPP%3BEVVP%22%2C%22session%22%3A%2232c78720-8c8b-11ed-bb84-5c9584ad896b
   *          %22%2C%22locale%22%3A%22en%22%2C%22alias%22%3A%22cookie%2Ffb77166519aa298f45762e80d670b47d
   *          %22%7D HTTP/2.0" "Mozilla/5.0 (iPhone; CPU iPhone OS 16_1 like Mac OS X)
   *          AppleWebKit/605.1.15 (KHTML, like Gecko) GSA/242.1.493995244 Mobile/15E148
   *          Safari/604.1" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2
   *          arn:aws:elasticloadbalancing:us-east-1:825286309336:targetgroup/prod-120722-54662ea26b-Jot/91ef322131566140
   *          "Root=1-63b611d4-3b55a5e44382ab730d1faa11" "www.civicscience.com"
   *          "arn:aws:acm:us-east-1:825286309336:certificate/7adab1f2-f93a-43a1-938c-c5d35e4aeef6"
   *          215 2023-01-04T23:55:00.100000Z "waf,forward" "-" "-" "172.18.3.190:80" "200" "-" "-"
   * @return JotLog object
   */
  public JotLog mapToJotLogObject(String s) {
    JotLog log = new JotLog();
    //Generating hashcode for each log line, to catch any duplicates
    log.setHashCode(DigestUtils.sha256Hex(s));
    //Split the string to extract the values needed
    List<String> list = splitTheString(s);

    log.setTs(list.get(1)); //2023-01-04T23:55:00.103056Z
    log.setIp_v4(list.get(3).split(":")[0]); //216.210.87.192

    //Decodes get string from log, returns url parameters
    Map<String, List<String>> urlParts = transformURL(list.get(12));
    log.setId(urlParts.containsKey("j") ? urlParts.get("j").get(0) : null);
    log.setSequence(urlParts.containsKey("n") ? Integer.valueOf(urlParts.get("n").get(0)) : null);
    log.setSpace(urlParts.containsKey("s") ? urlParts.get("s").get(0) : null);
    log.setState(urlParts.containsKey("t") ? urlParts.get("t").get(0) : null);
    if (urlParts.containsKey("d")) {
      //D element fields are extracted and assigned to JotLog
      Map<String, Object> dMap = extractDFields(urlParts.get("d").get(0));
      if (dMap != null) {
        log.setTarget_id(dMap.containsKey("target") ? dMap.get("target").toString() : null);
        log.setAge(dMap.containsKey("a") ? Integer.valueOf(dMap.get("a").toString()) : null);
        log.setGender(dMap.containsKey("g") ? Integer.valueOf(dMap.get("g").toString()) : null);
        log.setIsContainerSeen(
            dMap.containsKey("isContainerSeen") ? (boolean) dMap.get("isContainerSeen") : null);
        log.setNatures(dMap.containsKey("natures") ? (List<String>) dMap.get("natures") : null);
        log.setUser_alias(dMap.containsKey("alias") ? dMap.get("alias").toString() : null);
        log.setPlatform(dMap.containsKey("platform") ? dMap.get("platform").toString() : null);
        log.setUser_session(dMap.containsKey("session") ? dMap.get("session").toString() : null);
        log.setLocale(dMap.containsKey("locale") ? dMap.get("locale").toString() : null);
        log.setAskable(dMap.containsKey("askable") ? dMap.get("askable").toString() : null);
        log.setUsage(dMap.containsKey("usage") ? dMap.get("usage").toString() : null);
        log.setPosition(
            dMap.containsKey("position") ? Integer.valueOf(dMap.get("position").toString()) : null);
        log.setSession_template(dMap.containsKey("st") ? dMap.get("st").toString() : null);
        log.setSession_template_group(dMap.containsKey("stg") ? dMap.get("stg").toString() : null);
        log.setMeta_target_id(dMap.containsKey("otarget") ? dMap.get("otarget").toString() : null);
        log.setConsent_accepted(
            dMap.containsKey("accepted") ? (boolean) dMap.get("accepted") : null);
        log.setAd_id(dMap.containsKey("id") ? dMap.get("id").toString() : null);
        if (dMap.containsKey("context")) {
          log.setContext(dMap.get("context").toString());
        }
      }
    }

    UserAgent ua = transformUserAgent(list.get(13));
    if (ua != null) {
      log.setUa_device_class(ua.getValue("DeviceClass"));
      log.setUa_device_family(ua.getValue("DeviceName"));
      log.setUa_os_family(ua.getValue("OperatingSystemName"));
      log.setUa_os_version(Objects.equals(ua.getValue("OperatingSystemVersion"), "??") ? "Unknown"
          : ua.getValue("OperatingSystemVersion"));
      log.setUa_browser_family(ua.getValue("AgentName"));
      log.setUa_browser_version(ua.getValue("AgentVersion"));
      log.setUa_is_mobile(ua.getValue("OperatingSystemClass").equals("Mobile"));
      log.setUa_is_bot(ua.getValue("DeviceClass").equals("Robot"));
    }
    return log;
  }

  /**
   * @param s - original jot log line
   * @return - list of strings, split from original string We are splitting on space, but not in
   * double quotes
   */
  public List<String> splitTheString(String s) {
    List<String> list = new ArrayList<>();
    Matcher m = pattern.matcher(s);
    while (m.find()) {
      list.add(m.group(1));
    }
    return list;
  }

  /**
   * @param s - GET string from Jot log line GET
   *          https://www.civicscience.com:443/jot?j=2453065110.1774237028&n=2&s=poll&t=templates
   *          &d=%7B%22target%22%3A3832%2C%22natures%22%3A%5B%22ui-classic%22%2C%22ui-iframe
   *          %22%2C%22rootbeer-enabled%22%5D%2C%22instance%22%3A%22civsci-id-76398579-AA15U5tP
   *          %22%2C%22isContainerSeen%22%3Afalse%2C%22context%22%3A%22%2F%2Fwww.msn.com%2Fen-us
   *          %2Fmoney%2Fother%2Fshoppers-abandoning-walmart-after-they-implement-new-theft-deterrents
   *          %2Far-AA15U5tP%22%2C%22wx%22%3A0%2C%22wy%22%3A0%2C%22wh%22%3A694%2C%22ww%22%3A414
   *          %2C%22cx%22%3A0%2C%22cy%22%3A3322%2C%22comp%22%3Afalse%2C%22st%22%3A%22EVPP%22%2C
   *          %22stg%22%3A%22EVPP%3BEVVP%22%2C%22session%22%3A%2232c78720-8c8b-11ed-bb84-5c9584ad896b
   *          %22%2C%22locale%22%3A%22en%22%2C%22alias%22%3A%22cookie%2Ffb77166519aa298f45762e80d670b47d
   *          %22%7D HTTP/2.0
   * @return Map of decoded values from URL
   */
  public Map<String, List<String>> transformURL(String s) {
    String[] parts = s.split(" ");
    String url = parts[1];
    return new QueryStringDecoder(url).parameters();
    /* Example {j=[2453065110.1774237028], n=[2], s=[poll],
     * t=[templates], d=[{"target":3832,"natures":["ui-classic","ui-iframe","rootbeer-enabled"],
     * "instance":"civsci-id-76398579-AA15U5tP","isContainerSeen":false,
     * "context":"//www.msn.com/en-us/money/other/shoppers-abandoning-walmart-after-they-implement-
     * new-theft-deterrents/ar-AA15U5tP","wx":0,"wy":0,"wh":694,"ww":414,"cx":0,"cy":3322,
     * "comp":false,"st":"EVPP","stg":"EVPP;EVVP","session":"32c78720-8c8b-11ed-bb84-5c9584ad896b",
     * "locale":"en","alias":"cookie/fb77166519aa298f45762e80d670b47d"}]} */
  }

  /**
   *
   * @param user_agent - from log line
   * Mozilla/5.0 (iPhone; CPU iPhone OS 16_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)
   *                  GSA/242.1.493995244 Mobile/15E148 Safari/604.1
   * @return UserAgent object from UserAgentAnalyzer
   */
  public UserAgent transformUserAgent(String user_agent) {
    return uaa.parse(user_agent);
  }

  /**
   *
   * @param d_element - d element in log
   * d=[{"target":3832,"natures":["ui-classic","ui-iframe","rootbeer-enabled"],
   * "instance":"civsci-id-76398579-AA15U5tP","isContainerSeen":false,
   * "context":"//www.msn.com/en-us/money/other/shoppers-abandoning-walmart-after-they-implement-
   *  new-theft-deterrents/ar-AA15U5tP","wx":0,"wy":0,"wh":694,"ww":414,"cx":0,"cy":3322,
   *  "comp":false,"st":"EVPP","stg":"EVPP;EVVP","session":"32c78720-8c8b-11ed-bb84-5c9584ad896b",
   *  "locale":"en","alias":"cookie/fb77166519aa298f45762e80d670b47d"}]
   * @return Map of key values from d element
   */
  public Map<String, Object> extractDFields(String d_element) {
    Map<String, Object> map = null;
    try {
      map = mapper.readValue(d_element, HashMap.class);
    } catch (JsonProcessingException e) {
      LOG.error("Exception while reading d element : {}", d_element);
    }
    return map;
  }
}
