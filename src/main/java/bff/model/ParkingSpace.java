package bff.model;

import java.time.LocalDateTime;

public class ParkingSpace {
    private Long id;
    private String name;
    private String status;
    private LocalDateTime updatedAt;

    public ParkingSpace(String name, String status) {
        this.name = name;
        this.status = status;
    }
    
    public ParkingSpace(Long id, String name, String status, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}