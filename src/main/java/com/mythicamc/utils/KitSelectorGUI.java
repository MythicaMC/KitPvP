package com.mythicamc.utils;

import com.mythicamc.KitPvP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitSelectorGUI implements Listener {

    private static final Map<String, FileConfiguration> kits = new HashMap<>();

    public KitSelectorGUI(KitPvP plugin) {
        loadKits(plugin);
    }

    public static void loadKits(KitPvP plugin) {
        File kitsFolder = new File(plugin.getDataFolder(), "kits");
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
            createDefaultKit(kitsFolder);
        }

        File[] kitFiles = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (kitFiles != null) {
            for (File kitFile : kitFiles) {
                FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);
                String kitId = kitConfig.getString("kitId", kitFile.getName().replace(".yml", "").toLowerCase());
                kits.put(kitId, kitConfig);
            }
        }
    }

    private static void createDefaultKit(File kitsFolder) {
        File defaultKitFile = new File(kitsFolder, "default_kit.yml");
        if (!defaultKitFile.exists()) {
            FileConfiguration defaultKit = YamlConfiguration.loadConfiguration(defaultKitFile);
            defaultKit.set("kitId", "default_kit"); // Unique ID for the kit
            defaultKit.set("name", "§bDefault Kit");
            defaultKit.set("type", "free");
            defaultKit.set("icon", "IRON_SWORD");
            defaultKit.set("lore", List.of("§7A basic starter kit", "§7Contains essential items."));

            // Items
            Map<String, Object> sword = new HashMap<>();
            sword.put("material", "IRON_SWORD");
            sword.put("amount", 1);
            sword.put("name", "§bStarter Sword");
            sword.put("lore", List.of("§7A trusty iron sword."));
            sword.put("enchantments", Map.of("DAMAGE_ALL", 1));

            Map<String, Object> apple = new HashMap<>();
            apple.put("material", "GOLDEN_APPLE");
            apple.put("amount", 3);

            defaultKit.set("items", List.of(sword, apple));

            // Potions
            Map<String, Object> speedPotion = new HashMap<>();
            speedPotion.put("type", "SPEED");
            speedPotion.put("duration", 600);
            speedPotion.put("amplifier", 1);
            speedPotion.put("ambient", false);
            speedPotion.put("particles", true);

            defaultKit.set("potions", List.of(speedPotion));

            try {
                defaultKit.save(defaultKitFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void openKitSelector(Player player) {
        Inventory kitSelector = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', "&6Select your Kit!"));

        // Add gray stained-glass panes as border
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = grayPane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.setDisplayName(" ");
            grayPane.setItemMeta(paneMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                kitSelector.setItem(i, grayPane);
            }
        }

        // Add barrier item to close the menu
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cClose"));
            closeItem.setItemMeta(closeMeta);
        }
        kitSelector.setItem(49, closeItem);

        // Add kits to the inventory in the specified order
        int freeSlot = 20;
        int coinSlot = 22;
        int rankSlot = 24;

        for (Map.Entry<String, FileConfiguration> entry : kits.entrySet()) {
            FileConfiguration kitConfig = entry.getValue();
            String kitName = kitConfig.getString("name", "Unknown Kit");
            String type = kitConfig.getString("type", "free");

            ItemStack kitItem = new ItemStack(Material.valueOf(kitConfig.getString("icon", "IRON_SWORD")));
            ItemMeta kitMeta = kitItem.getItemMeta();
            if (kitMeta != null) {
                kitMeta.setDisplayName(kitName);
                kitMeta.setLore(List.of("kitId:" + entry.getKey())); // Set kitId in lore
                kitItem.setItemMeta(kitMeta);
            }

            switch (type.toLowerCase()) {
                case "free":
                    kitSelector.setItem(freeSlot++, kitItem);
                    break;
                case "coin":
                    kitSelector.setItem(coinSlot++, kitItem);
                    break;
                case "rank":
                    kitSelector.setItem(rankSlot++, kitItem);
                    break;
            }
        }

        player.openInventory(kitSelector);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || !(event.getAction().name().contains("RIGHT_CLICK_AIR") || event.getAction().name().contains("RIGHT_CLICK_BLOCK"))) {
            return;
        }
        if (!event.hasItem() || !event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.CHEST && item.getItemMeta() != null && ChatColor.translateAlternateColorCodes('&', "&6&lKit Selector").equals(item.getItemMeta().getDisplayName())) {
            openKitSelector(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void handleInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&6Select your Kit!"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                List<String> lore = clickedItem.getItemMeta().getLore();
                if (lore != null && !lore.isEmpty()) {
                    String kitId = lore.get(0).replace("kitId:", "").toLowerCase();
                    FileConfiguration selectedKit = kits.get(kitId);

                    if (selectedKit == null) {
                        player.sendMessage("§cKit configuration for '" + kitId + "' not found.");
                        return;
                    }

                    List<Map<?, ?>> items = selectedKit.getMapList("items");
                    for (Map<?, ?> itemData : items) {
                        try {
                            Material material = Material.getMaterial((String) itemData.get("material"));
                            if (material == null) {
                                player.sendMessage("§cInvalid material in kit: " + itemData.get("material"));
                                continue;
                            }
                            int amount = itemData.containsKey("amount") ? ((Number) itemData.get("amount")).intValue() : 1;
                            ItemStack item = new ItemStack(material, amount);

                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null) {
                                if (itemData.containsKey("name")) {
                                    itemMeta.setDisplayName((String) itemData.get("name"));
                                }
                                if (itemData.containsKey("lore")) {
                                    itemMeta.setLore((List<String>) itemData.get("lore"));
                                }
                                if (itemData.containsKey("enchantments")) {
                                    Map<String, Integer> enchantments = (Map<String, Integer>) itemData.get("enchantments");
                                    enchantments.forEach((enchantment, level) -> {
                                        itemMeta.addEnchant(org.bukkit.enchantments.Enchantment.getByName(enchantment), level, true);
                                    });
                                }
                                item.setItemMeta(itemMeta);
                            }

                            // Debug: Confirm item before adding
                            player.sendMessage("§aAdding item: " + item.getType() + " x" + amount);
                            player.getInventory().addItem(item);

                        } catch (Exception e) {
                            player.sendMessage("§cError adding item to inventory.");
                            e.printStackTrace();
                        }
                    }

                    // Apply potion effects if any
                    List<Map<?, ?>> potions = selectedKit.getMapList("potions");
                    for (Map<?, ?> potionData : potions) {
                        PotionEffectType effectType = PotionEffectType.getByName((String) potionData.get("type"));
                        if (effectType == null) {
                            player.sendMessage("§cInvalid potion effect type in kit: " + potionData.get("type"));
                            continue;
                        }

                        int duration = potionData.containsKey("duration") ? ((Number) potionData.get("duration")).intValue() : 200;
                        int amplifier = potionData.containsKey("amplifier") ? ((Number) potionData.get("amplifier")).intValue() : 1;
                        boolean ambient = potionData.containsKey("ambient") ? (Boolean) potionData.get("ambient") : false;
                        boolean particles = potionData.containsKey("particles") ? (Boolean) potionData.get("particles") : true;

                        PotionEffect potionEffect = new PotionEffect(effectType, duration, amplifier, ambient, particles);
                        player.addPotionEffect(potionEffect);
                        player.sendMessage("§aAdded potion effect: " + effectType.getName());
                    }

                    player.sendMessage("§aKit selected successfully!");
                    player.closeInventory();
                }
            }
        }
    }

    public static void handleInventoryClose(InventoryCloseEvent event) {
        // Optionally handle inventory close events if needed
    }
}
