package org.nezxenka.auth.telegram.bot;

import java.util.List;

public record BotContext(long chatId, long userId, List<String> args) {
}
