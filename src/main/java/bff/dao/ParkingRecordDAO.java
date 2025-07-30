package bff.dao;

import bff.database.DatabaseConnection;
import bff.model.ParkingRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParkingRecordDAO {
    
    public ParkingRecord createRecord(ParkingRecord record) throws SQLException {
        String sql = "INSERT INTO parking_records (space_id, user_id, entry_time) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, record.getSpaceId());
            stmt.setLong(2, record.getUserId());
            stmt.setTimestamp(3, null);  // entry_time은 NULL로 설정 (라즈베리파이가 업데이트)
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("주차기록 생성 실패");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    record.setId(generatedKeys.getLong(1));
                    return record;
                } else {
                    throw new SQLException("ID 생성 실패");
                }
            }
        }
    }
    
    public void updateExitTime(Long recordId, LocalDateTime exitTime, Integer parkingFee) throws SQLException {
        String sql = "UPDATE parking_records SET exit_time = ?, parking_fee = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(exitTime));
            stmt.setInt(2, parkingFee);
            stmt.setLong(3, recordId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("주차기록 업데이트 실패: ID " + recordId);
            }
        }
    }
    
    public List<ParkingRecord> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT id, space_id, user_id, entry_time, exit_time, parking_fee " +
                    "FROM parking_records WHERE user_id = ? ORDER BY entry_time DESC";
        List<ParkingRecord> records = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ParkingRecord record = new ParkingRecord();
                    record.setId(rs.getLong("id"));
                    record.setSpaceId(rs.getLong("space_id"));
                    record.setUserId(rs.getLong("user_id"));
                    record.setEntryTime(rs.getTimestamp("entry_time").toLocalDateTime());
                    
                    Timestamp exitTimestamp = rs.getTimestamp("exit_time");
                    if (exitTimestamp != null) {
                        record.setExitTime(exitTimestamp.toLocalDateTime());
                    }
                    
                    Integer fee = rs.getObject("parking_fee", Integer.class);
                    record.setParkingFee(fee);
                    
                    records.add(record);
                }
            }
        }
        
        return records;
    }
    
    public ParkingRecord findActiveRecordByUserId(Long userId) throws SQLException {
        String sql = "SELECT id, space_id, user_id, entry_time, exit_time, parking_fee " +
                    "FROM parking_records WHERE user_id = ? AND exit_time IS NULL " +
                    "ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ParkingRecord record = new ParkingRecord();
                    record.setId(rs.getLong("id"));
                    record.setSpaceId(rs.getLong("space_id"));
                    record.setUserId(rs.getLong("user_id"));
                    
                    Timestamp entryTimestamp = rs.getTimestamp("entry_time");
                    if (entryTimestamp != null) {
                        record.setEntryTime(entryTimestamp.toLocalDateTime());
                    }
                    
                    return record;
                }
                return null;
            }
        }
    }
}