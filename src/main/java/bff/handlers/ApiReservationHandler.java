package bff.handlers;

import bff.SessionManager;
import bff.dao.ParkingSpaceDAO;
import bff.dao.ParkingRecordDAO;
import bff.model.ParkingSpace;
import bff.model.ParkingRecord;
import bff.socket.RaspberryClient;
import bff.utils.CookieUtils;
import bff.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ApiReservationHandler implements Handler {
    private final SessionManager sessionManager;
    private final ParkingSpaceDAO parkingSpaceDAO;
    private final ParkingRecordDAO parkingRecordDAO;
    private final RaspberryClient raspberryClient;

    public ApiReservationHandler(SessionManager sessionManager, RaspberryClient raspberryClient) {
        this.sessionManager = sessionManager;
        this.parkingSpaceDAO = new ParkingSpaceDAO();
        this.parkingRecordDAO = new ParkingRecordDAO();
        this.raspberryClient = raspberryClient;
    }

    @Override
    public void handle(BufferedReader in, BufferedWriter out, String method, String path) throws IOException {
        try {
            if (!method.equals("POST")) {
                HttpUtils.sendJsonError(out, 405, "Method not allowed");
                return;
            }

            Map<String, String> headers = HttpUtils.parseHeaders(in);
            String sessionId = CookieUtils.parse(headers.get("cookie"), "SESSIONID");
            
            // 디버깅 로그
            System.out.println("=== 인증 디버깅 ===");
            System.out.println("Cookie Header: " + headers.get("cookie"));
            System.out.println("Extracted SessionId: " + sessionId);
            System.out.println("SessionManager isValid: " + (sessionId != null ? sessionManager.isValid(sessionId) : "sessionId is null"));
            
            if (sessionId == null || sessionId.isEmpty() || !sessionManager.isValid(sessionId)) {
                HttpUtils.sendJsonError(out, 401, "Authentication required");
                return;
            }

            String contentLength = headers.get("content-length");
            if (contentLength == null) {
                HttpUtils.sendJsonError(out, 400, "Content-Length required");
                return;
            }

            int length = Integer.parseInt(contentLength);
            char[] buffer = new char[length];
            in.read(buffer, 0, length);
            String body = new String(buffer);

            Map<String, String> requestData = parseReservationJson(body);
            String action = requestData.get("action");
            String spaceIdStr = requestData.get("spaceId");

            if (action == null || spaceIdStr == null) {
                HttpUtils.sendJsonError(out, 400, "Action and spaceId are required", headers);
                return;
            }

            Long spaceId = Long.parseLong(spaceIdStr);
            Long userId = Long.parseLong(sessionManager.get(sessionId).getUserId());

            try {
                if ("reserve".equals(action)) {
                    handleReservation(out, spaceId, userId, headers);
                } else if ("cancel".equals(action)) {
                    handleCancellation(out, spaceId, userId);
                } else {
                    HttpUtils.sendJsonError(out, 400, "Invalid action", headers);
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

    private void handleReservation(BufferedWriter out, Long spaceId, Long userId, Map<String, String> headers) throws SQLException, IOException {
        ParkingSpace space = parkingSpaceDAO.findById(spaceId);

        if (space == null) {
            HttpUtils.sendJsonError(out, 404, "Parking space not found", headers);
            return;
        }

        if (!"AVAILABLE".equals(space.getStatus())) {
            HttpUtils.sendJsonError(out, 409, "Parking space not available", headers);
            return;
        }

        ParkingRecord activeRecord = parkingRecordDAO.findActiveRecordByUserId(userId);
        if (activeRecord != null) {
            HttpUtils.sendJsonError(out, 409, "User already has an active reservation", headers);
            return;
        }

        try {
            int spaceNumber = Integer.parseInt(space.getName().split("-")[1]);
            boolean raspberrySuccess = raspberryClient.reserveParkingSpace(spaceNumber);
            
            if (raspberrySuccess) {
                parkingSpaceDAO.updateStatus(spaceId, "RESERVED");
                
                // 예약 기록 생성 (entry_time은 NULL - 라즈베리파이가 센서 감지시 업데이트)
                ParkingRecord record = new ParkingRecord(spaceId, userId);
                parkingRecordDAO.createRecord(record);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Reservation successful - Please park your car");
                response.put("spaceId", spaceId);
                response.put("spaceName", space.getName());

                HttpUtils.sendJsonResponse(out, 200, response, null);
            } else {
                HttpUtils.sendJsonError(out, 500, "Failed to communicate with parking system");
            }
        } catch (IOException e) {
            HttpUtils.sendJsonError(out, 500, "Parking system communication error: " + e.getMessage());
        }
    }

    private void handleCancellation(BufferedWriter out, Long spaceId, Long userId) throws SQLException, IOException {
        ParkingRecord activeRecord = parkingRecordDAO.findActiveRecordByUserId(userId);
        if (activeRecord == null || !activeRecord.getSpaceId().equals(spaceId)) {
            HttpUtils.sendJsonError(out, 404, "No active reservation found for this space");
            return;
        }

        // 이미 차량이 들어온 경우 취소 불가
        if (activeRecord.getEntryTime() != null) {
            HttpUtils.sendJsonError(out, 409, "Cannot cancel - Vehicle already parked");
            return;
        }

        ParkingSpace space = parkingSpaceDAO.findById(spaceId);
        if (space == null) {
            HttpUtils.sendJsonError(out, 404, "Parking space not found");
            return;
        }

        try {
            int spaceNumber = Integer.parseInt(space.getName().split("-")[1]);
            boolean raspberrySuccess = raspberryClient.cancelParkingSpace(spaceNumber);
            
            if (raspberrySuccess) {
                parkingSpaceDAO.updateStatus(spaceId, "AVAILABLE");
                
                // 예약만 취소 (entry_time이 NULL이므로 기록 삭제)
                parkingRecordDAO.updateExitTime(activeRecord.getId(), LocalDateTime.now(), 0);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Reservation cancelled successfully");
                response.put("spaceId", spaceId);
                response.put("spaceName", space.getName());

                HttpUtils.sendJsonResponse(out, 200, response, null);
            } else {
                HttpUtils.sendJsonError(out, 500, "Failed to communicate with parking system");
            }
        } catch (IOException e) {
            HttpUtils.sendJsonError(out, 500, "Parking system communication error: " + e.getMessage());
        }
    }


    private Map<String, String> parseReservationJson(String json) {
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