package com.mythicamc.managers;

import com.mythicamc.KitPvP;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final KitPvP plugin;
    private Connection connection;
    private DatabaseType databaseType;

    public enum DatabaseType {
        MYSQL, SQLITE
    }

    public DatabaseManager(KitPvP plugin, DatabaseType databaseType) {
        this.plugin = plugin;
        this.databaseType = databaseType;
    }

    public void connect() {
        try {
            if (databaseType == DatabaseType.MYSQL) {
                String host = plugin.getConfig().getString("database.mysql.host");
                String port = plugin.getConfig().getString("database.mysql.port");
                String database = plugin.getConfig().getString("database.mysql.database");
                String username = plugin.getConfig().getString("database.mysql.username");
                String password = plugin.getConfig().getString("database.mysql.password");
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                connection = DriverManager.getConnection(url, username, password);
                Bukkit.getLogger().info("Connected to MySQL database.");
            } else if (databaseType == DatabaseType.SQLITE) {
                String dbPath = plugin.getDataFolder() + "/database.sqlite";
                String url = "jdbc:sqlite:" + dbPath;

                connection = DriverManager.getConnection(url);
                Bukkit.getLogger().info("Connected to SQLite database.");
            }

            if (connection != null) {
                initializeTables();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Could not connect to database: " + e.getMessage());
        }
    }

    private void initializeTables() {
        try (Statement statement = connection.createStatement()) {
            String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(16), " +
                    "kills INT DEFAULT 0, " +
                    "deaths INT DEFAULT 0, " +
                    "score INT DEFAULT 0, " +
                    "coins INT DEFAULT 0);";
            statement.execute(createPlayersTable);
            Bukkit.getLogger().info("Database tables initialized.");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Could not initialize database: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connect();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[MythicaKitPvP] Could not re-establish the database connection: " + e.getMessage());
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                Bukkit.getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Could not close database connection: " + e.getMessage());
            }
        }
    }
}
