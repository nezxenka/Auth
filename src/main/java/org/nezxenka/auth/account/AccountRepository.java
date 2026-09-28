package org.nezxenka.auth.account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<Account> findByUuid(UUID uuid);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByTelegramId(long telegramId);

    void insert(Account account);

    void updatePassword(UUID uuid, String passwordHash);

    void resetPassword(UUID uuid, String passwordHash);

    void updateLogin(UUID uuid, String username, long lastLogin, String sessionIp, long sessionExpiry);

    void updateLastLogin(UUID uuid, String username, long lastLogin);

    void updateUsername(UUID uuid, String username);
}
