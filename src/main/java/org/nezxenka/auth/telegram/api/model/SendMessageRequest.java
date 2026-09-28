package org.nezxenka.auth.telegram.api.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SendMessageRequest {

    long chatId;
    String text;
    InlineKeyboardMarkup replyMarkup;
}
