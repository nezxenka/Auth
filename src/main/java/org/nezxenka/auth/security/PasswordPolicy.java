package org.nezxenka.auth.security;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.PasswordSettings;
import org.nezxenka.auth.message.Placeholder;

@RequiredArgsConstructor
public final class PasswordPolicy {

    private final ConfigService config;

    public Optional<PasswordViolation> check(String password) {
        PasswordSettings settings = settings();
        if (password.length() < settings.minLength()) {
            return Optional.of(PasswordViolation.TOO_SHORT);
        }
        if (password.length() > settings.maxLength() || !BCryptPasswordHasher.fits(password)) {
            return Optional.of(PasswordViolation.TOO_LONG);
        }
        return Optional.empty();
    }

    public Placeholder[] placeholders() {
        PasswordSettings settings = settings();
        return new Placeholder[] {
            Placeholder.of("min", settings.minLength()),
            Placeholder.of("max", settings.maxLength())
        };
    }

    private PasswordSettings settings() {
        return config.settings().security().password();
    }
}
