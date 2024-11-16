package com.mythicamc.listeners;

import com.mythicamc.KitPvP;
import com.mythicamc.managers.CombatManager;
import com.mythicamc.utils.KitSelectorGUI;
import com.mythicamc.utils.WorldGuardUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PvPListener implements Listener {

    private final KitPvP plugin;
    private final CombatManager combatManager;

    public PvPListener(KitPvP plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player damaged) {
            // Determine the source of the damage
            Entity damagerEntity = event.getDamager();
            Player damager = null;

            // If the damager is a player, assign directly
            if (damagerEntity instanceof Player) {
                damager = (Player) damagerEntity;
            }
            // If the damager is a projectile get its shooter
            else if (damagerEntity instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                }
            }

            // Check if event happens in spawn
            if (WorldGuardUtils.isInRegion(damager, "spawn") || WorldGuardUtils.isInRegion(damaged, "spawn")) {
                event.setCancelled(true);
                damager.sendMessage("PvP is not allowed in the spawn area.");
                return;
            }

            // Tag both players only if the damager is a player
            if (damager != null) {
                boolean damagedWasTagged = combatManager.tagPlayer(damaged);
                boolean damagerWasTagged = combatManager.tagPlayer(damager);

                // Notify players only if they were not already tagged
                String tagMessage = plugin.getConfig().getString("messages.combat-tagged", "&cYou are now in combat!");
                if (!damagerWasTagged) {
                    damager.sendMessage(ChatColor.translateAlternateColorCodes('&', tagMessage));
                }
                if (!damagedWasTagged) {
                    damaged.sendMessage(ChatColor.translateAlternateColorCodes('&', tagMessage));
                }
            }

            // Fake death behavior
            double finalDamage = event.getFinalDamage();
            double health = damaged.getHealth();

            // Check if the player would die from this damage
            if (health - finalDamage <= 0) {
                event.setCancelled(true); // Prevent actual death
                simulateDeath(damaged, damager); // Simulate death, passing damager (null if not player)
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        // Skip handling if this is an EntityDamageByEntityEvent to avoid duplication
        if (e instanceof EntityDamageByEntityEvent) {
            return;
        }

        // Check if the damaged entity is a player
        if (e.getEntity() instanceof Player victim) {
            double finalDamage = e.getFinalDamage();
            double health = victim.getHealth();

            // Check if the player would die from this damage
            if (health - finalDamage <= 0) {
                e.setCancelled(true); // Prevent actual death
                simulateDeath(victim, null); // Simulate death with no specific killer
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // TO DO: Check if player tries to leave spawn area without a kit selected.
    }

    private void startRespawnCountdown(Player player) {
        int countdownTime = plugin.getConfig().getInt("respawn.countdown", 3);

        // Set player to Spectator mode
        player.setGameMode(GameMode.SPECTATOR);

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = countdownTime;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    String title = ChatColor.translateAlternateColorCodes('&', "&cYOU DIED!");
                    String subtitle = ChatColor.translateAlternateColorCodes('&', "&eRespawning in " + timeLeft + "...");
                    player.sendTitle(title, subtitle, 0, 25, 5);

                    timeLeft--;
                } else {
                    // Set player back to Survival mode
                    player.setGameMode(GameMode.SURVIVAL);

                    // Teleport player to spawn location
                    Location respawnLocation = player.getWorld().getSpawnLocation(); // Has to be replaced with config value
                    player.teleport(respawnLocation);

                    // Give player the kit selector
                    KitSelectorGUI.givePlayerKitSelectorItem(player);

                    // Send a title upon respawn
                    String respawnTitle = ChatColor.translateAlternateColorCodes('&', "&aReady to fight.");
                    player.sendTitle(respawnTitle, "", 10, 70, 20);

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void simulateDeath(Player victim, Player killer) {
        // Remove combat tag and cancel timers
        combatManager.removeTag(victim);

        // Send action bar and chat message
        victim.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou are no longer in combat!"));
        String actionBarMessage = ChatColor.translateAlternateColorCodes('&', "&aYou are no longer in combat!");
        victim.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));

        // Clear effects and set health
        victim.setHealth(20);
        victim.setFoodLevel(20);
        victim.setFireTicks(0);
        victim.getActivePotionEffects().forEach(potionEffect -> victim.removePotionEffect(potionEffect.getType()));

        // Handle inventory and experience
        victim.getInventory().clear();
        victim.setExp(0);
        victim.setLevel(0);

        // Update stats
        plugin.getStatsManager().addDeath(victim);
        if (killer != null) {
            // Send messages to victim and killer
            sendDeathMessages(victim, killer);

            plugin.getStatsManager().addKill(killer);

            // Reward killer with coins
            int killReward = plugin.getConfig().getInt("kill-reward", 10);
            plugin.getStatsManager().addCoins(killer, killReward);

            // Notify killer
            killer.sendMessage(ChatColor.GREEN + "You received " + killReward + " coins for killing " + victim.getName() + "!");
        }

        // Update the scoreboards
        plugin.getScoreboardManager().updateScoreboard(victim);
        if (killer != null) {
            plugin.getScoreboardManager().updateScoreboard(killer);
        }

        // Start respawn countdown
        startRespawnCountdown(victim);
    }

    private void sendDeathMessages(Player victim, Player killer) {
        String victimMessage;
        String killerMessage;

        if (killer != null) {
            victimMessage = ChatColor.translateAlternateColorCodes('&', "&cYou were killed by &e" + killer.getName() + "&c!");
            killerMessage = ChatColor.translateAlternateColorCodes('&', "&aYou killed &e" + victim.getName() + "&a!");
        } else {
            // If no killer (e.g., environmental damage)
            victimMessage = ChatColor.translateAlternateColorCodes('&', "&cYou died.");
            killerMessage = null;
        }

        // Send message to victim
        victim.sendMessage(victimMessage);

        // Send message to killer if applicable
        if (killerMessage != null) {
            killer.sendMessage(killerMessage);
        }
    }
}
