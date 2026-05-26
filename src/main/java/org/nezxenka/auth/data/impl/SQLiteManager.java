package org.nezxenka.auth.data.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.nezxenka.auth.Auth;
import org.nezxenka.auth.DatabaseConfig;
import org.nezxenka.auth.data.DatabaseManager;

public class SQLiteManager implements DatabaseManager {

    private final String url;

    public SQLiteManager(DatabaseConfig config) {
        File dataFolder = Auth.getInstance().getDataFolder();
        this.url =
            "jdbc:sqlite:" +
            new File(dataFolder, config.getSqliteFilename()).getAbsolutePath();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public void createTable() {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS auth_users (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "last_login INTEGER DEFAULT 0, " +
                    "registered INTEGER DEFAULT 0, " +
                    "telegram_id INTEGER DEFAULT 0, " +
                    "link_code TEXT DEFAULT NULL, " +
                    "session_ip TEXT DEFAULT NULL, " +
                    "session_expiry INTEGER DEFAULT 0" +
                    ")"
            )
        ) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTelegramTable() {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS telegram_links (" +
                    "telegram_id INTEGER PRIMARY KEY, " +
                    "uuid TEXT NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "linked_at INTEGER DEFAULT 0" +
                    ")"
            )
        ) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRegistered(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT uuid FROM auth_users WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void register(String uuid, String username, String password) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO auth_users (uuid, username, password, registered) VALUES (?, ?, ?, ?)"
            )
        ) {
            stmt.setString(1, uuid);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPassword(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT password FROM auth_users WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updatePassword(String uuid, String password) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE auth_users SET password = ? WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, password);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateLastLogin(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE auth_users SET last_login = ? WHERE uuid = ?"
            )
        ) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveLinkCode(String uuid, String code) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE auth_users SET link_code = ? WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, code);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLinkCode(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT link_code FROM auth_users WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("link_code");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void linkTelegram(String uuid, long telegramId) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE auth_users SET telegram_id = ?, link_code = NULL WHERE uuid = ?"
            )
        ) {
            stmt.setLong(1, telegramId);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO telegram_links (telegram_id, uuid, username, linked_at) VALUES (?, ?, ?, ?)"
            )
        ) {
            stmt.setLong(1, telegramId);
            stmt.setString(2, uuid);
            stmt.setString(3, "");
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isTelegramLinked(long telegramId) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT uuid FROM telegram_links WHERE telegram_id = ?"
            )
        ) {
            stmt.setLong(1, telegramId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isTelegramLinkedByUUID(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT telegram_id FROM auth_users WHERE uuid = ? AND telegram_id != 0"
            )
        ) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Long getTelegramId(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT telegram_id FROM auth_users WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong("telegram_id");
                return id != 0 ? id : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getUUIDByTelegramId(long telegramId) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT uuid FROM telegram_links WHERE telegram_id = ?"
            )
        ) {
            stmt.setLong(1, telegramId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("uuid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void unlinkTelegram(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE auth_users SET telegram_id = 0 WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM telegram_links WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeLinkCode(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE auth_users SET link_code = NULL WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean is2FAEnabled(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT telegram_id FROM auth_users WHERE uuid = ? AND telegram_id != 0"
            )
        ) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void set2FAEnabled(String uuid, boolean enabled) {}

    @Override
    public void saveSession(String uuid, String ip, long expiry) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE auth_users SET session_ip = ?, session_expiry = ? WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, ip);
            stmt.setLong(2, expiry);
            stmt.setString(3, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSessionIp(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT session_ip FROM auth_users WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("session_ip");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getSessionExpiry(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT session_expiry FROM auth_users WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("session_expiry");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void clearSession(String uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE auth_users SET session_ip = NULL, session_expiry = 0 WHERE uuid = ?"
            )
        ) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {}
}
