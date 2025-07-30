package bff.model;

import java.time.LocalDateTime;

public class ParkingRecord {
    private Long id;
    private Long spaceId;
    private Long userId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer parkingFee;
    
    public ParkingRecord() {}
    
    public ParkingRecord(Long spaceId, Long userId) {
        this.spaceId = spaceId;
        this.userId = userId;
        this.entryTime = null;  // 라즈베리파이에서 설정
    }
    
    public ParkingRecord(Long id, Long spaceId, Long userId, LocalDateTime entryTime, 
                        LocalDateTime exitTime, Integer parkingFee) {
        this.id = id;
        this.spaceId = spaceId;
        this.userId = userId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.parkingFee = parkingFee;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSpaceId() {
        return spaceId;
    }
    
    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
    
    public Integer getParkingFee() {
        return parkingFee;
    }
    
    public void setParkingFee(Integer parkingFee) {
        this.parkingFee = parkingFee;
    }
}