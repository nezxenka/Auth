package org.nezxenka.auth.database.dialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SqlDialect {

    List<String> createTableStatements();

    List<IndexDefinition> indexes();

    String createIndexStatement(IndexDefinition index);

    boolean indexExists(Connection connection, IndexDefinition index) throws SQLException;

    String usernameCondition();

    String upsertTelegramLink();
}
