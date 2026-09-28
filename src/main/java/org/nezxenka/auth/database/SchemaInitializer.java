package org.nezxenka.auth.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.database.dialect.IndexDefinition;
import org.nezxenka.auth.database.dialect.SqlDialect;

@RequiredArgsConstructor
public final class SchemaInitializer {

    private final Database database;

    public void initialize() {
        SqlDialect dialect = database.getDialect();
        try (
            Connection connection = database.getDataSource().getConnection();
            Statement statement = connection.createStatement()
        ) {
            for (String ddl : dialect.createTableStatements()) {
                statement.executeUpdate(ddl);
            }
            for (IndexDefinition index : dialect.indexes()) {
                if (!dialect.indexExists(connection, index)) {
                    statement.executeUpdate(dialect.createIndexStatement(index));
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to initialize database schema", exception);
        }
    }
}
