package com.civicscience.datapipeline;

import com.esotericsoftware.kryo.util.ObjectMap;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.flink.api.common.serialization.Encoder;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

public class JotEncoder implements Encoder {

  private ObjectMapper mapper =new ObjectMapper();
  @Override
  public void encode(Object element, OutputStream stream) throws IOException {

    stream.write(mapper.writeValueAsString(element).getBytes(StandardCharsets.UTF_8));
    stream.write('\n');
  }
}
