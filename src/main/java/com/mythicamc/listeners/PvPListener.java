package com.mythicamc.listeners;

import com.mythicamc.KitPvP;
import com.mythicamc.utility.CombatManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PvPListener implements Listener {

    private final KitPvP plugin;
    private final CombatManager combatManager;

    private final Map<UUID, BukkitTask> combatTagTimers = new HashMap<>();
    private final Set<UUID> respawningPlayers = new HashSet<>();

    public PvPListener(KitPvP plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // Check if both entities are players
        if (e.getEntity() instanceof Player damaged && e.getDamager() instanceof Player damager) {
            // Tag both players
            boolean damagedWasTagged = combatManager.tagPlayer(damaged);
            boolean damagerWasTagged = combatManager.tagPlayer(damager);

            // Notify players only if they were not already tagged
            String tagMessage = plugin.getConfig().getString("messages.combat-tagged", "&cYou are now in combat!");
            if (!damagerWasTagged) {
                damager.playSound(damager.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&', tagMessage));
            }
            if (!damagedWasTagged) {
                damaged.playSound(damaged.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                damaged.sendMessage(ChatColor.translateAlternateColorCodes('&', tagMessage));
            }

            // Start combat tag timer in action bar
            startCombatTagTimer(damaged);
            startCombatTagTimer(damager);

            // Fake death behaviour
            double finalDamage = e.getFinalDamage();
            double health = damaged.getHealth();

            if (health - finalDamage <= 0) {
                e.setCancelled(true); // Prevent death

                // Determine the damager if killed by bow, by default we already have the damager
                if (damager instanceof Projectile projectile) {
                    if (projectile.getShooter() instanceof Player) {
                        damager = (Player) projectile.getShooter();
                    }
                }

                // Simulate death
                simulateDeath(damaged, damager);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e instanceof EntityDamageByEntityEvent) {
            return;
        }

        if (e.getEntity() instanceof Player victim) {
            double finalDamage = e.getFinalDamage();
            double health = victim.getHealth();

            if (health - finalDamage <= 0) {
                e.setCancelled(true); // Prevent death

                // Simulate death with no killer
                simulateDeath(victim, (Player) null);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        combatManager.removeTag(p);
        cancelCombatTagTimer(p);

        // Remove player's scoreboard
        plugin.getScoreboardManager().removeScoreboard(p);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        if (respawningPlayers.contains(player.getUniqueId())) {
            e.setCancelled(true);
        }
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Initialize stats
        plugin.getStatsManager().initializePlayerStats(player);

        // Create scoreboard
        plugin.getScoreboardManager().createScoreboard(player);

        // Update scoreboard
        plugin.getScoreboardManager().updateScoreboard(player);
    }

    private void cancelCombatTagTimer(Player p) {
        UUID playerId = p.getUniqueId();
        if (combatTagTimers.containsKey(playerId)) {
            combatTagTimers.get(playerId).cancel();
            combatTagTimers.remove(playerId);
        }
    }

    private void startCombatTagTimer(Player p) {
        UUID playerId = p.getUniqueId();

        // Cancel any existing timer for the player
        if (combatTagTimers.containsKey(playerId)) {
            combatTagTimers.get(playerId).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                    combatTagTimers.remove(playerId);
                    return;
                }

                long remainingTime = combatManager.getRemainingTagTime(p);
                int secondsLeft = (int) Math.ceil(remainingTime / 1000.0);

                if (remainingTime <= 0) {
                    // Play sound
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    // Send action bar message
                    String actionBarMessage = ChatColor.translateAlternateColorCodes('&', "&aYou are no longer in combat!");
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));

                    // Send chat message
                    String untagMessage = plugin.getConfig().getString("messages.combat-untagged", "&aYou are no longer in combat!");
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', untagMessage));

                    // Remove combat tag
                    combatManager.removeTag(p);

                    // Update scoreboard
                    plugin.getScoreboardManager().updateScoreboard(p);

                    // Cancel the timer
                    cancel();
                    combatTagTimers.remove(playerId);
                    return;
                }

                if (secondsLeft <= 3) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }

                String actionBarMessage = ChatColor.translateAlternateColorCodes('&', "&cIn combat: " + secondsLeft + " seconds!");
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second

        // Store the task so we can cancel it later if needed
        combatTagTimers.put(playerId, task);
    }

    private void startRespawnCountdown(Player player) {
        int countdownTime = plugin.getConfig().getInt("respawn.countdown", 3);

        // Add player to respawningPlayers set
        respawningPlayers.add(player.getUniqueId());

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

                    // Remove player from respawningPlayers set
                    respawningPlayers.remove(player.getUniqueId());

                    // Teleport player to spawn location
                    Location respawnLocation = player.getWorld().getSpawnLocation(); // Has to be replaced with config value
                    player.teleport(respawnLocation);

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
        cancelCombatTagTimer(victim);

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

        // Send messages to victim and killer
        sendDeathMessages(victim, killer);

        // Update stats
        plugin.getStatsManager().addDeath(victim);
        if (killer != null) {
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
