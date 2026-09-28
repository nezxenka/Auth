package org.nezxenka.auth.database.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementBinder {

    void bind(PreparedStatement statement) throws SQLException;
}
