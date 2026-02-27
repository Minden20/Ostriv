package org.example.ostriv.ostriv.utill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleJsonParser {

    public static class JsonObject {
        private final Map<String, Object> map = new HashMap<>();

        public void put(String key, Object value) {
            map.put(key, value);
        }

        public Object get(String key) {
            return map.get(key);
        }

        public boolean containsKey(String key) {
            return map.containsKey(key);
        }

        public Map<String, Object> getMap() {
            return map;
        }
    }

    public static List<JsonObject> parseArray(String json) {
        List<JsonObject> result = new ArrayList<>();
        if (json == null || json.trim().isEmpty())
            return result;
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]"))
            return result;

        String inner = json.substring(1, json.length() - 1).trim();
        if (inner.isEmpty())
            return result;

        List<String> objectStrings = splitRespectingQuotesAndBraces(inner, ',');
        for (String objStr : objectStrings) {
            JsonObject obj = parseObject(objStr);
            if (obj != null)
                result.add(obj);
        }
        return result;
    }

    public static JsonObject parseObject(String json) {
        if (json == null)
            return null;
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}"))
            return null;

        JsonObject obj = new JsonObject();
        String inner = json.substring(1, json.length() - 1).trim();
        if (inner.isEmpty())
            return obj;

        List<String> pairs = splitRespectingQuotesAndBraces(inner, ',');

        for (String pair : pairs) {
            int colonIndex = findSeparatorRaw(pair, ':');
            if (colonIndex == -1)
                continue;

            String keyStr = pair.substring(0, colonIndex).trim();
            String valueStr = pair.substring(colonIndex + 1).trim();

            String key = unescapeString(keyStr);
            // Remove surrounding quotes from key if unescapeString didn't
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = unescapeString(key);
            }

            Object value = parseValue(valueStr);
            obj.put(key, value);
        }
        return obj;
    }

    private static Object parseValue(String valueStr) {
        if (valueStr == null)
            return null;
        valueStr = valueStr.trim();

        if ("null".equals(valueStr)) {
            return null;
        }
        if (valueStr.startsWith("{") && valueStr.endsWith("}")) {
            return parseObject(valueStr);
        }
        if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
            return parseArray(valueStr);
        }
        if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return unescapeString(valueStr);
        } else if (valueStr.matches("-?\\d+")) {
            return Long.valueOf(valueStr);
        } else if (valueStr.matches("-?\\d+\\.\\d+")) {
            return Double.valueOf(valueStr);
        } else if ("true".equalsIgnoreCase(valueStr) || "false".equalsIgnoreCase(valueStr)) {
            return Boolean.valueOf(valueStr);
        }
        return valueStr;
    }

    private static List<String> splitRespectingQuotesAndBraces(String input, char delimiter) {
        List<String> chunks = new ArrayList<>();
        int braceCount = 0;
        int bracketCount = 0;
        boolean inQuote = false;
        int start = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\"' && (i == 0 || input.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }

            if (!inQuote) {
                switch (c) {
                    case '{' -> braceCount++;
                    case '}' -> braceCount--;
                    case '[' -> bracketCount++;
                    case ']' -> bracketCount--;
                    default -> {
                    }
                }

                if (braceCount == 0 && bracketCount == 0 && c == delimiter) {
                    chunks.add(input.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }
        if (start < input.length()) {
            chunks.add(input.substring(start).trim());
        } else if (start == input.length() && !chunks.isEmpty()) {
        }
        return chunks;
    }

    private static int findSeparatorRaw(String pair, char separator) {
        boolean inQuote = false;
        for (int i = 0; i < pair.length(); i++) {
            char c = pair.charAt(i);
            if (c == '\"' && (i == 0 || pair.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }
            if (!inQuote && c == separator)
                return i;
        }
        return -1;
    }

    private static String unescapeString(String quoted) {
        if (quoted == null)
            return null;
        if (quoted.startsWith("\"") && quoted.endsWith("\"") && quoted.length() >= 2) {
            quoted = quoted.substring(1, quoted.length() - 1);
        }
        return quoted.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r");
    }

    public static String toJsonString(JsonObject obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : obj.map.entrySet()) {
            if (!first)
                sb.append(",");
            sb.append("\"").append(escapeString(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof String || value instanceof java.util.UUID || value instanceof Enum) {
                sb.append("\"").append(escapeString(value.toString())).append("\"");
            } else if (value == null) {
                sb.append("null");
            } else {
                sb.append(value);
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    public static String arrayToJsonString(List<JsonObject> objects) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < objects.size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(toJsonString(objects.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeString(String input) {
        if (input == null)
            return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
