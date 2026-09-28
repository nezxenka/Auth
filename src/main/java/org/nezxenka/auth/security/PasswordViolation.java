package org.nezxenka.auth.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.message.MessageKey;

@Getter
@RequiredArgsConstructor
public enum PasswordViolation {
    TOO_SHORT(MessageKey.PASSWORD_TOO_SHORT),
    TOO_LONG(MessageKey.PASSWORD_TOO_LONG);

    private final MessageKey messageKey;
}
