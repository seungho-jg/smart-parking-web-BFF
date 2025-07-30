package bff.dao;

import bff.database.DatabaseConnection;
import bff.model.User;

import java.sql.*;

public class UserDAO {
    
    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (login_id, password, name, vehicle_number) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getLoginId());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getVehicleNumber());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("회원가입 실패");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                    return user;
                } else {
                    throw new SQLException("ID 생성 실패");
                }
            }
        }
    }
    
    public User findByLoginId(String loginId) throws SQLException {
        String sql = "SELECT id, login_id, password, name, vehicle_number FROM users WHERE login_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, loginId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getLong("id"),
                        rs.getString("login_id"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("vehicle_number")
                    );
                }
                return null;
            }
        }
    }
    
    public boolean existsByLoginId(String loginId) throws SQLException {
        return findByLoginId(loginId) != null;
    }
    
    public boolean existsByVehicleNumber(String vehicleNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE vehicle_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, vehicleNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
}