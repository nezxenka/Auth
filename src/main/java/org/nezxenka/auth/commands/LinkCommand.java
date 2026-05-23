package org.nezxenka.auth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.auth.Auth;

public class LinkCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("not_logged_in")
            );
            return true;
        }

        if (
            Auth.getInstance()
                .getTelegramLinkManager()
                .isLinked(player.getUniqueId().toString())
        ) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("telegram_already_linked")
            );
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§eИспользование: /link <код_привязки>");
            player.sendMessage(
                "§7Получите код у бота командой /привязать <никнейм>"
            );
            return true;
        }

        String code = args[0];
        String uuid = player.getUniqueId().toString();
        String storedCode = Auth.getInstance()
            .getDatabaseManager()
            .getLinkCode(uuid);

        if (storedCode == null || !storedCode.equals(code)) {
            player.sendMessage("§cНеверный код привязки.");
            return true;
        }

        Long telegramId = Auth.getInstance()
            .getTelegramLinkManager()
            .getPendingTelegramId(uuid);
        if (telegramId == null) {
            player.sendMessage(
                "§cКод привязки не привязан к Telegram аккаунту. Сначала получите код у бота."
            );
            return true;
        }

        if (
            Auth.getInstance().getDatabaseManager().isTelegramLinked(telegramId)
        ) {
            player.sendMessage(
                "§cЭтот Telegram ID уже привязан к другому аккаунту."
            );
            return true;
        }

        Auth.getInstance().getDatabaseManager().linkTelegram(uuid, telegramId);
        Auth.getInstance().getDatabaseManager().removeLinkCode(uuid);
        Auth.getInstance().getTelegramLinkManager().clearPendingLink(uuid);

        boolean telegram2FA = Auth.getInstance()
            .getConfig()
            .getBoolean("telegram.2fa.enabled", false);
        if (telegram2FA) {
            Auth.getInstance().getDatabaseManager().set2FAEnabled(uuid, true);
            player.sendMessage(
                "§aАккаунт успешно привязан к Telegram! 2FA автоматически включена."
            );
        } else {
            player.sendMessage("§aАккаунт успешно привязан к Telegram!");
        }
        return true;
    }
}
