package bff;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;



public class SessionManager implements SessionManagerInterface {
    // 세션 만료 시간 (2분)
    private static final long SESSION_OVER_MILLITS = 2 * 60 * 1000;
    private final Map<String, SessionInfo> sessions = new HashMap<>();


    @Override
    public synchronized String create(int id, String nickname) {
        // 세션 아이디 생성시간 + hash값
       String sessionId = Instant.now().toString() + System.identityHashCode(nickname);
       sessions.put(sessionId, new SessionInfo(id, nickname));
       return sessionId;
    }

    @Override
    public synchronized String[] getAll() {
        String[] res = new String[sessions.size()];
        // 이름만 배열로 내보내기
        int i = 0;
        for (SessionInfo info : sessions.values()){
            res[i++] = info.getNickname();
        }
        return res;
    }

    @Override
    public synchronized SessionInfo getOne(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        if (info != null && !info.isExpired(SESSION_OVER_MILLITS)) {
            return info;
        }
        sessions.remove(sessionId);
        return null;
    }

    @Override
    public synchronized void grant(String sessionsId, boolean b) {
        sessions.get(sessionsId).setDisabled(b);
    }

    @Override
    public synchronized void delete(String sessionsId) {
        sessions.remove(sessionsId);
    }

    @Override
    public synchronized void cleanupExpired() {
        sessions.forEach((key, value) -> {
            if (value.isExpired(SESSION_OVER_MILLITS)) {
                sessions.remove(key);
            }
        });
    }
}
