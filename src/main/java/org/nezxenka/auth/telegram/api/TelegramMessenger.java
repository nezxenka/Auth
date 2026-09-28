package org.nezxenka.auth.telegram.api;

import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.concurrent.Futures;
import org.nezxenka.auth.telegram.api.model.AnswerCallbackQueryRequest;
import org.nezxenka.auth.telegram.api.model.InlineKeyboardMarkup;
import org.nezxenka.auth.telegram.api.model.SendMessageRequest;

@RequiredArgsConstructor
public final class TelegramMessenger {

    private final TelegramApiClient api;
    private final Logger logger;

    public void send(long chatId, String text) {
        send(chatId, text, null);
    }

    public void send(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessageRequest request = SendMessageRequest.builder()
            .chatId(chatId)
            .text(text)
            .replyMarkup(keyboard)
            .build();
        api.sendMessage(request).exceptionally(error -> {
            logger.warning("Failed to send Telegram message: " + Futures.unwrap(error).getMessage());
            return null;
        });
    }

    public void answerCallback(String callbackQueryId) {
        if (callbackQueryId == null) {
            return;
        }
        api.answerCallbackQuery(AnswerCallbackQueryRequest.of(callbackQueryId)).exceptionally(error -> null);
    }
}
