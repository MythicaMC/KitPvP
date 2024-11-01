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
        validateConfig();

        // Initialize CombatManager
        combatManager = new CombatManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new LogoutListener(this), this);

        // Schedule a test task
        getServer().getScheduler().runTaskLater(this, () -> {
            getLogger().info("Scheduler is working!");
        }, 100L); // Runs after 5 seconds (100 ticks)
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("KitPvP is disabled!");
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    private void validateConfig() {
        long tagDuration = getConfig().getLong("combat.tag-duration", -1);
        if (tagDuration <= 0) {
            getLogger().severe("Invalid 'combat.tag-duration' in config.yml. It must be a positive number.");
        }

        String banDuration = getConfig().getString("combat.ban-duration", "");
        if (banDuration.isEmpty()) {
            getLogger().severe("Invalid 'combat.ban-duration' in config.yml. It cannot be empty.");
        }
    }
}
