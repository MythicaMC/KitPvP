package com.mythicamc;

import com.mythicamc.listeners.LogoutListener;
import com.mythicamc.listeners.PvPListener;
import com.mythicamc.utility.CombatManager;
import com.mythicamc.utility.PlayerScoreboardManager;
import com.mythicamc.utility.PlayerStatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class KitPvP extends JavaPlugin {

    private CombatManager combatManager;
    private PlayerScoreboardManager scoreboardManager;
    private PlayerStatsManager statsManager;

    @Override
    public void onEnable() {
        // Save the default config if it doesn't exist
        saveDefaultConfig();

        // Validate the config for basic errors that cause errors
        validateConfig();

        // Initialize CombatManager, statsManager and scoreboardManager
        combatManager = new CombatManager(this);
        statsManager = new PlayerStatsManager();
        scoreboardManager = new PlayerScoreboardManager(this);

        // Start the scoreboard updater
        scoreboardManager.startScoreboardUpdater();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new LogoutListener(this), this);

        getLogger().info("KitPvP has been enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("KitPvP is disabled!");
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public PlayerScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public PlayerStatsManager getStatsManager() {
        return statsManager;
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
