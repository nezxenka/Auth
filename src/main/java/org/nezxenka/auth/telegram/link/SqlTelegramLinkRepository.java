package org.nezxenka.auth.telegram.link;

import java.util.UUID;
import org.nezxenka.auth.database.Database;
import org.nezxenka.auth.database.dialect.SqlDialect;
import org.nezxenka.auth.database.jdbc.JdbcTemplate;

public final class SqlTelegramLinkRepository implements TelegramLinkRepository {

    private static final String EXISTS = "SELECT 1 FROM telegram_links WHERE telegram_id = ?";
    private static final String LINK_ACCOUNT = "UPDATE auth_users SET telegram_id = ?, link_code = NULL WHERE uuid = ?";
    private static final String UNLINK_ACCOUNT = "UPDATE auth_users SET telegram_id = 0 WHERE uuid = ?";
    private static final String DELETE_LINK = "DELETE FROM telegram_links WHERE uuid = ?";

    private final JdbcTemplate jdbc;
    private final SqlDialect dialect;

    public SqlTelegramLinkRepository(Database database) {
        this.jdbc = database.getJdbc();
        this.dialect = database.getDialect();
    }

    @Override
    public boolean isTelegramLinked(long telegramId) {
        return jdbc.exists(EXISTS, statement -> statement.setLong(1, telegramId));
    }

    @Override
    public boolean link(UUID uuid, String username, long telegramId) {
        return jdbc.transaction(connection -> {
            if (JdbcTemplate.exists(connection, EXISTS, statement -> statement.setLong(1, telegramId))) {
                return false;
            }
            JdbcTemplate.update(connection, LINK_ACCOUNT, statement -> {
                statement.setLong(1, telegramId);
                statement.setString(2, uuid.toString());
            });
            JdbcTemplate.update(connection, dialect.upsertTelegramLink(), statement -> {
                statement.setLong(1, telegramId);
                statement.setString(2, uuid.toString());
                statement.setString(3, username);
                statement.setLong(4, System.currentTimeMillis());
            });
            return true;
        });
    }

    @Override
    public void unlink(UUID uuid) {
        jdbc.transaction(connection -> {
            JdbcTemplate.update(connection, UNLINK_ACCOUNT, statement -> statement.setString(1, uuid.toString()));
            return JdbcTemplate.update(connection, DELETE_LINK, statement -> statement.setString(1, uuid.toString()));
        });
    }
}
