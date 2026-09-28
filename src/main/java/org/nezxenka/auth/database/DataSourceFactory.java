package org.nezxenka.auth.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.config.settings.DatabaseSettings;
import org.nezxenka.auth.config.settings.MySqlSettings;
import org.nezxenka.auth.config.settings.PoolSettings;
import org.nezxenka.auth.config.settings.SqliteSettings;

@RequiredArgsConstructor
public final class DataSourceFactory {

    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    private final File dataFolder;

    public HikariDataSource create(DatabaseSettings settings) {
        HikariConfig config = switch (settings.type()) {
            case MYSQL -> mysql(settings.mysql());
            case SQLITE -> sqlite(settings.sqlite());
        };
        return new HikariDataSource(config);
    }

    private HikariConfig mysql(MySqlSettings settings) {
        PoolSettings pool = settings.pool();
        HikariConfig config = new HikariConfig();
        config.setPoolName("Auth-MySQL");
        config.setDriverClassName(MYSQL_DRIVER);
        config.setJdbcUrl(settings.jdbcUrl());
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.setMaximumPoolSize(pool.maxPoolSize());
        config.setMinimumIdle(pool.minIdle());
        config.setConnectionTimeout(pool.connectionTimeout());
        config.setIdleTimeout(pool.idleTimeout());
        config.setMaxLifetime(pool.maxLifetime());
        config.addDataSourceProperty("sslMode", settings.useSsl() ? "REQUIRED" : "DISABLED");
        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        settings.properties().forEach(config::addDataSourceProperty);
        return config;
    }

    private HikariConfig sqlite(SqliteSettings settings) {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("Failed to create data folder " + dataFolder);
        }
        File file = new File(dataFolder, settings.filename());
        HikariConfig config = new HikariConfig();
        config.setPoolName("Auth-SQLite");
        config.setDriverClassName(SQLITE_DRIVER);
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setIdleTimeout(0L);
        config.setMaxLifetime(0L);
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        config.addDataSourceProperty("busy_timeout", "5000");
        return config;
    }
}
