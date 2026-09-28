package org.nezxenka.auth.telegram;

import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.message.Placeholder;

@RequiredArgsConstructor
public final class TelegramTexts {

    private static final String NEWLINE_TOKEN = "%newline%";

    private final MessageService messages;

    public String render(TelegramText text, Placeholder... placeholders) {
        return Placeholder.apply(messages.telegram(text), placeholders).replace(NEWLINE_TOKEN, "\n");
    }
}
