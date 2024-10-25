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

    public void tagPlayer(Player p) {
        long tagDuration = plugin.getConfig().getLong("combatlog.tag-duration") * 1000; // Convert to milliseconds
        combatTaggedPlayers.put(p.getUniqueId(), System.currentTimeMillis() + tagDuration);
    }

    public boolean isTagged(Player p) {
        UUID playerId = p.getUniqueId();
        if (!combatTaggedPlayers.containsKey(playerId)) {
            return false;
        }
        long tagExpiryTime = combatTaggedPlayers.get(playerId);
        if (System.currentTimeMillis() > tagExpiryTime) {
            combatTaggedPlayers.remove(playerId);
            return false;
        }
        return true;
    }

    public void removeTag(Player p) {
        combatTaggedPlayers.remove(p.getUniqueId());
    }
}
