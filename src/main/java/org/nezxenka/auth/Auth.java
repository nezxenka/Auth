package org.nezxenka.auth;

import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.auth.bootstrap.AuthBootstrap;

public final class Auth extends JavaPlugin {

    private AuthBootstrap bootstrap;

    @Override
    public void onEnable() {
        bootstrap = new AuthBootstrap(this);
        try {
            bootstrap.enable();
            getLogger().info("Auth plugin enabled successfully!");
        } catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE, "Failed to enable Auth plugin", exception);
            bootstrap.disable();
            bootstrap = null;
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
            bootstrap = null;
        }
        getLogger().info("Auth plugin disabled!");
    }
}
