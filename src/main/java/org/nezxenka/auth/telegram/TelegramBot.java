package org.nezxenka.auth.telegram;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.nezxenka.auth.Auth;

public class TelegramBot {

    private final String botToken;
    private final String apiUrl;
    private boolean running = false;
    private int lastUpdateId = 0;

    private final Map<UUID, String> pendingConfirmations =
        new ConcurrentHashMap<>();

    public TelegramBot(String botToken) {
        this.botToken = botToken;
        this.apiUrl = "https://api.telegram.org/bot" + botToken;
    }

    public void start() {
        running = true;
        new Thread(() -> {
            while (running) {
                try {
                    getUpdates();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    Auth.getInstance()
                        .getLogger()
                        .warning("Telegram bot error: " + e.getMessage());
                }
            }
        }).start();
        Auth.getInstance().getLogger().info("Telegram bot started!");
    }

    public void stop() {
        running = false;
    }

    public void request2FA(Player player, String ip) {
        UUID uuid = player.getUniqueId();
        String nickname = player.getName();
        pendingConfirmations.put(uuid, nickname);

        String msg = getMessage("2fa_request")
            .replace("%nickname%", nickname)
            .replace("%ip%", ip);

        Long tgId = Auth.getInstance()
            .getDatabaseManager()
            .getTelegramId(uuid.toString());
        if (tgId != null) {
            sendMessageWithKeyboard(tgId, msg, create2FAKeyboard());
        }
    }

    public boolean is2FAPending(UUID uuid) {
        return pendingConfirmations.containsKey(uuid);
    }

    public void cancel2FA(UUID uuid) {
        pendingConfirmations.remove(uuid);
    }

