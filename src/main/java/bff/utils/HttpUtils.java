package bff.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {
    public static void sendHtmlResponse(BufferedWriter out, String html) throws IOException {
        byte[] bodyBytes = html.getBytes(StandardCharsets.UTF_8);

        // 응답 헤더
        out.write("HTTP/1.1 200 OK\r\n");
        out.write("Content-Type: text/html; charset=UTF-8\r\n");
        out.write("Content-Length: " + bodyBytes.length + "\r\n");
        out.write("\r\n");  // 헤더 끝, 빈 줄

        out.write(html);
    }
    public static void sendRedirect(BufferedWriter out, String location) throws IOException {
        out.write("HTTP/1.1 302 Found\r\n");
        out.write("Location: " + location + "\r\n");
        out.write("Content-Length: 0\r\n");
        out.write("\r\n");
    }

    public static void sendMethodNotAllowed(BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 405 Method Not Allowed\r\n");
        out.write("Allow: GET, POST\r\n");
        out.write("Content-Length: 0\r\n");
        out.write("\r\n");
    }
    public static void sendCustomResponse(BufferedWriter out, String msg) throws IOException {
        byte[] bodyBytes = msg.getBytes(StandardCharsets.UTF_8);
        out.write("HTTP/1.1 200 OK\r\n");
        out.write("Content-Type: text/plain; charset=UTF-8\r\n");
        out.write("Content-Length: " + bodyBytes.length + "\r\n");
        out.write("\r\n");
        out.write(msg);
    }
    public static Map<String,String> parseHeaders(BufferedReader in) throws IOException {
        Map<String,String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                String name  = line.substring(0, idx).trim().toLowerCase();
                String value = line.substring(idx + 1).trim();
                headers.put(name, value);
            }
        }
        return headers;
    }
    public static void sendJsonResponse(BufferedWriter out, int statusCode, Map<String, Object> data) throws IOException {
        String json = mapToJson(data);

        out.write("HTTP/1.1 " + statusCode + " OK\r\n");
        out.write("Content-Type: application/json; charset=utf-8\r\n");
        out.write("Content-Length: " + json.getBytes("UTF-8").length + "\r\n");
        out.write("Access-Control-Allow-Origin: *\r\n");
        out.write("Access-Control-Allow-Methods: GET, POST, PUT, DELETE\r\n");
        out.write("Access-Control-Allow-Headers: Content-Type, Authorization\r\n");
        out.write("\r\n");
        out.write(json);
    }

    public static void sendJsonResponse(BufferedWriter out, int statusCode, Map<String, Object> data, String sessionId) throws IOException {
        String json = mapToJson(data);

        out.write("HTTP/1.1 " + statusCode + " OK\r\n");
        out.write("Set-Cookie: SESSIONID=" + sessionId + "; HttpOnly; Path=/\r\n");
        out.write("Content-Type: application/json; charset=utf-8\r\n");
        out.write("Content-Length: " + json.getBytes("UTF-8").length + "\r\n");
        out.write("Access-Control-Allow-Origin: *\r\n");
        out.write("Access-Control-Allow-Methods: GET, POST, PUT, DELETE\r\n");
        out.write("Access-Control-Allow-Headers: Content-Type, Authorization\r\n");
        out.write("\r\n");
        out.write(json);
    }
    public static void sendJsonError(BufferedWriter out, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);

        sendJsonResponse(out, statusCode, error);
    }
    public static String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Boolean) {
                json.append(value.toString());
            } else if (value instanceof Map) {
                json.append(mapToJson((Map<String, Object>) value));
            } else if (value instanceof java.util.List) {
                json.append(listToJson((java.util.List<?>) value));
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
            first = false;
        }

        json.append("}");
        System.out.println(json);
        return json.toString();
    }
    private static String listToJson(java.util.List<?> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        for (Object item : list) {
            if (!first) {
                json.append(",");
            }
            json.append(mapToJson((Map<String, Object>) item));
            first = false;
        }

        json.append("]");
        return json.toString();
    }
}
