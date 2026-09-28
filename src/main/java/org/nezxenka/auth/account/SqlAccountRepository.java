package org.nezxenka.auth.account;

import java.util.Optional;
import java.util.UUID;
import org.nezxenka.auth.database.Database;
import org.nezxenka.auth.database.dialect.SqlDialect;
import org.nezxenka.auth.database.jdbc.JdbcTemplate;
import org.nezxenka.auth.database.jdbc.RowMapper;

public final class SqlAccountRepository implements AccountRepository {

    private static final RowMapper<Account> MAPPER = new AccountRowMapper();

    private static final String SELECT = """
        SELECT uuid, username, password, registered, last_login, telegram_id, session_ip, session_expiry
        FROM auth_users
        """;

    private static final String SELECT_BY_TELEGRAM = """
        SELECT u.uuid AS uuid, u.username AS username, u.password AS password,
               u.registered AS registered, u.last_login AS last_login, u.telegram_id AS telegram_id,
               u.session_ip AS session_ip, u.session_expiry AS session_expiry
        FROM auth_users u
        JOIN telegram_links t ON t.uuid = u.uuid
        WHERE t.telegram_id = ?
        LIMIT 1
        """;

    private static final String INSERT = """
        INSERT INTO auth_users (uuid, username, password, registered, last_login, telegram_id, session_ip, session_expiry)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_PASSWORD = "UPDATE auth_users SET password = ? WHERE uuid = ?";
    private static final String RESET_PASSWORD =
        "UPDATE auth_users SET password = ?, session_ip = NULL, session_expiry = 0 WHERE uuid = ?";
    private static final String UPDATE_LOGIN =
        "UPDATE auth_users SET username = ?, last_login = ?, session_ip = ?, session_expiry = ? WHERE uuid = ?";
    private static final String UPDATE_LAST_LOGIN = "UPDATE auth_users SET username = ?, last_login = ? WHERE uuid = ?";
    private static final String UPDATE_USERNAME = "UPDATE auth_users SET username = ? WHERE uuid = ?";

    private final JdbcTemplate jdbc;
    private final String selectByUsername;

    public SqlAccountRepository(Database database) {
        SqlDialect dialect = database.getDialect();
        this.jdbc = database.getJdbc();
        this.selectByUsername = SELECT + "WHERE " + dialect.usernameCondition() + " ORDER BY last_login DESC LIMIT 1";
    }

    @Override
    public Optional<Account> findByUuid(UUID uuid) {
        return jdbc.queryOne(SELECT + "WHERE uuid = ?", statement -> statement.setString(1, uuid.toString()), MAPPER);
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return jdbc.queryOne(selectByUsername, statement -> statement.setString(1, username), MAPPER);
    }

    @Override
    public Optional<Account> findByTelegramId(long telegramId) {
        return jdbc.queryOne(SELECT_BY_TELEGRAM, statement -> statement.setLong(1, telegramId), MAPPER);
    }

    @Override
    public void insert(Account account) {
        jdbc.update(INSERT, statement -> {
            statement.setString(1, account.getUuid().toString());
            statement.setString(2, account.getUsername());
            statement.setString(3, account.getPasswordHash());
            statement.setLong(4, account.getRegisteredAt());
            statement.setLong(5, account.getLastLogin());
            statement.setLong(6, account.getTelegramId());
            JdbcTemplate.setNullableString(statement, 7, account.getSessionIp());
            statement.setLong(8, account.getSessionExpiry());
        });
    }

    @Override
    public void updatePassword(UUID uuid, String passwordHash) {
        jdbc.update(UPDATE_PASSWORD, statement -> {
            statement.setString(1, passwordHash);
            statement.setString(2, uuid.toString());
        });
    }

    @Override
    public void resetPassword(UUID uuid, String passwordHash) {
        jdbc.update(RESET_PASSWORD, statement -> {
            statement.setString(1, passwordHash);
            statement.setString(2, uuid.toString());
        });
    }

    @Override
    public void updateLogin(UUID uuid, String username, long lastLogin, String sessionIp, long sessionExpiry) {
        jdbc.update(UPDATE_LOGIN, statement -> {
            statement.setString(1, username);
            statement.setLong(2, lastLogin);
            JdbcTemplate.setNullableString(statement, 3, sessionIp);
            statement.setLong(4, sessionExpiry);
            statement.setString(5, uuid.toString());
        });
    }

    @Override
    public void updateLastLogin(UUID uuid, String username, long lastLogin) {
        jdbc.update(UPDATE_LAST_LOGIN, statement -> {
            statement.setString(1, username);
            statement.setLong(2, lastLogin);
            statement.setString(3, uuid.toString());
        });
    }

    @Override
    public void updateUsername(UUID uuid, String username) {
        jdbc.update(UPDATE_USERNAME, statement -> {
            statement.setString(1, username);
            statement.setString(2, uuid.toString());
        });
    }
}
