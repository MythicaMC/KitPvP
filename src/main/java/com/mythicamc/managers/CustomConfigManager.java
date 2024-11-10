package com.mythicamc.managers;

import com.mythicamc.KitPvP;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomConfigManager {
    private final KitPvP plugin;
    private final String fileName;
    private File configFile;
    private FileConfiguration config;


    public CustomConfigManager(KitPvP plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        createConfig();
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), fileName);

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("An error occurred while trying to save a custom config: " + e.getMessage());
        }
    }
}
