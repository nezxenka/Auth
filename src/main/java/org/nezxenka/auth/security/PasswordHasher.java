package org.nezxenka.auth.security;

public interface PasswordHasher {

    String hash(String password);

    boolean verify(String password, String hash);

    boolean supports(String hash);
}
