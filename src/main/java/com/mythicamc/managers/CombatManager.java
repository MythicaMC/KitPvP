package com.mythicamc.utility;

import com.mythicamc.KitPvP;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class CombatManager {

    private final KitPvP plugin;
    private final HashMap<UUID, Long> combatTaggedPlayers; // Stores player's UUID and the timestamp when they were last tagged

    public CombatManager(KitPvP plugin) {
        this.plugin = plugin;
        this.combatTaggedPlayers = new HashMap<>();
    }

    public boolean tagPlayer(Player p) {
        UUID playerId = p.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long tagDuration = plugin.getConfig().getLong("combat.tag-duration") * 1000; // Convert to milliseconds
        if (tagDuration <= 0) {
            plugin.getLogger().warning("Invalid combat tag duration: " + tagDuration + "ms. Check your config.yml.");
            tagDuration = 15000; // Default to 15 seconds if invalid
        }
        long tagExpiryTime = currentTime + tagDuration;

        boolean wasAlreadyTagged = isTagged(p);

        combatTaggedPlayers.put(playerId, tagExpiryTime);

        plugin.getLogger().info("Player " + p.getName() + " tagged. Expires at: " + tagExpiryTime);

        return wasAlreadyTagged;
    }

    public boolean isTagged(Player p) {
        UUID playerId = p.getUniqueId();
        Long tagExpiryTime = combatTaggedPlayers.get(playerId);
        if (tagExpiryTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > tagExpiryTime) {
            combatTaggedPlayers.remove(playerId);
            return false;
        }
        return true;
    }

    public void removeTag(Player p) {
        combatTaggedPlayers.remove(p.getUniqueId());
    }

    public long getRemainingTagTime(Player p) {
        UUID playerId = p.getUniqueId();
        Long tagExpiryTime = combatTaggedPlayers.get(playerId);
        if (tagExpiryTime == null) {
            return 0;
        }
        long remainingTime = tagExpiryTime - System.currentTimeMillis();
        return Math.max(remainingTime, 0);
    }
}
