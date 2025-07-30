package bff.handlers;

import bff.SessionInfo;
import bff.SessionManager;
import bff.dao.UserDAO;
import bff.model.User;
import bff.utils.CookieUtils;
import bff.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ApiLoginHandler implements Handler{
    private final SessionManager sessionManager;
    private final UserDAO userDAO;

    public ApiLoginHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.userDAO = new UserDAO();
    }

    @Override
    public void handle(BufferedReader in,
                       BufferedWriter out,
                       String method,
                       String path) throws IOException {
        try {
            // 헤더 파싱
            Map<String, String> headers = HttpUtils.parseHeaders(in);
            
            if (!method.equals("POST")) {
                HttpUtils.sendJsonError(out, 405, "Method not allowed", headers);
                return;
            }
            String contentLength = headers.get("content-length");
            if (contentLength == null) {
                HttpUtils.sendJsonError(out, 400, "Content-Length required", headers);
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
                HttpUtils.sendJsonError(out, 400, "Username and password required", headers);
                return;
            }
            // 사용자 인증
            try {
                User user = userDAO.findByLoginId(id);
                if (user != null && user.getPassword().equals(password)) {
                    // 세션 생성
                    String sessionId = sessionManager.create(String.valueOf(user.getId()), user.getName());

                    // 성공 응답
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Login successful");
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("loginId", user.getLoginId());
                    userInfo.put("name", user.getName());
                    userInfo.put("vehicleNumber", user.getVehicleNumber());
                    response.put("user", userInfo);

                    HttpUtils.sendJsonResponse(out, 200, response, sessionId, headers);
                } else {
                    HttpUtils.sendJsonError(out, 401, "Invalid credentials", headers);
                }
            } catch (SQLException e) {
                HttpUtils.sendJsonError(out, 500, "Database error: " + e.getMessage(), headers);
            }

        } catch (Exception e) {
            HttpUtils.sendJsonError(out, 500, "Internal server error: " + e.getMessage(), null);
        } finally {
            out.flush();
        }
    }

    private Map<String, String> parseLoginJson(String json) {
        Map<String, String> result = new HashMap<>();

        json = json.trim().replaceAll("[{}\"]", "");
//        System.out.println("result: " + json);
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