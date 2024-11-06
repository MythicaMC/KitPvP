package com.mythicamc.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerStatsManager {

    private final HashMap<UUID, PlayerStats> playerStats = new HashMap<>();
    private final DatabaseManager databaseManager;

    public PlayerStatsManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void addKill(Player player) {
        PlayerStats stats = getPlayerStats(player);
        stats.kills++;
        updateStatsInDatabase(player);
    }

    public void addDeath(Player player) {
        PlayerStats stats = getPlayerStats(player);
        stats.deaths++;
        updateStatsInDatabase(player);
    }

    public int getKills(Player player) {
        return getPlayerStats(player).kills;
    }

    public int getDeaths(Player player) {
        return getPlayerStats(player).deaths;
    }

    public double getKDR(Player player) {
        PlayerStats stats = getPlayerStats(player);
        if (stats.deaths == 0) {
            if (stats.kills == 0) {
                return 0.0; // No kills or deaths
            } else {
                return (double) stats.kills; // Deaths are zero, KDR equals kills
            }
        }
        return (double) stats.kills / stats.deaths;
    }

    public int getCoins(Player player) {
        return getPlayerStats(player).coins;
    }

    public void addCoins(Player player, int amount) {
        PlayerStats stats = getPlayerStats(player);
        stats.coins += amount;
        updateStatsInDatabase(player);
    }

    public void removeCoins(Player player, int amount) {
        PlayerStats stats = getPlayerStats(player);
        stats.coins = Math.max(0, stats.coins - amount);
        updateStatsInDatabase(player);
    }

    public void setCoins(Player player, int amount) {
        PlayerStats stats = getPlayerStats(player);
        stats.coins = Math.max(0, stats.coins - amount);
        updateStatsInDatabase(player);
    }

    public void initializePlayerStats(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerStats.containsKey(playerId)) {
            loadPlayerStatsFromDatabase(player);
        }
    }

    private PlayerStats getPlayerStats(Player player) {
        return playerStats.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
    }

    private void loadPlayerStatsFromDatabase(Player player) {
        UUID playerId = player.getUniqueId();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT kills, deaths, coins FROM players WHERE uuid = ?")) {
            statement.setString(1, playerId.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                PlayerStats stats = new PlayerStats();
                stats.kills = resultSet.getInt("kills");
                stats.deaths = resultSet.getInt("deaths");
                stats.coins = resultSet.getInt("coins");
                playerStats.put(playerId, stats);
            } else {
                playerStats.put(playerId, new PlayerStats());
                savePlayerStatsToDatabase(player);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Could not load player stats for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void savePlayerStatsToDatabase(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerStats stats = getPlayerStats(player);
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO players (uuid, name, kills, deaths, coins) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, playerId.toString());
            statement.setString(2, player.getName());
            statement.setInt(3, stats.kills);
            statement.setInt(4, stats.deaths);
            statement.setInt(5, stats.coins);
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Could not save player stats for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void updateStatsInDatabase(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerStats stats = getPlayerStats(player);
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE players SET kills = ?, deaths = ?, coins = ? WHERE uuid = ?")) {
            statement.setInt(1, stats.kills);
            statement.setInt(2, stats.deaths);
            statement.setInt(3, stats.coins);
            statement.setString(4, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Could not update player stats for " + player.getName() + ": " + e.getMessage());
        }
    }

    private static class PlayerStats {
        int kills = 0;
        int deaths = 0;
        int coins = 0;
    }
}
