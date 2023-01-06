package com.civicscience.utils;

import com.civicscience.entity.JotLog;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.QueryStringDecoder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DataTransformationTest {
    DataTransformation dt = new DataTransformation();
    String s = "h2 2022-11-30T00:11:29.309066Z app/web-PROD-ALB/05f72ee140d3036c 70.181.206.224:60561 172.18.2.48:80 " +
            "0.001 0.000 0.000 200 200 681 152 \"GET https://www.civicscience.com:443/jot?j=628319436" +
            ".1055353609&n=3&s=poll&t=served&d=%7B%22target%22%3A3815%2C%22natures%22%3A%5B%22ui-classic%22%2C%22ui" +
            "-iframe%22%2C%22rootbeer-enabled%22%5D%2C%22instance%22%3A%22civsci-id-76398579-AA14HOKH%22%2C" +
            "%22isContainerSeen%22%3Atrue%2C%22context%22%3A%22%2F%2Fwww.msn" +
            ".com%2Fen-us%2Fnews%2Fus%2Fi-m-looking-for-another-job-walmart-worker-says-manager-walked-into-the-break" +
            "-room-made-concerning-comment-about-manager-who-shot-employees-at-another-store%2Far-AA14HOKH%3Focid" +
            "%3Dmmx%26cvid%3D2809d0b01c264980963ea53d45bef1bb%22%2C%22wx%22%3A0%2C%22wy%22%3A3104%2C%22wh%22%3A844%2C" +
            "%22ww%22%3A428%2C%22cx%22%3A0%2C%22cy%22%3A3714%2C%22askable%22%3A%22398326%22%2C%22usage%22%3A%22pinned" +
            "%22%2C%22position%22%3A0%2C%22questions%22%3A%5B%22398326%22%5D%2C%22comp%22%3Atrue%2C%22session%22%3A" +
            "%2268c01d60-7043-11ed-8de6-3015f844650b%22%2C%22alias%22%3A%22cookie%2F5ce7f90d703ac86120844ff906ed1c5a" +
            "%22%2C%22locale%22%3A%22en%22%2C%22rec%22%3Afalse%7D HTTP/2.0\" \"Mozilla/5.0 (iPhone; CPU iPhone OS " +
            "16_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) EdgiOS/107.0.1418.52 Version/16.0 " +
            "Mobile/15E148 Safari/604.1\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2 " +
            "arn:aws:elasticloadbalancing:us-east-1:825286309336:targetgroup/prod-110722-9c6bd23c7d-Jot" +
            "/f9f3d9823e92227b \"Root=1-63869fb1-115d3e700e334696544dfa40\" \"www138.civicscience.com\" " +
            "\"arn:aws:acm:us-east-1:825286309336:certificate/7adab1f2-f93a-43a1-938c-c5d35e4aeef6\" 215 " +
            "2022-11-30T00:11:29.307000Z \"waf,forward\" \"-\" \"-\" \"172.18.2.48:80\" \"200\" \"-\" \"-\"";

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
        String s = "GET https://www.civicscience.com:443/jot?j=4076315079" +
                ".1018291929&n=8&s=poll&t=submit&d=%7B%22target%22%3A3818%2C%22natures%22%3A%5B%22ui-classic%22%2C" +
                "%22ui-iframe%22%2C%22rootbeer-enabled%22%5D%2C%22instance%22%3A%22civsci-id-76398579-AA14Hjea%22%2C" +
                "%22isContainerSeen%22%3Atrue%2C%22context%22%3A%22%2F%2Fwww.msn" +
                ".com%2Fen-us%2Fentertainment%2Fnews%2Fdisgraced-matt-lauer-s-trust-diminished-after-katie-couric-s" +
                "-tell-all-memoir-left-him-extremely-upset-source%2Far-AA14Hjea%3Fcvid" +
                "%3Dd5463f8f484847f78654697b8a3d048f%22%2C%22wx%22%3A0%2C%22wy%22%3A4300%2C%22wh%22%3A872%2C%22ww%22" +
                "%3A1920%2C%22cx%22%3A4%2C%22cy%22%3A4047%2C%22askable%22%3A%22161557%22%2C%22usage%22%3A%22value%22" +
                "%2C%22position%22%3A1%2C%22questions%22%3A%5B%22161557%22%5D%2C%22comp%22%3Atrue%2C%22session%22%3A" +
                "%222efa19f0-7043-11ed-bad1-7efeaa1f43b3%22%2C%22alias%22%3A%22cookie" +
                "%2Fdab73e100d21783af22e62bf2fd42e91%22%2C%22locale%22%3A%22en%22%2C%22rec%22%3Afalse%2C%22y%22%3A" +
                "%22_NA%22%7D HTTP/2.0";
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

    @Test
    public void dElementFieldExtractTest(){
        String d = "{\"target\":3818,\"natures\":[\"ui-classic\",\"ui-iframe\",\"rootbeer-enabled\"]," +
                "\"instance\":\"civsci-id-76398579-AA14Hjea\",\"isContainerSeen\":true,\"context\":\"//www.msn" +
                ".com/en-us/entertainment/news/disgraced-matt-lauer-s-trust-diminished-after-katie-couric-s-tell-all" +
                "-memoir-left-him-extremely-upset-source/ar-AA14Hjea?cvid=d5463f8f484847f78654697b8a3d048f\"," +
                "\"wx\":0,\"wy\":4300,\"wh\":872,\"ww\":1920,\"cx\":4,\"cy\":4047,\"askable\":\"161557\"," +
                "\"usage\":\"value\",\"position\":1,\"questions\":[\"161557\"],\"comp\":true," +
                "\"session\":\"2efa19f0-7043-11ed-bad1-7efeaa1f43b3\"," +
                "\"alias\":\"cookie/dab73e100d21783af22e62bf2fd42e91\",\"locale\":\"en\",\"rec\":false,\"y\":\"_NA\"}";
        Map<String, Object> map = dt.extractDFields(d);
    }
}
