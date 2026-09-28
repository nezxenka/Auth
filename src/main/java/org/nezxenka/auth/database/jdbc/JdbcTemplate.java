package org.nezxenka.auth.database.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.database.DataAccessException;

@RequiredArgsConstructor
public final class JdbcTemplate {

    private final DataSource dataSource;

    public <T> Optional<T> queryOne(String sql, StatementBinder binder, RowMapper<T> mapper) {
        try (Connection connection = dataSource.getConnection()) {
            return queryOne(connection, sql, binder, mapper);
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to execute query: " + sql, exception);
        }
    }

    public boolean exists(String sql, StatementBinder binder) {
        try (Connection connection = dataSource.getConnection()) {
            return exists(connection, sql, binder);
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to execute query: " + sql, exception);
        }
    }

    public int update(String sql, StatementBinder binder) {
        try (Connection connection = dataSource.getConnection()) {
            return update(connection, sql, binder);
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to execute update: " + sql, exception);
        }
    }

    public <T> T transaction(TransactionCallback<T> callback) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = callback.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to execute transaction", exception);
        }
    }

    public static <T> Optional<T> queryOne(
        Connection connection,
        String sql,
        StatementBinder binder,
        RowMapper<T> mapper
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.ofNullable(mapper.map(resultSet)) : Optional.empty();
            }
        }
    }

    public static boolean exists(Connection connection, String sql, StatementBinder binder) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public static int update(Connection connection, String sql, StatementBinder binder) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate();
        }
    }

    public static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }
}
