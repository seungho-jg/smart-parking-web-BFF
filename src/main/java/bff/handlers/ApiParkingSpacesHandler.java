package bff.handlers;

import bff.SessionManager;
import bff.socket.RaspberryClient;
import bff.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiParkingSpacesHandler implements Handler {
    private final RaspberryClient raspberryClient;

    public ApiParkingSpacesHandler(RaspberryClient raspberryClient){
        this.raspberryClient = raspberryClient;
    }

    @Override
    public void handle(BufferedReader in,
           BufferedWriter out,
           String method,
           String path) throws IOException{

        // 초기 데이터 24개 생성
        List<Map<String, Object>> spaces = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            Map<String, Object> space = new HashMap<>();
            space.put("id", i);
            space.put("name", "P" + i);
            space.put("floor", (i - 1) / 8 + 1); // 층수: 8개씩 1층, 2층, 3층
            space.put("status", "disable");
            spaces.add(space);
        }

        try {
            Map<String, String> headers = HttpUtils.parseHeaders(in);
            
            if (!method.equals("GET")) {
                HttpUtils.sendJsonError(out, 405, "Method not allowed", headers);
                return;
            }

            // 라즈베리파이에서 실시간 주차 데이터 가져오기
            spaces = getParkingDataFromRasp();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("spaces", spaces);
//            System.out.println(spaces);

            HttpUtils.sendJsonResponse(out, 200, response, null, headers);
        } catch (Exception e) {
            Map<String, String> headers = HttpUtils.parseHeaders(in);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "라즈베리파이 연결 오류, 기본 데이터 반환");
            response.put("spaces", spaces);

            HttpUtils.sendJsonResponse(out, 200, response, null, headers);
        } finally {
            out.flush();
        }

    }
    private List<Map<String, Object>> getParkingDataFromRasp() throws IOException {
        // 라즈베리파이에서 데이터 요청
        String rawData = raspberryClient.getParkingSpaceData();
        if (rawData == null || rawData.length() < 24) {
            throw new IOException("라즈베리파이에서 잘못된 데이터 수신: " + rawData);
        }
        List<Map<String, Object>> spaces = new ArrayList<>();

        // 24개 주차공간 데이터 파싱
        for (int i = 0; i < 24; i++) {
            char statusChar = rawData.charAt(i);

            Map<String, Object> space = new HashMap<>();
            space.put("id", i + 1);
            space.put("name", "P" + (i + 1));
            space.put("floor", (i / 8) + 1); // 8개씩 1층, 2층, 3층
            space.put("position", (i % 8) + 1); // 각 층에서의 위치

            // 상태 변환: 0=사용가능, 1=예약됨, 2=점유됨
            String status = convertStatusToString(statusChar);
            space.put("status", status);
            space.put("statusCode", String.valueOf(statusChar));

            spaces.add(space);
        }

        return spaces;
    }

    private String convertStatusToString(char statusCode) {
        return switch (statusCode) {
            case '0' -> "AVAILABLE";
            case '1' -> "RESERVED";
            case '2' -> "OCCUPIED";
            default -> {
                System.err.println("알 수 없는 상태 코드: " + statusCode);
                yield "UNKNOWN";
            }
        };
    }
}
