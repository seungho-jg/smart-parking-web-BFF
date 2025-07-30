package bff.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static HikariDataSource dataSource;
    private static boolean dbAvailable = false;
    
    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mariadb://192.168.0.51:3306/parkingsys");
            config.setUsername("seunho");
            config.setPassword("12345");
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
            
            // 연결 테스트
            try (Connection testConn = dataSource.getConnection()) {
                dbAvailable = true;
                System.out.println("데이터베이스 연결 성공");
            }
        } catch (Exception e) {
            dbAvailable = false;
            System.err.println("데이터베이스 연결 실패: " + e.getMessage());
            System.out.println("데이터베이스 없이 서버 시작 (일부 기능 제한)");
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (!dbAvailable) {
            throw new SQLException("데이터베이스를 사용할 수 없습니다");
        }
        return dataSource.getConnection();
    }
    
    public static boolean isDbAvailable() {
        return dbAvailable;
    }
    
    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}