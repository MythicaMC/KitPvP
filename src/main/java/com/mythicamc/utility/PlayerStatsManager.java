package com.mythicamc.utility;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerStatsManager {

    private final HashMap<UUID, PlayerStats> playerStats = new HashMap<>();

    public void addKill(Player player) {
        PlayerStats stats = getPlayerStats(player);
        stats.kills++;
    }

    public void addDeath(Player player) {
        PlayerStats stats = getPlayerStats(player);
        stats.deaths++;
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
    }

    public void removeCoins(Player player, int amount) {
        PlayerStats stats = getPlayerStats(player);
        stats.coins = Math.max(0, stats.coins - amount);
    }

    public void initializePlayerStats(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerStats.containsKey(playerId)) {
            playerStats.put(playerId, new PlayerStats());
        }
    }

    private PlayerStats getPlayerStats(Player player) {
        return playerStats.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
    }

    private static class PlayerStats {
        int kills = 0;
        int deaths = 0;
        int coins = 0;
    }
}
