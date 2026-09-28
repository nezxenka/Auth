package org.nezxenka.auth.telegram.api.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class InlineKeyboardButton {

    String text;
    String callbackData;
}
