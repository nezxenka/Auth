package org.nezxenka.auth.database;

import java.util.Locale;

public enum DatabaseType {
    SQLITE,
    MYSQL;

    public static DatabaseType parse(String value) {
        if (value == null) {
            return SQLITE;
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "MYSQL", "MARIADB" -> MYSQL;
            default -> SQLITE;
        };
    }
}
