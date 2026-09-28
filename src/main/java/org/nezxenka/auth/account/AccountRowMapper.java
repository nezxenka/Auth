package org.nezxenka.auth.account;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.nezxenka.auth.database.jdbc.RowMapper;

public final class AccountRowMapper implements RowMapper<Account> {

    @Override
    public Account map(ResultSet resultSet) throws SQLException {
        return Account.builder()
            .uuid(UUID.fromString(resultSet.getString("uuid")))
            .username(resultSet.getString("username"))
            .passwordHash(resultSet.getString("password"))
            .registeredAt(resultSet.getLong("registered"))
            .lastLogin(resultSet.getLong("last_login"))
            .telegramId(resultSet.getLong("telegram_id"))
            .sessionIp(resultSet.getString("session_ip"))
            .sessionExpiry(resultSet.getLong("session_expiry"))
            .build();
    }
}
