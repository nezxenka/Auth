package org.nezxenka.auth.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.nio.charset.StandardCharsets;

public final class BCryptPasswordHasher implements PasswordHasher {

    public static final int MAX_PASSWORD_BYTES = 72;

    private static final String PREFIX = "$2";
    private static final int COST_START = 4;
    private static final int COST_END = 6;

    private final BCrypt.Hasher hasher = BCrypt.withDefaults();
    private final BCrypt.Verifyer verifyer = BCrypt.verifyer();
    private final int cost;

    public BCryptPasswordHasher(int cost) {
        this.cost = cost;
    }

    public static boolean fits(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }

    @Override
    public String hash(String password) {
        return hasher.hashToString(cost, password.toCharArray());
    }

    @Override
    public boolean verify(String password, String hash) {
        if (!fits(password)) {
            return false;
        }
        try {
            return verifyer.verify(password.toCharArray(), hash).verified;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    @Override
    public boolean supports(String hash) {
        return hash != null && hash.startsWith(PREFIX);
    }

    public boolean needsRehash(String hash) {
        try {
            return Integer.parseInt(hash.substring(COST_START, COST_END)) != cost;
        } catch (RuntimeException exception) {
            return true;
        }
    }
}
