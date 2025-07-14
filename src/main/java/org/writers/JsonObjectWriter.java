package org.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JsonObjectWriter {

    private JsonObjectWriter() {}

    private static final com.fasterxml.jackson.databind.ObjectWriter OBJECT_WRITER =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    static final Set<String> sensitiveFields = Set.of("characteristics");

    public static String writeValueAsJsonString(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.convertValue(object, new TypeReference<>() {});

            removeKeysRecursively(map, sensitiveFields);
            // Convert this object to JSON string using the static ObjectMapper
            return OBJECT_WRITER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public static void removeKeysRecursively(Map<String, Object> map, Set<String> keysToRemove) {
        if (map == null) return;

        Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            // Remove key if matches
            if (keysToRemove.contains(key)) {
                iter.remove();
                continue;
            }

            // If value is another Map, recurse
            if (value instanceof Map) {
                removeKeysRecursively((Map<String, Object>) value, keysToRemove);
            }
            // If value is a List, recurse into list elements
            else if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (item instanceof Map) {
                        removeKeysRecursively((Map<String, Object>) item, keysToRemove);
                    }
                }
            }
        }
    }
}
