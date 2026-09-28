package org.nezxenka.auth.message;

import java.util.Map;
import org.nezxenka.auth.telegram.TelegramText;

public record LocaleBundle(
    String language,
    String prefix,
    Map<MessageKey, String> messages,
    Map<TelegramText, String> telegramTexts
) {

    public static final LocaleBundle EMPTY = new LocaleBundle("", "", Map.of(), Map.of());

    public String message(MessageKey key) {
        return messages.getOrDefault(key, key.getPath());
    }

    public String telegramText(TelegramText text) {
        return telegramTexts.getOrDefault(text, text.getKey());
    }
}
