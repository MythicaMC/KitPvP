package com.mythicamc.managers;

import com.mythicamc.KitPvP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerScoreboardManager {

    private final KitPvP plugin;
    private final HashMap<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final DecimalFormat kdrFormat = new DecimalFormat("#.##");

    public PlayerScoreboardManager(KitPvP plugin) {
        this.plugin = plugin;
    }

    public void createScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("kitpvp", "dummy", ChatColor.RED + "" + ChatColor.BOLD + "Mythica KitPvP");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Initialize scoreboard entries
        updateScoreboard(player, board);

        // Assign the scoreboard to the player
        player.setScoreboard(board);

        // Store the scoreboard for future updates
        playerScoreboards.put(player.getUniqueId(), board);
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        playerScoreboards.remove(player.getUniqueId());
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = playerScoreboards.get(player.getUniqueId());
        if (board != null) {
            updateScoreboard(player, board);
        }
    }

    private void updateScoreboard(Player player, Scoreboard board) {
        Objective objective = board.getObjective("kitpvp");
        if (objective == null) {
            return;
        }

        // Clear existing entries
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        // Generate unique empty lines
        String emptyLine1 = ChatColor.RESET.toString() + ChatColor.WHITE + " ";
        String emptyLine2 = ChatColor.RESET.toString() + ChatColor.WHITE + "  ";
        String emptyLine3 = ChatColor.RESET.toString() + ChatColor.WHITE + "   ";
        String emptyLine4 = ChatColor.RESET.toString() + ChatColor.WHITE + "    ";

        // Get actual stats
        int kills = plugin.getStatsManager().getKills(player);
        int deaths = plugin.getStatsManager().getDeaths(player);
        double kdr = plugin.getStatsManager().getKDR(player);
        int coins = plugin.getStatsManager().getCoins(player);

        // Format KDR to display two decimal places, even if zero
        String formattedKDR = kdrFormat.format(kdr);

        // Build the scoreboard entries
        Map<Integer, String> entries = new HashMap<>();

        entries.put(14, emptyLine1);
        entries.put(13, ChatColor.YELLOW + "Kills:");
        entries.put(12, ChatColor.WHITE + String.valueOf(kills) + " ");
        entries.put(11, emptyLine2);
        entries.put(10, ChatColor.YELLOW + "Deaths:");
        entries.put(9, ChatColor.WHITE + String.valueOf(deaths) + "  ");
        entries.put(8, emptyLine3);
        entries.put(7, ChatColor.YELLOW + "KDR:");
        entries.put(6, ChatColor.WHITE + formattedKDR + "   ");
        entries.put(5, ChatColor.YELLOW + "Coins:");
        entries.put(4, ChatColor.WHITE + String.valueOf(coins) + "    ");
        entries.put(3, emptyLine4);
        entries.put(2, ChatColor.YELLOW + "play.mythicamc.com");

        // Set the scores
        for (Map.Entry<Integer, String> entry : entries.entrySet()) {
            String entryText = entry.getValue();
            int scoreValue = entry.getKey();

            // Create the score entry
            Score score = objective.getScore(entryText);
            score.setScore(scoreValue);
        }
    }

    public void startScoreboardUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID playerId : playerScoreboards.keySet()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    updateScoreboard(player);
                }
            }
        }, 0L, 20L); // Update every second
    }
}
