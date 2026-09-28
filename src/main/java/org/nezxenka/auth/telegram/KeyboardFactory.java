package org.nezxenka.auth.telegram;

import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.ButtonSettings;
import org.nezxenka.auth.config.settings.KeyboardSettings;
import org.nezxenka.auth.telegram.api.model.InlineKeyboardButton;
import org.nezxenka.auth.telegram.api.model.InlineKeyboardMarkup;

@RequiredArgsConstructor
public final class KeyboardFactory {

    private final ConfigService config;
    private final TelegramTexts texts;

    public InlineKeyboardMarkup twoFactor() {
        KeyboardSettings keyboard = keyboard();
        return InlineKeyboardMarkup.row(button(keyboard.approve()), button(keyboard.decline()));
    }

    public InlineKeyboardMarkup main() {
        KeyboardSettings keyboard = keyboard();
        return InlineKeyboardMarkup.row(button(keyboard.info()), button(keyboard.unlink()));
    }

    public InlineKeyboardMarkup instruction() {
        return InlineKeyboardMarkup.row(button(keyboard().instruction()));
    }

    private KeyboardSettings keyboard() {
        return config.settings().telegram().keyboard();
    }

    private InlineKeyboardButton button(ButtonSettings settings) {
        return InlineKeyboardButton.of(texts.render(settings.text()), settings.callback());
    }
}
