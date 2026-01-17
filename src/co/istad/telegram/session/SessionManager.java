package co.istad.telegram.session;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final long SESSION_TIMEOUT_HOURS = 24;
    private final Map<Long, StaffSession> sessions;

    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public void createSession(Long chatId, String staffName) {
        StaffSession session = new StaffSession(staffName, LocalDateTime.now());
        sessions.put(chatId, session);
    }

    public boolean isAuthenticated(Long chatId) {
        StaffSession session = sessions.get(chatId);
        if (session == null) {
            return false;
        }

        if (isSessionExpired(session)) {
            sessions.remove(chatId);
            return false;
        }

        return true;
    }

    public String getStaffName(Long chatId) {
        StaffSession session = sessions.get(chatId);
        if (session != null && !isSessionExpired(session)) {
            return session.getStaffName();
        }
        return null;
    }

    public void removeSession(Long chatId) {
        sessions.remove(chatId);
    }

    public void refreshSession(Long chatId) {
        StaffSession session = sessions.get(chatId);
        if (session != null) {
            session.setLoginTime(LocalDateTime.now());
        }
    }

    private boolean isSessionExpired(StaffSession session) {
        return LocalDateTime.now().isAfter(
                session.getLoginTime().plusHours(SESSION_TIMEOUT_HOURS)
        );
    }

    public void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> isSessionExpired(entry.getValue()));
    }
}
