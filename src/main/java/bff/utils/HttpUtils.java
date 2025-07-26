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
}