    private void approve2FA(long telegramId) {
        UUID uuid = getUUIDFromPending(telegramId);
        if (uuid == null) return;

        pendingConfirmations.remove(uuid);
        sendMessage(telegramId, getMessage("2fa_approved"));

        Bukkit.getScheduler().runTask(Auth.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                Auth.getInstance().getSessionManager().addAuthenticated(player);
                Auth.getInstance()
                    .getDatabaseManager()
                    .updateLastLogin(uuid.toString());
                saveIpSession(player);
                player.sendMessage(
                    Auth.getInstance()
                        .getMessageManager()
                        .getMessage("login_success")
                );
            }
        });
    }

    private void decline2FA(long telegramId) {
        UUID uuid = getUUIDFromPending(telegramId);
        if (uuid == null) return;

        pendingConfirmations.remove(uuid);
        sendMessage(telegramId, getMessage("2fa_declined"));

        Bukkit.getScheduler().runTask(Auth.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.kickPlayer(getMessage("2fa_declined"));
            }
        });
    }

    private UUID getUUIDFromPending(long telegramId) {
        String uuidStr = Auth.getInstance()
            .getDatabaseManager()
            .getUUIDByTelegramId(telegramId);
        if (uuidStr == null) return null;
        UUID uuid = UUID.fromString(uuidStr);
        return pendingConfirmations.containsKey(uuid) ? uuid : null;
    }

    public void notifyJoin(Player player, String ip) {
        if (!getConfigBoolean("notifications.join.enabled")) return;
        String uuid = player.getUniqueId().toString();
        Long tgId = Auth.getInstance().getDatabaseManager().getTelegramId(uuid);
        if (tgId == null) return;

        String msg = getNotificationMessage("notifications.join.message", "")
            .replace("%nickname%", player.getName())
            .replace("%ip%", ip);
        sendMessage(tgId, msg);
    }

    public void notifyLeave(Player player, String ip) {
        if (!getConfigBoolean("notifications.leave.enabled")) return;
        String uuid = player.getUniqueId().toString();
        Long tgId = Auth.getInstance().getDatabaseManager().getTelegramId(uuid);
        if (tgId == null) return;

        String msg = getNotificationMessage("notifications.leave.message", "")
            .replace("%nickname%", player.getName())
            .replace("%ip%", ip);
        sendMessage(tgId, msg);
    }

    private void getUpdates() {
        try {
            String urlString =
                apiUrl +
                "/getUpdates?offset=" +
                (lastUpdateId + 1) +
                "&timeout=1";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) return;

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JsonObject json = new JsonParser()
                .parse(response.toString())
                .getAsJsonObject();
            if (!json.get("ok").getAsBoolean()) return;

            JsonArray results = json.getAsJsonArray("result");
            if (results.size() == 0) return;

            for (int i = 0; i < results.size(); i++) {
                JsonObject update = results.get(i).getAsJsonObject();
                lastUpdateId = update.get("update_id").getAsInt();
                processUpdate(update);
            }
        } catch (Exception e) {
            Auth.getInstance()
                .getLogger()
                .warning("Telegram getUpdates error: " + e.getMessage());
        }
    }

    private void processUpdate(JsonObject update) {
        if (update.has("callback_query")) {
            JsonObject callback = update.getAsJsonObject("callback_query");
            long chatId = callback
                .getAsJsonObject("message")
                .getAsJsonObject("chat")
                .get("id")
                .getAsLong();
            long userId = callback
                .getAsJsonObject("from")
                .get("id")
                .getAsLong();
            String data = callback.get("data").getAsString();

            answerCallbackQuery(callback.get("id").getAsString());
            processCallback(userId, chatId, data);
            return;
        }

        if (!update.has("message")) return;
        JsonObject message = update.getAsJsonObject("message");
        if (!message.has("text")) return;

        String text = message.get("text").getAsString();
        long chatId = message.getAsJsonObject("chat").get("id").getAsLong();
        long userId = message.has("from")
            ? message.getAsJsonObject("from").get("id").getAsLong()
            : chatId;

        processMessage(userId, chatId, text);
    }

    private void processMessage(long userId, long chatId, String text) {
        if (text.startsWith("/start")) {
            sendMessageWithKeyboard(
                chatId,
                getMessage("start"),
                createLinkStartKeyboard()
            );
        } else if (text.startsWith("/привязать") || text.startsWith("/link")) {
            handleLinkCommand(chatId, userId, text);
        } else if (text.startsWith("/отвязать") || text.startsWith("/unlink")) {
            handleUnlinkCommand(chatId, userId);
        } else if (text.startsWith("/info")) {
            handleInfoCommand(chatId, userId);
        } else if (
            text.startsWith("/keyboard") || text.startsWith("/клавиатура")
        ) {
            sendMessageWithKeyboard(
                chatId,
                getMessage("start"),
                createMainKeyboard(userId)
            );
        }
    }

    private void processCallback(long userId, long chatId, String data) {
        switch (data) {
            case "2fa_approve":
                approve2FA(userId);
                break;
            case "2fa_decline":
                decline2FA(userId);
                break;
            case "unlink":
                handleUnlinkCommand(chatId, userId);
                break;
            case "info":
                handleInfoCommand(chatId, userId);
                break;
        }
    }

    private void answerCallbackQuery(String callbackId) {
        try {
            String urlString = apiUrl + "/answerCallbackQuery";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JsonObject body = new JsonObject();
            body.addProperty("callback_query_id", callbackId);
            body.addProperty("text", "");

            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes("UTF-8"));
            os.flush();
            os.close();
            conn.getResponseCode();
        } catch (Exception ignored) {}
    }

    private void handleLinkCommand(long chatId, long userId, String text) {
        String[] parts = text.split(" ");
        if (parts.length < 2) {
            sendMessageWithKeyboard(
                chatId,
                getMessage("link_usage"),
                createLinkStartKeyboard()
            );
            return;
        }
        String nickname = parts[1];
        String uuid = getUUIDByNickname(nickname);
        if (uuid == null) {
            sendMessage(
                chatId,
                getMessage("link_player_not_found").replace(
                    "%nickname%",
                    nickname
                )
            );
            return;
        }
        if (
            Auth.getInstance().getDatabaseManager().isTelegramLinkedByUUID(uuid)
        ) {
            sendMessage(chatId, getMessage("link_already_linked"));
            return;
        }
        if (Auth.getInstance().getDatabaseManager().isTelegramLinked(userId)) {
            sendMessage(chatId, getMessage("link_telegram_already_linked"));
            return;
        }

        String code = Auth.getInstance()
            .getTelegramLinkManager()
            .generateLinkCode(uuid);
        Auth.getInstance()
            .getTelegramLinkManager()
            .setPendingTelegramId(uuid, userId);
        sendMessage(chatId, getMessage("link_code").replace("%code%", code));
    }

    private void handleUnlinkCommand(long chatId, long userId) {
        if (!Auth.getInstance().getDatabaseManager().isTelegramLinked(userId)) {
            sendMessage(chatId, getMessage("unlink_not_linked"));
            return;
        }
        String uuid = Auth.getInstance()
            .getDatabaseManager()
            .getUUIDByTelegramId(userId);
        Auth.getInstance().getDatabaseManager().set2FAEnabled(uuid, false);
        Auth.getInstance().getDatabaseManager().unlinkTelegram(uuid);
        pendingConfirmations.remove(UUID.fromString(uuid));
        sendMessage(chatId, getMessage("unlink_success"));
    }

    private void handleInfoCommand(long chatId, long userId) {
        String uuidStr = Auth.getInstance()
            .getDatabaseManager()
            .getUUIDByTelegramId(userId);
        if (uuidStr == null) {
            sendMessage(chatId, "Вы не привязаны к аккаунту.");
            return;
        }
        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(
            UUID.fromString(uuidStr)
        );
        String nickname =
            offPlayer.getName() != null ? offPlayer.getName() : "Unknown";
        String status = offPlayer.isOnline()
            ? getMessage("info_online")
            : getMessage("info_offline");

        String msg = getMessage("info_message")
            .replace("%nickname%", nickname)
            .replace("%status%", status);
        sendMessageWithKeyboard(chatId, msg, createMainKeyboard(userId));
    }

    private String getUUIDByNickname(String nickname) {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (
                player.getName() != null &&
                player.getName().equalsIgnoreCase(nickname)
            ) {
                String uuid = player.getUniqueId().toString();
                if (
                    Auth.getInstance().getDatabaseManager().isRegistered(uuid)
                ) {
                    return uuid;
                }
            }
        }
        return null;
    }

    private JsonObject create2FAKeyboard() {
        JsonObject keyboard = new JsonObject();
        JsonArray rows = new JsonArray();
        JsonArray row = new JsonArray();

        JsonObject approveBtn = new JsonObject();
        approveBtn.addProperty("text", getButtonText("approve", "Подтвердить"));
        approveBtn.addProperty(
            "callback_data",
            getButtonCallback("approve", "2fa_approve")
        );
        row.add(approveBtn);

        JsonObject declineBtn = new JsonObject();
        declineBtn.addProperty("text", getButtonText("decline", "Отклонить"));
        declineBtn.addProperty(
            "callback_data",
            getButtonCallback("decline", "2fa_decline")
        );
        row.add(declineBtn);

        rows.add(row);
        keyboard.add("inline_keyboard", rows);
        return keyboard;
    }

    private JsonObject createMainKeyboard(long userId) {
        JsonObject keyboard = new JsonObject();
        JsonArray rows = new JsonArray();
        JsonArray row = new JsonArray();

        JsonObject infoBtn = new JsonObject();
        infoBtn.addProperty("text", getButtonText("info", "Инфо"));
        infoBtn.addProperty("callback_data", getButtonCallback("info", "info"));
        row.add(infoBtn);

        JsonObject unlinkBtn = new JsonObject();
        unlinkBtn.addProperty("text", getButtonText("unlink", "Отвязать"));
        unlinkBtn.addProperty(
            "callback_data",
            getButtonCallback("unlink", "unlink")
        );
        row.add(unlinkBtn);

        rows.add(row);
        keyboard.add("inline_keyboard", rows);
        return keyboard;
    }

    private JsonObject createLinkStartKeyboard() {
        JsonObject keyboard = new JsonObject();
        JsonArray rows = new JsonArray();
        JsonArray row = new JsonArray();

        JsonObject infoBtn = new JsonObject();
        infoBtn.addProperty("text", "Инструкция");
        infoBtn.addProperty("callback_data", "info");
        row.add(infoBtn);

        rows.add(row);
        keyboard.add("inline_keyboard", rows);
        return keyboard;
    }

    private void sendMessage(long chatId, String text) {
        sendMessageInternal(chatId, text, null);
    }

    private void sendMessageWithKeyboard(
        long chatId,
        String text,
        JsonObject keyboard
    ) {
        sendMessageInternal(chatId, text, keyboard);
    }

    private void sendMessageInternal(
        long chatId,
        String text,
        JsonObject keyboard
    ) {
        try {
            String urlString = apiUrl + "/sendMessage";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JsonObject body = new JsonObject();
            body.addProperty("chat_id", chatId);
            body.addProperty("text", formatText(text));

            if (keyboard != null) {
                body.add("reply_markup", keyboard);
            }

            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            if (code != 200) {
                Auth.getInstance()
                    .getLogger()
                    .warning("Telegram API error: " + code);
            }
        } catch (Exception e) {
            Auth.getInstance()
                .getLogger()
                .warning("Failed to send Telegram message: " + e.getMessage());
        }
    }

    private String formatText(String text) {
        return text.replace("%newline%", "\n");
    }

    private String getMessage(String key) {
        String msg = Auth.getInstance()
            .getConfig()
            .getString("telegram.messages." + key, "");
        if (!msg.isEmpty()) return msg;
        return getDefaultMessage(key);
    }

    private String getDefaultMessage(String key) {
        switch (key) {
            case "start":
                return "Привет! Используй /привязать никнейм чтобы привязать аккаунт.";
            case "link_usage":
                return "Использование: /привязать никнейм";
            case "link_player_not_found":
                return "Игрок с ником %nickname% не найден или не зарегистрирован.";
            case "link_already_linked":
                return "Этот аккаунт уже привязан к Telegram.";
            case "link_telegram_already_linked":
                return "Ваш Telegram уже привязан к другому аккаунту.";
            case "link_code":
                return "Код привязки: %code%%newline%Зайдите на сервер и введите /link %code%";
            case "link_success":
                return "Аккаунт успешно привязан! 2FA автоматически включена.";
            case "unlink_not_linked":
                return "Ваш Telegram не привязан ни к какому аккаунту.";
            case "unlink_success":
                return "Привязка успешно удалена.";
            case "info_message":
                return "Ник: %nickname%%newline%2FA: Включена%newline%Статус: %status%";
            case "info_online":
                return "На сервере";
            case "info_offline":
                return "Не в сети";
            case "2fa_request":
                return "Кто-то пытается зайти под аккаунтом %nickname%%newline%IP: %ip%%newline%Это вы?";
            case "2fa_approved":
                return "Вход подтверждён.";
            case "2fa_declined":
                return "Вход отклонён.";
            case "2fa_kick":
                return "Вы не подтвердили вход. Отключены.";
            default:
                return "Message not found: " + key;
        }
    }

    private String getNotificationMessage(String path, String defaultVal) {
        String msg = Auth.getInstance()
            .getConfig()
            .getString("telegram." + path, "");
        if (!msg.isEmpty()) return msg;
        if (
            path.contains("join")
        ) return "Игрок %nickname% зашёл на сервер%newline%IP: %ip%";
        if (
            path.contains("leave")
        ) return "Игрок %nickname% вышел с сервера%newline%IP: %ip%";
        return defaultVal;
    }

    private boolean getConfigBoolean(String key) {
        return Auth.getInstance()
            .getConfig()
            .getBoolean("telegram." + key, false);
    }

    private String getButtonText(String key, String defaultVal) {
        return Auth.getInstance()
            .getConfig()
            .getString("telegram.keyboard." + key + ".text", defaultVal);
    }

    private String getButtonCallback(String key, String defaultVal) {
        return Auth.getInstance()
            .getConfig()
            .getString("telegram.keyboard." + key + ".callback", defaultVal);
    }

    private void saveIpSession(Player player) {
        boolean enabled = Auth.getInstance()
            .getConfig()
            .getBoolean("security.ip-session.enabled", true);
        if (!enabled) return;
        long lifetime = Auth.getInstance()
            .getConfig()
            .getLong("security.ip-session.lifetime", 21600);
        String ip = player.getAddress().getAddress().getHostAddress();
        long expiry = System.currentTimeMillis() + (lifetime * 1000L);
        Auth.getInstance()
            .getDatabaseManager()
            .saveSession(player.getUniqueId().toString(), ip, expiry);
    }
}
