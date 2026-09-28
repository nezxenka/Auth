package org.nezxenka.auth.telegram.api.model;

import java.util.List;
import lombok.Value;

@Value
public class InlineKeyboardMarkup {

    List<List<InlineKeyboardButton>> inlineKeyboard;

    public static InlineKeyboardMarkup row(InlineKeyboardButton... buttons) {
        return new InlineKeyboardMarkup(List.of(List.of(buttons)));
    }
}
