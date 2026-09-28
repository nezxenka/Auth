package org.nezxenka.auth.database.dialect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class MySqlDialect implements SqlDialect {

    private static final List<String> TABLES = List.of(
        """
        CREATE TABLE IF NOT EXISTS auth_users (
            uuid VARCHAR(36) NOT NULL PRIMARY KEY,
            username VARCHAR(16) NOT NULL,
            password VARCHAR(128) NOT NULL,
            last_login BIGINT NOT NULL DEFAULT 0,
            registered BIGINT NOT NULL DEFAULT 0,
            telegram_id BIGINT NOT NULL DEFAULT 0,
            link_code VARCHAR(16) DEFAULT NULL,
            session_ip VARCHAR(45) DEFAULT NULL,
            session_expiry BIGINT NOT NULL DEFAULT 0
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """,
        """
        CREATE TABLE IF NOT EXISTS telegram_links (
            telegram_id BIGINT NOT NULL PRIMARY KEY,
            uuid VARCHAR(36) NOT NULL,
            username VARCHAR(16) NOT NULL,
            linked_at BIGINT NOT NULL DEFAULT 0
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """
    );

    private static final List<IndexDefinition> INDEXES = List.of(
        new IndexDefinition("idx_auth_users_username", "auth_users", "username"),
        new IndexDefinition("idx_auth_users_telegram", "auth_users", "telegram_id"),
        new IndexDefinition("idx_telegram_links_uuid", "telegram_links", "uuid")
    );

    @Override
    public List<String> createTableStatements() {
        return TABLES;
    }

    @Override
    public List<IndexDefinition> indexes() {
        return INDEXES;
    }

    @Override
    public String createIndexStatement(IndexDefinition index) {
        return "CREATE INDEX " + index.name() + " ON " + index.table() + " (" + index.columns() + ")";
    }

    @Override
    public boolean indexExists(Connection connection, IndexDefinition index) throws SQLException {
        try (
            ResultSet indexes = connection
                .getMetaData()
                .getIndexInfo(connection.getCatalog(), null, index.table(), false, false)
        ) {
            while (indexes.next()) {
                if (index.name().equalsIgnoreCase(indexes.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String usernameCondition() {
        return "username = ?";
    }

    @Override
    public String upsertTelegramLink() {
        return """
            INSERT INTO telegram_links (telegram_id, uuid, username, linked_at)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE uuid = VALUES(uuid), username = VALUES(username), linked_at = VALUES(linked_at)
            """;
    }
}
