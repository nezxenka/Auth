package org.nezxenka.auth.managers;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nezxenka.auth.Auth;

public class MessageManager {

    private FileConfiguration messages;

    public MessageManager() {
        load();
    }

    public void load() {
        File file = new File(
            Auth.getInstance().getDataFolder(),
            "messages.yml"
        );
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        String prefix = messages.getString("prefix", "");
        String msg = messages.getString(path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public String getRawMessage(String path) {
        String msg = messages.getString(path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void reload() {
        load();
    }
}
