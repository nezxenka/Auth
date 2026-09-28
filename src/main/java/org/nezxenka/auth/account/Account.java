package org.nezxenka.auth.account;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true)
public class Account {

    UUID uuid;
    String username;
    String passwordHash;
    long registeredAt;
    long lastLogin;
    long telegramId;
    String sessionIp;
    long sessionExpiry;

    public boolean isTelegramLinked() {
        return telegramId != 0L;
    }

    public boolean hasValidIpSession(String ip, long now) {
        return sessionIp != null && sessionIp.equals(ip) && now < sessionExpiry;
    }
}
