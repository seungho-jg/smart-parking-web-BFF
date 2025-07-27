package bff;

public interface SessionManagerInterface extends Create, Read, Update, Delete {
}

interface Create {
    String create(String id, String nickname);
}

interface Read {
    String[] getAll();
    SessionInfo getOne(String sessionId);
}

interface Update {
    void grant(String sessionId, boolean b);
}

interface Delete {
    void delete(String sessionId);
    void cleanupExpired();
}
