package com.civicscience.utilstest;

import com.civicscience.entity.JotLog;
import com.civicscience.utils.DataTransformation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.QueryStringDecoder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class DataTransformationTest {
    DataTransformation dt = new DataTransformation();
    String s = "h2 2022-11-29T00:00:05.740140Z app/web-PROD-ALB/05f72ee140d3036c " +
            "67.44.208.22:36487 172.18.2.135:80 0.008 0.002 0.000 200 200 706 153 \"GET https://www" +
            ".civicscience.com:443/jot?j=711145199" +
            ".2962738547&n=0&s=poll&t=created&d=%7B%22target%22%3A%22dc47b0af-1755-c124-4d1b-758f0eee9014%22" +
            "%2C%22instance%22%3A%22civsci-id-76398579-AA14F9zA%22%2C%22isContainerSeen%22%3Afalse%2C" +
            "%22context%22%3A%22%2F%2Fwww.msn.com%2Fen-us%2Ftv%2Fnews%2Fdancing-with-the-stars-fans-can-t" +
            "-stop-freaking-out-over-tom-bergeron-s-return-to-tv%2Far-AA14F9zA%3Focid%3Dmsedgntp%26cvid" +
            "%3Dff4e15af52c94d639748d9dd430b7f6d%22%2C%22wx%22%3A0%2C%22wy%22%3A0%2C%22wh%22%3A610%2C%22ww" +
            "%22%3A1280%2C%22cx%22%3A4%2C%22cy%22%3A2155%7D HTTP/2.0\" \"Mozilla/5.0 (Windows NT 10.0; " +
            "Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0" +
            ".1418.56\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2 " +
            "arn:aws:elasticloadbalancing:us-east-1:825286309336:targetgroup/prod-110722-9c6bd23c7d-Jot" +
            "/f9f3d9823e92227b \"Root=1-63854b85-6509f70e18414ce8568ce716\" \"www.civicscience.com\" " +
            "\"arn:aws:acm:us-east-1:825286309336:certificate/7adab1f2-f93a-43a1-938c-c5d35e4aeef6\" 215 " +
            "2022-11-29T00:00:05.729000Z \"waf,forward\" \"-\" \"-\" \"172.18.2.135:80\" \"200\" \"-\" \"-\"";

    @Test
    public void testStringSplit() {

        List<String> list = dt.splitTheString(s);

        Assert.assertEquals(29, list.size());
    }
    @Test
    public void testTransformUserAgent(){
        UserAgent ua = dt.transformUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " +
                "like Gecko)" +
                " Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.56");
        Assert.assertEquals("Desktop", ua.getValue("DeviceClass"));
    }
    @Test
    public void testTransformURL() throws IOException {
        String s = "GET https://www.civicscience.com:443/jot?j=711145199.2962738547&n=0&s=poll&t=created&d=" +
                "%7B%22target%22%3A%22dc47b0af-1755-c124-4d1b-758f0eee9014%22%2C%22instance%22%3A%22" +
                "civsci-id-76398579-AA14F9zA%22%2C%22isContainerSeen%22%3Afalse%2C%22context%22%3A%22%2F%2" +
                "Fwww.msn.com%2Fen-us%2Ftv%2Fnews%2Fdancing-with-the-stars-fans-can-t-stop-freaking-out-over-tom-" +
                "bergeron-s-return-to-tv%2Far-AA14F9zA%3Focid%3Dmsedgntp%26cvid%3Dff4e15af52c94d639748d9dd430b7f6d%22%2C%22" +
                "wx%22%3A0%2C%22wy%22%3A0%2C%22wh%22%3A610%2C%22ww%22%3A1280%2C%22cx%22%3A4%2C%22cy%22%3A2155%7D " +
                "HTTP/2.0";
        String[] parts = s.split(" ");
        Map<String, List<String>> parameters = new QueryStringDecoder(parts[1]).parameters();
        System.out.println(parameters);
        System.out.println(parameters.get("d").get(0));
        System.out.println(new QueryStringDecoder(parts[1]).path());

    }

    @Test
    public void mapToObjectTest(){
        JotLog log = dt.mapToJotLogObject(s);

        System.out.println(log.toString());
    }
}
