package bff.handlers;

import bff.dao.UserDAO;
import bff.model.User;
import bff.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ApiRegisterHandler implements Handler {
    private final UserDAO userDAO;

    public ApiRegisterHandler() {
        this.userDAO = new UserDAO();
    }

    @Override
    public void handle(BufferedReader in, BufferedWriter out, String method, String path) throws IOException {
        try {
            if (!method.equals("POST")) {
                HttpUtils.sendJsonError(out, 405, "Method not allowed");
                return;
            }

            Map<String, String> headers = HttpUtils.parseHeaders(in);
            String contentLength = headers.get("content-length");
            if (contentLength == null) {
                HttpUtils.sendJsonError(out, 400, "Content-Length required");
                return;
            }

            int length = Integer.parseInt(contentLength);
            char[] buffer = new char[length];
            in.read(buffer, 0, length);
            String body = new String(buffer);

            Map<String, String> registerData = parseRegisterJson(body);
            String loginId = registerData.get("id");
            String password = registerData.get("password");
            String name = registerData.get("name");
            String vehicleNumber = registerData.get("vehicle_number");

            if (loginId == null || password == null || name == null || vehicleNumber == null) {
                HttpUtils.sendJsonError(out, 400, "All fields are required");
                return;
            }

            try {
                if (userDAO.existsByLoginId(loginId)) {
                    HttpUtils.sendJsonError(out, 409, "Login ID already exists");
                    return;
                }

                if (userDAO.existsByVehicleNumber(vehicleNumber)) {
                    HttpUtils.sendJsonError(out, 409, "Vehicle number already exists");
                    return;
                }

                User newUser = new User(loginId, password, name, vehicleNumber);
                User createdUser = userDAO.createUser(newUser);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Registration successful");
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", createdUser.getId());
                userInfo.put("loginId", createdUser.getLoginId());
                userInfo.put("name", createdUser.getName());
                userInfo.put("vehicleNumber", createdUser.getVehicleNumber());
                response.put("user", userInfo);

                HttpUtils.sendJsonResponse(out, 201, response, null);

            } catch (SQLException e) {
                HttpUtils.sendJsonError(out, 500, "Database error: " + e.getMessage());
            }

        } catch (Exception e) {
            HttpUtils.sendJsonError(out, 500, "Internal server error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private Map<String, String> parseRegisterJson(String json) {
        Map<String, String> result = new HashMap<>();

        json = json.trim().replaceAll("[{}\"]", "");
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