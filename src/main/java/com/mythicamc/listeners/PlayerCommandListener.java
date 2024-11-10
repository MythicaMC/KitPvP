package com.mythicamc.listeners;

import com.mythicamc.managers.CombatManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class PlayerCommandListener implements Listener {

    private final CombatManager combatManager;

    public PlayerCommandListener(CombatManager combatManager) {
        this.combatManager = combatManager;
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
