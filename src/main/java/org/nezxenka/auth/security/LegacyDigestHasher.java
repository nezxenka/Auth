package org.nezxenka.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.util.Hex;

@RequiredArgsConstructor
public final class LegacyDigestHasher implements PasswordHasher {

    private final String algorithm;

    @Override
    public String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            return Hex.encode(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unsupported hash algorithm " + algorithm, exception);
        }
    }

    @Override
    public boolean verify(String password, String hash) {
        if (hash == null) {
            return false;
        }
        byte[] expected = hash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = hash(password).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }

    @Override
    public boolean supports(String hash) {
        return hash != null && !hash.startsWith("$");
    }
}
