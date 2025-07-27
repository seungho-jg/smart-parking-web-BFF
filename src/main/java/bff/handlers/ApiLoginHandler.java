package bff.handlers;


import bff.SessionInfo;
import bff.SessionManager;
import bff.utils.CookieUtils;
import bff.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiLoginHandler implements Handler{
    private final SessionManager sessionManager;

    public ApiLoginHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(BufferedReader in,
                       BufferedWriter out,
                       String method,
                       String path) throws IOException {
        try {
            if (!method.equals("POST")) {
                HttpUtils.sendJsonError(out, 405, "Method not allowed");
                return;
            }
            // 헤더 파싱
            Map<String, String> headers = HttpUtils.parseHeaders(in);
            String contentLength = headers.get("content-length");
            if (contentLength == null) {
                HttpUtils.sendJsonError(out, 400, "Content-Length required");
                return;
            }
            // 요청 바디 읽기
            int length = Integer.parseInt(contentLength);
            char[] buffer = new char[length];
            in.read(buffer, 0, length);
            String body = new String(buffer);
            // JSON 파싱
            Map<String, String> loginData = parseLoginJson(body);
            String id = loginData.get("id");
            String password = loginData.get("password");
            System.out.println(id + password);

            if (id == null || password == null) {
                HttpUtils.sendJsonError(out, 400, "Username and password required");
                return;
            }
            // 사용자 인증
            boolean isValid = true;

            if (isValid) {
                // 세션 생성
                String sessionId = sessionManager.create("id", "test");

                // JWT 토큰 생성
                // 성공 응답
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("user", Map.of("username", "test"));

                HttpUtils.sendJsonResponse(out, 200, response, sessionId);
            } else {
                HttpUtils.sendJsonError(out, 401, "Invalid credentials");
            }

        } catch (Exception e) {
            HttpUtils.sendJsonError(out, 500, "Internal server error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private Map<String, String> parseLoginJson(String json) {
        Map<String, String> result = new HashMap<>();

        json = json.trim().replaceAll("[{}\"]", "");
        System.out.println("result: " + json);
        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                result.put(key, value);
            }
        }
        return result;
    }
}