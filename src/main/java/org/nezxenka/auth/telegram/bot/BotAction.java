package org.nezxenka.auth.telegram.bot;

@FunctionalInterface
public interface BotAction {

    void handle(BotContext context);
}
