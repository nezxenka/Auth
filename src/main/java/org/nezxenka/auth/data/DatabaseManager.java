package org.nezxenka.auth.data;

public interface DatabaseManager {
    void createTable();
    void createTelegramTable();
    boolean isRegistered(String uuid);
    void register(String uuid, String username, String password);
    String getPassword(String uuid);
    void updatePassword(String uuid, String password);
    void updateLastLogin(String uuid);
    void saveLinkCode(String uuid, String code);
    String getLinkCode(String uuid);
    void linkTelegram(String uuid, long telegramId);
    boolean isTelegramLinked(long telegramId);
    boolean isTelegramLinkedByUUID(String uuid);
    Long getTelegramId(String uuid);
    String getUUIDByTelegramId(long telegramId);
    void unlinkTelegram(String uuid);
    void removeLinkCode(String uuid);
    boolean is2FAEnabled(String uuid);
    void set2FAEnabled(String uuid, boolean enabled);
    void saveSession(String uuid, String ip, long expiry);
    String getSessionIp(String uuid);
    long getSessionExpiry(String uuid);
    void clearSession(String uuid);
    void shutdown();
}
