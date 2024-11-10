package com.mythicamc;

import com.mythicamc.commands.BuildModeCommand;
import com.mythicamc.listeners.PlayerQuitListener;
import com.mythicamc.listeners.PlayerJoinListener;
import com.mythicamc.listeners.PvPListener;
import com.mythicamc.managers.*;
import com.mythicamc.utils.AntiGriefUtility;
import com.mythicamc.utils.KitSelectorGUI;
import org.bukkit.plugin.java.JavaPlugin;

public final class KitPvP extends JavaPlugin {

    private CombatManager combatManager;
    private PlayerScoreboardManager scoreboardManager;
    private PlayerStatsManager statsManager;
    private DatabaseManager databaseManager;
    private CustomConfigManager spawnConfigManager;

    @Override
    public void onEnable() {
        // Save the default config if it doesn't exist
        saveDefaultConfig();

        // Initialize DatabaseManager
        DatabaseManager.DatabaseType dbType = getConfig().getString("database.type").equalsIgnoreCase("mysql") ? DatabaseManager.DatabaseType.MYSQL : DatabaseManager.DatabaseType.SQLITE;
        databaseManager = new DatabaseManager(this, dbType);
        databaseManager.connect();

        // Initialize other managers
        combatManager = new CombatManager(this);
        statsManager = new PlayerStatsManager(databaseManager);
        scoreboardManager = new PlayerScoreboardManager(this);

        // Initialize custom config managers
        spawnConfigManager = new CustomConfigManager(this, "spawn.yml");

        // Start the scoreboard updater
        scoreboardManager.startScoreboardUpdater();

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        // Tell console we're running!
        getLogger().info("KitPvP has been enabled.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info("KitPvP is disabled!");
    }

    private void registerCommands() {
        // Register command
        getCommand("kitpvpbuild").setExecutor(new BuildModeCommand());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new KitSelectorGUI(this), this);
        getServer().getPluginManager().registerEvents(new AntiGriefUtility(), this);
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

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    // Custom configs
    public CustomConfigManager getSpawnConfigManager() {
        return spawnConfigManager;
    }
}
