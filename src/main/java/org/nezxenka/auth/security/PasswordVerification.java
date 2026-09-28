package org.nezxenka.auth.security;

public record PasswordVerification(boolean matched, boolean rehashRequired) {

    public static final PasswordVerification FAILED = new PasswordVerification(false, false);
}
