package com.mythicamc.commands;

import com.mythicamc.KitPvP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    private final KitPvP plugin;

    public SpawnCommand(KitPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {
            // Check if player is in combat, if not start timer of 5 seconds before teleporting them to the spawn. If combattagged while waiting on this timer cancel the timer.
        }

        return true;
    }

    private Location getSpawnLocation() {
        FileConfiguration spawnConfig = plugin.getSpawnConfigManager().getConfig();
        String worldName = spawnConfig.getString("spawn.world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return null; // Handle this case if the world is not loaded
        }

        double x = spawnConfig.getDouble("spawn.x");
        double y = spawnConfig.getDouble("spawn.y");
        double z = spawnConfig.getDouble("spawn.z");
        float pitch = (float) spawnConfig.getDouble("spawn.pitch");
        float yaw = (float) spawnConfig.getDouble("spawn.yaw");

        return new Location(world, x, y, z, yaw, pitch);
    }
}
