package org.nezxenka.auth.telegram.bot;

import java.util.List;

public interface BotCommand extends BotAction {

    List<String> aliases();
}
