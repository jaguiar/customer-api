package com.prez.ws.model;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ListOfObjectsToMapDeserializer extends JsonDeserializer<Map<String, String>> {

  @Override
  public Map<String, String> deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {
    Map<String, String> ret = new HashMap<>();

    ObjectCodec codec = parser.getCodec();
    TreeNode node = codec.readTree(parser);

    if (node.isArray()) {
      for (JsonNode n : (ArrayNode) node) {
        JsonNode key = n.get("key");
        JsonNode value = n.get("value");
        if (key != null && value != null) {
          String keyAsText = key.asText();
          String valueAsText = value.asText();
          if (isNotEmpty(keyAsText) && isNotEmpty(valueAsText)) {
            ret.put(keyAsText, valueAsText);
          }
        }
      }
    }
    return ret;
  }
}