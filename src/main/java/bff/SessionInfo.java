package bff;

import java.time.Instant;

public class SessionInfo {
    private final String id;
    private final String name;
    private final long createAt;
    private boolean disabled = false;

    public SessionInfo(String id, String name) {
        this.id = id;
        this.createAt = Instant.now().toEpochMilli();
        this.name = name;
    }
    
    public String getUserId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    public long getCreateAt() {
        return this.createAt;
    }

    public boolean isExpired(long ttlMillis) {
        return Instant.now().toEpochMilli() - this.createAt > ttlMillis;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean b) {
        this.disabled = b;
    }
}
