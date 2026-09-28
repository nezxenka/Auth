package org.nezxenka.auth.database;

import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import lombok.Getter;
import org.nezxenka.auth.config.settings.DatabaseSettings;
import org.nezxenka.auth.database.dialect.MySqlDialect;
import org.nezxenka.auth.database.dialect.SqlDialect;
import org.nezxenka.auth.database.dialect.SqliteDialect;
import org.nezxenka.auth.database.jdbc.JdbcTemplate;

@Getter
public final class Database implements AutoCloseable {

    private final HikariDataSource dataSource;
    private final SqlDialect dialect;
    private final JdbcTemplate jdbc;

    private Database(HikariDataSource dataSource, SqlDialect dialect) {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public static Database connect(DatabaseSettings settings, File dataFolder) {
        HikariDataSource dataSource = new DataSourceFactory(dataFolder).create(settings);
        SqlDialect dialect = switch (settings.type()) {
            case MYSQL -> new MySqlDialect();
            case SQLITE -> new SqliteDialect();
        };
        return new Database(dataSource, dialect);
    }

    @Override
    public void close() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
