package com.mythicamc.listeners;

import com.mythicamc.KitPvP;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerJoinListener implements Listener {

    private final KitPvP plugin;

    public PlayerJoinListener(KitPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Set join message
        String joinText = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.join-message", "&8[&2+&8] &7%player_name% &7has joined the server."));
        joinText = PlaceholderAPI.setPlaceholders(player, joinText);

        event.setJoinMessage(joinText);

        // Initialize stats
        plugin.getStatsManager().initializePlayerStats(player);

        // Create scoreboard
        plugin.getScoreboardManager().createScoreboard(player);

        // Update scoreboard
        plugin.getScoreboardManager().updateScoreboard(player);

        // Give player a chest item for kit selection
        ItemStack kitSelector = new ItemStack(Material.CHEST);
        ItemMeta meta = kitSelector.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lKit Selector"));
            kitSelector.setItemMeta(meta);
        }

        player.getInventory().setItem(0, kitSelector);
    }
}
