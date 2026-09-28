package org.nezxenka.auth.message;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.auth.telegram.TelegramText;

public final class MessageService {

    private final LocaleLoader loader;
    private volatile LocaleBundle bundle = LocaleBundle.EMPTY;

    public MessageService(JavaPlugin plugin) {
        this.loader = new LocaleLoader(plugin);
    }

    public void load(String language) {
        bundle = loader.load(language);
    }

    public String language() {
        return bundle.language();
    }

    public String raw(MessageKey key, Placeholder... placeholders) {
        return Placeholder.apply(bundle.message(key), placeholders);
    }

    public String prefixed(MessageKey key, Placeholder... placeholders) {
        return bundle.prefix() + raw(key, placeholders);
    }

    public void send(CommandSender sender, MessageKey key, Placeholder... placeholders) {
        sender.sendMessage(prefixed(key, placeholders));
    }

    public String telegram(TelegramText text) {
        return bundle.telegramText(text);
    }
}
