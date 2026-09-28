package org.nezxenka.auth.telegram.bot;

import java.util.Optional;
import org.nezxenka.auth.config.settings.KeyboardSettings;

public enum CallbackType {
    APPROVE,
    DECLINE,
    UNLINK,
    INFO;

    public static Optional<CallbackType> resolve(String data, KeyboardSettings keyboard) {
        if (data.equals(keyboard.approve().callback())) {
            return Optional.of(APPROVE);
        }
        if (data.equals(keyboard.decline().callback())) {
            return Optional.of(DECLINE);
        }
        if (data.equals(keyboard.unlink().callback())) {
            return Optional.of(UNLINK);
        }
        if (data.equals(keyboard.info().callback()) || data.equals(keyboard.instruction().callback())) {
            return Optional.of(INFO);
        }
        return Optional.empty();
    }
}
