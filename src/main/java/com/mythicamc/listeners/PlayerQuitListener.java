package com.mythicamc.listeners;

import com.mythicamc.KitPvP;
import com.mythicamc.managers.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LogoutListener implements Listener {

    private final KitPvP plugin;
    private final CombatManager combatManager;

    public LogoutListener(KitPvP plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (combatManager.isTagged(p)) {
            handleCombatLog(p);
        } else {
            // Optional: Handle players who are not combat-tagged
        }

        // Clean up any combat tags
        combatManager.removeTag(p);
    }

    private void handleCombatLog(Player p) {
        p.setHealth(0.0);

        // Ban the player using LiteBans
        String banDuration = plugin.getConfig().getString("combat.ban-duration");
        String reason = "Combat Logging";

        // Execute LiteBans ban command
        String command = String.format("tempban %s %s %s", p.getName(), banDuration, reason);
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
    }
}
