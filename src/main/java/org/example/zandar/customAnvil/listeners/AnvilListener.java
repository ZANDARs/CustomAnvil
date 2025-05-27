package org.example.zandar.customAnvil.listeners;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.example.zandar.customAnvil.CustomAnvil;
import org.example.zandar.customAnvil.utils.MessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnvilListener implements Listener {
    private final CustomAnvil plugin;
    private final Map<Material, List<Enchantment>> enchants = new HashMap<>();

    public AnvilListener(CustomAnvil plugin) {
        this.plugin = plugin;
        loadCustomEnchantments();
    }

    public void loadCustomEnchantments() {
        enchants.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("custom-item-enchants");
        if (section == null) {
            return;
        }
        for (String itemKey : section.getKeys(false)) {
            try {
                String materialName = itemKey.replace("minecraft:", "").toUpperCase();
                Material material = Material.valueOf(materialName);
                List<String> enchantKeys = section.getStringList(itemKey);
                if (enchantKeys.isEmpty()) {
                    continue;
                }
                List<Enchantment> enchantments = enchantKeys.stream()
                        .map(key -> {
                            try {
                                String[] parts = key.split(":");
                                String namespace = parts.length > 1 ? parts[0] : "minecraft";
                                String enchantKey = parts.length > 1 ? parts[1] : parts[0];
                                NamespacedKey namespacedKey = new NamespacedKey(namespace, enchantKey);
                                return Registry.ENCHANTMENT.get(namespacedKey);
                            } catch (Exception e) {
                                Map<String, String> placeholders = MessageUtils.createPlaceholders();
                                placeholders.put("key", key);
                                MessageUtils.log("invalid-enchant", placeholders);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();
                if (!enchantments.isEmpty()) {
                    enchants.put(material, enchantments);
                    Map<String, String> placeholders = MessageUtils.createPlaceholders();
                    placeholders.put("material", material.toString());
                    placeholders.put("enchantments", enchantments.toString());
                    MessageUtils.log("enchant-registered", placeholders);
                }
            } catch (IllegalArgumentException e) {
                Map<String, String> placeholders = MessageUtils.createPlaceholders();
                placeholders.put("key", itemKey);
                MessageUtils.log("invalid-material", placeholders);
            }
        }
    }

    @EventHandler
    public void customAnvil(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack first = inv.getFirstItem();
        ItemStack second = inv.getSecondItem();
        ItemStack result = e.getResult();
        if (second == null) {
            int cost = plugin.getConfig().getInt("rename-cost", 1);
            e.getView().setRepairCost(cost);
            return;
        }
        if (first != null && first.getType() == Material.ENCHANTED_BOOK && second.getType() == Material.ENCHANTED_BOOK) {
            return;
        }
        if (first != null && enchants.containsKey(first.getType())) {
            List<Enchantment> allowed = enchants.get(first.getType());
            if (second.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) second.getItemMeta();
                if (bookMeta == null) {
                    return;
                }
                ItemStack newResult = first.clone();
                ItemMeta meta = newResult.getItemMeta();
                if (meta == null) {
                    return;
                }
                int targetRepairCost = 0;
                if (meta instanceof Repairable repairable) {
                    if (repairable.hasRepairCost()) {
                        targetRepairCost = repairable.getRepairCost();
                    }
                }
                int sacrificeRepairCost = 0;
                if (bookMeta instanceof Repairable repairable) {
                    if (repairable.hasRepairCost()) {
                        sacrificeRepairCost = repairable.getRepairCost();
                    }
                }
                for (Map.Entry<Enchantment, Integer> entry : first.getEnchantments().entrySet()) {
                    meta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
                int enchantCost = 0;
                int newEnchants = 0;
                for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
                    Enchantment enchant = entry.getKey();
                    int lvl = entry.getValue();
                    if (allowed.contains(enchant)) {
                        if (!meta.hasEnchant(enchant)) {
                            meta.addEnchant(enchant, lvl, true);
                            enchantCost += lvl;
                            newEnchants++;
                        } else {
                            int currentLvl = meta.getEnchantLevel(enchant);
                            if (lvl > currentLvl) {
                                meta.addEnchant(enchant, lvl, true);
                                enchantCost += lvl;
                                newEnchants++;
                            }
                        }
                    }
                }
                int renameCost = 0;
                if (result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
                    if (!first.hasItemMeta() || !first.getItemMeta().hasDisplayName() ||
                            !first.getItemMeta().getDisplayName().equals(result.getItemMeta().getDisplayName())) {
                        renameCost = plugin.getConfig().getInt("rename-cost", 1);
                    }
                }
                if (newEnchants > 0) {
                    if (meta instanceof Repairable repairable) {
                        int newRepairCost = (targetRepairCost * 2) + 1;
                        repairable.setRepairCost(newRepairCost);
                    }
                    if (result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
                        meta.setDisplayName(result.getItemMeta().getDisplayName());
                    }
                    newResult.setItemMeta(meta);
                    e.setResult(newResult);
                    int totalCost = enchantCost + targetRepairCost + sacrificeRepairCost + renameCost;
                    e.getView().setRepairCost(totalCost);
                } else {
                    e.setResult(null);
                }
            } else {
                e.setResult(null);
            }
        } else if (first != null && second.getType() == Material.ENCHANTED_BOOK && result != null) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) second.getItemMeta();
            if (bookMeta == null) {
                return;
            }
            if (first.getType() == Material.ENCHANTED_BOOK) {
                return;
            }
            for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
                Enchantment enchant = entry.getKey();
                if (!enchant.canEnchantItem(first)) {
                    e.setResult(null);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void disableDamage(AnvilDamagedEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack second = inv.getSecondItem();
        if (second == null) {
            boolean anvilBreak = plugin.getConfig().getBoolean("disable-anvil-damage", true);
            e.setCancelled(anvilBreak);
        }
    }
}