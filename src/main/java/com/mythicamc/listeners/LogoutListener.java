package com.mythicamc.listeners;

import com.mythicamc.KitPvP;
import com.mythicamc.utility.CombatManager;
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


}
