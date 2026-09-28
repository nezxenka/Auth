package org.nezxenka.auth.security;

import org.nezxenka.auth.config.settings.PasswordSettings;

public final class PasswordService {

    private volatile BCryptPasswordHasher primary;
    private volatile LegacyDigestHasher legacy;

    public PasswordService(PasswordSettings settings) {
        configure(settings);
    }

    public void configure(PasswordSettings settings) {
        primary = new BCryptPasswordHasher(settings.bcryptCost());
        legacy = new LegacyDigestHasher(settings.legacyAlgorithm());
    }

    public String hash(String password) {
        return primary.hash(password);
    }

    public PasswordVerification verify(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isEmpty()) {
            return PasswordVerification.FAILED;
        }
        BCryptPasswordHasher bcrypt = primary;
        if (bcrypt.supports(storedHash)) {
            boolean matched = bcrypt.verify(password, storedHash);
            return new PasswordVerification(matched, matched && bcrypt.needsRehash(storedHash));
        }
        boolean matched = legacy.verify(password, storedHash);
        return new PasswordVerification(matched, matched);
    }
}
