package com.tuantoquq.forwardmail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode jsonNode;

    public static String get(String json, String key) throws JsonProcessingException {
        jsonNode = mapper.readTree(json);
        return jsonNode.get("data").get(key).asText();
    }
}
