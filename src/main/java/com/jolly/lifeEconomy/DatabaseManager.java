package com.jolly.lifeEconomy;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

/**
 * Universal DatabaseManager for LifeEconomy
 * Supports SQLite and MySQL
 * Async-safe for Folia
 */
public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection mysqlConnection;

    private final boolean useMySQL;
    private final String host, database, username, password;
    private final int port;
    private final File sqliteFile;

    public DatabaseManager(JavaPlugin plugin, boolean useMySQL,
                           String host, int port, String database,
                           String username, String password,
                           File sqliteFile) {
        this.plugin = plugin;
        this.useMySQL = useMySQL;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.sqliteFile = sqliteFile;
    }

    public boolean isMySQL() {
        return useMySQL;
    }

    // ================================
    // 🔹 Connection Handling
    // ================================

    /**
     * Get a connection. For MySQL, reuse a persistent connection.
     * For SQLite, always create a fresh connection (async safe).
     */
    public Connection getConnection() throws SQLException {
        if (useMySQL) {
            if (mysqlConnection != null && !mysqlConnection.isClosed()) {
                return mysqlConnection;
            }
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            mysqlConnection = DriverManager.getConnection(url, username, password);
            return mysqlConnection;
        } else {
            if (!sqliteFile.exists()) {
                try {
                    plugin.getDataFolder().mkdirs();
                    sqliteFile.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String url = "jdbc:sqlite:" + sqliteFile.getAbsolutePath();
            return DriverManager.getConnection(url);
        }
    }

    public void close() {
        try {
            if (mysqlConnection != null && !mysqlConnection.isClosed()) {
                mysqlConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================================
    // 🔹 Parameter Utility
    // ================================
    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    // ================================
    // 🔹 Safe Query Helpers (Auto-close)
    // ================================
    public <T> T querySafe(String sql, ResultProcessor<T> processor, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return processor.process(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int updateSafe(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ================================
    // 🔹 Async Helpers (Folia-safe)
    // ================================
    public CompletableFuture<Integer> updateSafeAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, params); // local to this async thread
                return ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        });
    }

    public <T> CompletableFuture<T> querySafeAsync(String sql, ResultProcessor<T> processor, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    return processor.process(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    // ================================
    // 🔹 Functional Interface
    // ================================
    @FunctionalInterface
    public interface ResultProcessor<T> {
        T process(ResultSet rs) throws SQLException;
    }
}
