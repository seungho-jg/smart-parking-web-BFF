package bff.handlers;

import bff.SessionManager;
import bff.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiParkingSpacesHandler implements Handler {
    SessionManager sessionManager;

    public ApiParkingSpacesHandler(SessionManager sessionManager){
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(BufferedReader in,
           BufferedWriter out,
           String method,
           String path) throws IOException{
        try {
            if (!method.equals("GET")) {
                HttpUtils.sendJsonError(out, 405, "Method not allowed");
                return;
            }
            // 테스트용 데이터 24개 생성
            List<Map<String, Object>> spaces = new ArrayList<>();
            for (int i = 1; i <= 24; i++) {
                Map<String, Object> space = new HashMap<>();
                space.put("id", i);
                space.put("name", "P" + i);
                space.put("floor", (i - 1) / 8 + 1); // 층수: 8개씩 1층, 2층, 3층
                space.put("status", i % 3 == 0 ? "reserved" : (i % 2 == 0 ? "occupied" : "available"));
                spaces.add(space);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("spaces", spaces);
            System.out.println(spaces);

            HttpUtils.sendJsonResponse(out, 200, response);
        } catch (Exception e) {
            HttpUtils.sendJsonError(out, 500, "Internal server error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
}
