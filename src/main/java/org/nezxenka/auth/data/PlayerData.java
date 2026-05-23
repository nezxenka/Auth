package org.nezxenka.auth.data;

import org.bukkit.entity.Player;

public class PlayerData {

    private final Player player;
    private long lastActivity;

    public PlayerData(Player player) {
        this.player = player;
        this.lastActivity = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
}
