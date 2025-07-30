package bff.dao;

import bff.database.DatabaseConnection;
import bff.model.ParkingSpace;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpaceDAO {
    
    public List<ParkingSpace> findAll() throws SQLException {
        String sql = "SELECT id, name, status, updated_at FROM parking_spaces ORDER BY name";
        List<ParkingSpace> spaces = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ParkingSpace space = new ParkingSpace(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("status"),
                    rs.getTimestamp("updated_at").toLocalDateTime()
                );
                spaces.add(space);
            }
        }
        
        return spaces;
    }
    
    public ParkingSpace findById(Long id) throws SQLException {
        String sql = "SELECT id, name, status, updated_at FROM parking_spaces WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ParkingSpace(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("status"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
                return null;
            }
        }
    }
    
    public ParkingSpace findByName(String name) throws SQLException {
        String sql = "SELECT id, name, status, updated_at FROM parking_spaces WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ParkingSpace(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("status"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
                return null;
            }
        }
    }
    
    public void updateStatus(Long id, String status) throws SQLException {
        String sql = "UPDATE parking_spaces SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setLong(2, id);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("주차공간 상태 업데이트 실패: ID " + id);
            }
        }
    }
    
    public void initializeParkingSpaces() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM parking_spaces";
        String insertSql = "INSERT INTO parking_spaces (id, name, status) VALUES (?, ?, 'AVAILABLE')";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 ResultSet rs = checkStmt.executeQuery()) {
                
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        for (int i = 1; i <= 24; i++) {
                            insertStmt.setInt(1, i);  // ID 명시적 지정
                            insertStmt.setString(2, "A-" + String.format("%02d", i));
                            insertStmt.addBatch();
                        }
                        insertStmt.executeBatch();
                    }
                }
            }
        }
    }
}