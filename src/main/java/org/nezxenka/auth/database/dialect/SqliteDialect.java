package org.nezxenka.auth.database.dialect;

import java.sql.Connection;
import java.util.List;

public final class SqliteDialect implements SqlDialect {

    private static final List<String> TABLES = List.of(
        """
        CREATE TABLE IF NOT EXISTS auth_users (
            uuid TEXT PRIMARY KEY,
            username TEXT NOT NULL,
            password TEXT NOT NULL,
            last_login INTEGER DEFAULT 0,
            registered INTEGER DEFAULT 0,
            telegram_id INTEGER DEFAULT 0,
            link_code TEXT DEFAULT NULL,
            session_ip TEXT DEFAULT NULL,
            session_expiry INTEGER DEFAULT 0
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS telegram_links (
            telegram_id INTEGER PRIMARY KEY,
            uuid TEXT NOT NULL,
            username TEXT NOT NULL,
            linked_at INTEGER DEFAULT 0
        )
        """
    );

    private static final List<IndexDefinition> INDEXES = List.of(
        new IndexDefinition("idx_auth_users_username", "auth_users", "username COLLATE NOCASE"),
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
        return "CREATE INDEX IF NOT EXISTS " + index.name() + " ON " + index.table() + " (" + index.columns() + ")";
    }

    @Override
    public boolean indexExists(Connection connection, IndexDefinition index) {
        return false;
    }

    @Override
    public String usernameCondition() {
        return "username = ? COLLATE NOCASE";
    }

    @Override
    public String upsertTelegramLink() {
        return "INSERT OR REPLACE INTO telegram_links (telegram_id, uuid, username, linked_at) VALUES (?, ?, ?, ?)";
    }
}
