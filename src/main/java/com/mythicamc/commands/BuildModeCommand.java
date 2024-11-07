package com.mythicamc.commands;

import com.mythicamc.utils.AntiGriefUtility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BuildModeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Check if the player has the necessary permission
            if (player.hasPermission("kitpvp.admin.build")) {
                UUID playerId = player.getUniqueId();

                // Toggle build mode for the player
                if (AntiGriefUtility.buildModePlayers.contains(playerId)) {
                    AntiGriefUtility.buildModePlayers.remove(playerId);
                    player.sendMessage("Build mode disabled.");
                } else {
                    AntiGriefUtility.buildModePlayers.add(playerId);
                    player.sendMessage("Build mode enabled.");
                }

                return true;
            } else {
                player.sendMessage("You do not have permission to use this command.");
            }
        } else {
            sender.sendMessage("Only players can use this command.");
        }
        return false;
    }
}
