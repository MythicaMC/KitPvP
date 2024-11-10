package com.mythicamc.managers;

import com.mythicamc.KitPvP;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {

    private final KitPvP plugin;
    private final HashMap<UUID, Long> combatTaggedPlayers; // Stores player's UUID and the timestamp when they were last tagged
    private final Map<UUID, BukkitTask> combatTagTimers = new HashMap<>();

    public CombatManager(KitPvP plugin) {
        this.plugin = plugin;
        this.combatTaggedPlayers = new HashMap<>();
    }

    public boolean tagPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long tagDuration = plugin.getConfig().getLong("combat.tag-duration") * 1000; // Convert to milliseconds
        if (tagDuration <= 0) {
            plugin.getLogger().warning("Invalid combat tag duration: " + tagDuration + "ms. Check your config.yml.");
            tagDuration = 15000; // Default to 15 seconds if invalid
        }
        long tagExpiryTime = currentTime + tagDuration;

        boolean wasAlreadyTagged = isTagged(player);

        combatTaggedPlayers.put(playerId, tagExpiryTime);
        startCombatTagTimer(player);

        return wasAlreadyTagged;
    }

    public boolean isTagged(Player player) {
        UUID playerId = player.getUniqueId();
        Long tagExpiryTime = combatTaggedPlayers.get(playerId);
        if (tagExpiryTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > tagExpiryTime) {
            combatTaggedPlayers.remove(playerId);
            return false;
        }
        return true;
    }

    public void removeTag(Player player) {
        cancelCombatTagTimer(player);
        combatTaggedPlayers.remove(player.getUniqueId());
    }

    public long getRemainingTagTime(Player player) {
        UUID playerId = player.getUniqueId();
        Long tagExpiryTime = combatTaggedPlayers.get(playerId);
        if (tagExpiryTime == null) {
            return 0;
        }
        long remainingTime = tagExpiryTime - System.currentTimeMillis();
        return Math.max(remainingTime, 0);
    }

    private void cancelCombatTagTimer(Player player) {
        UUID playerId = player.getUniqueId();
        if (combatTagTimers.containsKey(playerId)) {
            combatTagTimers.get(playerId).cancel();
            combatTagTimers.remove(playerId);
        }
    }

    private void startCombatTagTimer(Player player) {
        UUID playerId = player.getUniqueId();

        // Cancel any existing timer for the player
        if (combatTagTimers.containsKey(playerId)) {
            combatTagTimers.get(playerId).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    combatTagTimers.remove(playerId);
                    return;
                }

                long remainingTime = getRemainingTagTime(player);
                int secondsLeft = (int) Math.ceil(remainingTime / 1000.0);

                if (remainingTime <= 0) {
                    // Play sound
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    // Send action bar message
                    String actionBarMessage = ChatColor.translateAlternateColorCodes('&', "&aYou are no longer in combat!");
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));

                    // Send chat message
                    String untagMessage = plugin.getConfig().getString("messages.combat-untagged", "&aYou are no longer in combat!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', untagMessage));

                    // Remove combat tag
                    removeTag(player);

                    // Update scoreboard
                    plugin.getScoreboardManager().updateScoreboard(player);

                    // Cancel the timer
                    cancel();
                    combatTagTimers.remove(playerId);
                    return;
                }

                if (secondsLeft <= 3) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }

                String actionBarMessage = ChatColor.translateAlternateColorCodes('&', "&cIn combat: " + secondsLeft + " seconds!");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second

        // Store the task so we can cancel it later if needed
        combatTagTimers.put(playerId, task);
    }
}
