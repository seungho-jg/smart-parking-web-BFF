package bff.handlers;

import bff.SessionManager;
import bff.dao.ParkingRecordDAO;
import bff.dao.ParkingSpaceDAO;
import bff.model.ParkingRecord;
import bff.model.ParkingSpace;
import bff.utils.CookieUtils;
import bff.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.spec.RSAOtherPrimeInfo;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiParkingRecordsHandler implements Handler {
    private final SessionManager sessionManager;
    private final ParkingRecordDAO parkingRecordDAO;
    private final ParkingSpaceDAO parkingSpaceDAO;

    public ApiParkingRecordsHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.parkingRecordDAO = new ParkingRecordDAO();
        this.parkingSpaceDAO = new ParkingSpaceDAO();
    }

    @Override
    public void handle(BufferedReader in, BufferedWriter out, String method, String path) throws IOException {
        try {
            Map<String, String> headers = HttpUtils.parseHeaders(in);
            
            if (!method.equals("GET")) {
                HttpUtils.sendJsonError(out, 405, "Method not allowed", headers);
                return;
            }
            String sessionId = CookieUtils.parse(headers.get("cookie"), "SESSIONID");
            
            if (sessionId == null || sessionId.isEmpty() || !sessionManager.isValid(sessionId)) {
                HttpUtils.sendJsonError(out, 401, "Authentication required", headers);
                return;
            }

            Long userId = Long.parseLong(sessionManager.get(sessionId).getUserId());
            String userName = sessionManager.get(sessionId).getName();
            System.out.println(userName);

            try {
                List<ParkingRecord> records = parkingRecordDAO.findByUserId(userId);
                List<Map<String, Object>> recordList = new ArrayList<>();

                for (ParkingRecord record : records) {
                    Map<String, Object> recordMap = new HashMap<>();
                    recordMap.put("id", record.getId());
                    recordMap.put("spaceId", record.getSpaceId());
                    
                    try {
                        ParkingSpace space = parkingSpaceDAO.findById(record.getSpaceId());
                        recordMap.put("spaceName", space != null ? space.getName() : "Unknown");
                    } catch (SQLException e) {
                        recordMap.put("spaceName", "Unknown");
                    }
                    
                    recordMap.put("entryTime", record.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    
                    if (record.getExitTime() != null) {
                        recordMap.put("exitTime", record.getExitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else {
                        recordMap.put("exitTime", null);
                    }
                    
                    recordMap.put("parkingFee", record.getParkingFee());
                    recordMap.put("status", record.getExitTime() != null ? "completed" : "active");
                    
                    recordList.add(recordMap);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("records", recordList);
                response.put("totalCount", recordList.size());

                HttpUtils.sendJsonResponse(out, 200, response, null, headers);

            } catch (SQLException e) {
                HttpUtils.sendJsonError(out, 500, "Database error: " + e.getMessage(), headers);
            }

        } catch (Exception e) {
            HttpUtils.sendJsonError(out, 500, "Internal server error: " + e.getMessage(), null);
        } finally {
            out.flush();
        }
    }

}