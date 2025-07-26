package bff;

import java.time.Instant;

public class SessionInfo {
    private final int id;
    private final String nickname;
    private final long createAt;
    private boolean disabled = false;

    public SessionInfo(int id, String nickname) {
        this.id = id;
        this.createAt = Instant.now().toEpochMilli();
        this.nickname = nickname;
    }
    public String getNickname() {
        return this.nickname;
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
