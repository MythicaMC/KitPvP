package com.mythicamc.listeners;

import com.mythicamc.KitPvP;
import com.mythicamc.utility.CombatManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class PvPListener implements Listener {

    private final CombatManager combatManager;

    public PvPListener(KitPvP plugin) {
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // Check if both entities are players
        if (e.getEntity() instanceof Player damaged && e.getDamager() instanceof Player damager) {
            // Tag both players
            combatManager.tagPlayer(damaged);
            combatManager.tagPlayer(damager);

            // Notify players
            damager.sendMessage(ChatColor.RED + "You are now in combat!");
            damaged.sendMessage(ChatColor.RED + "You are now in combat!");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        combatManager.removeTag(p);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (combatManager.isTagged(p)) {
            // List of blocked commands
            List<String> blockedCommands = List.of("/spawn");
            String command = e.getMessage().split(" ")[0].toLowerCase();

            if (blockedCommands.contains(command)) {
                p.sendMessage(ChatColor.RED + "You cannot use that command while in combat!");
                e.setCancelled(true);
            }
        }
    }
}
