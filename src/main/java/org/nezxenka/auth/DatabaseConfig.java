package org.nezxenka.auth;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DatabaseConfig {

    private FileConfiguration dbConfig;
    private final File file;
    private final Auth plugin;

    public DatabaseConfig() {
        this.plugin = Auth.getInstance();
        this.file = new File(plugin.getDataFolder(), "databases.yml");
        load();
    }

    public void load() {
        dbConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getType() {
        return dbConfig.getString("database.type", "SQLite").toUpperCase();
    }

    public String getHost() {
        return dbConfig.getString("database.mysql.host", "localhost");
    }

    public int getPort() {
        return dbConfig.getInt("database.mysql.port", 3306);
    }

    public String getDatabase() {
        return dbConfig.getString("database.mysql.database", "auth");
    }

    public String getUsername() {
        return dbConfig.getString("database.mysql.username", "root");
    }

    public String getPassword() {
        return dbConfig.getString("database.mysql.password", "");
    }

    public boolean isUseSSL() {
        return dbConfig.getBoolean("database.mysql.useSSL", false);
    }

    public int getMaxPoolSize() {
        return dbConfig.getInt("database.mysql.pool.max-pool-size", 10);
    }

    public int getMinIdle() {
        return dbConfig.getInt("database.mysql.pool.min-idle", 2);
    }

    public long getConnectionTimeout() {
        return dbConfig.getLong("database.mysql.pool.connection-timeout", 5000);
    }

    public long getIdleTimeout() {
        return dbConfig.getLong("database.mysql.pool.idle-timeout", 600000);
    }

    public long getMaxLifetime() {
        return dbConfig.getLong("database.mysql.pool.max-lifetime", 1800000);
    }

    public String getSqliteFilename() {
        return dbConfig.getString("database.sqlite.filename", "auth.db");
    }
}
