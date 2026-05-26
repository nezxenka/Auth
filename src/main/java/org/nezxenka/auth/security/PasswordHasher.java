package org.nezxenka.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.nezxenka.auth.Auth;

public class PasswordHasher {

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(
                Auth.getInstance().getConfigManager().getHashAlgorithm()
            );
            byte[] hash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8)
            );
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifyPassword(String password, String hash) {
        String hashedPassword = hashPassword(password);
        return hashedPassword != null && hashedPassword.equals(hash);
    }
}
