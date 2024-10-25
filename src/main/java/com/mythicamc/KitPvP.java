package com.mythicamc;

import com.mythicamc.listeners.LogoutListener;
import com.mythicamc.listeners.PvPListener;
import com.mythicamc.utility.CombatManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class KitPvP extends JavaPlugin {

    private CombatManager combatManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("KitPvP is enabled!");

        // Save the default config if it doesn't exist
        saveDefaultConfig();

        // Initialize CombatManager
        combatManager = new CombatManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new LogoutListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("KitPvP is disabled!");
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }
}
