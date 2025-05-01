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
    public void customAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack firstItem = anvil.getFirstItem();
        ItemStack secondItem = anvil.getSecondItem();
        ItemStack resultItem = event.getResult();
        if (secondItem == null) {
            int cost = plugin.getConfig().getInt("rename-cost", 1);
            event.getView().setRepairCost(cost);
            return;
        }
        if (firstItem != null && enchants.containsKey(firstItem.getType())) {
            List<Enchantment> allowed = enchants.get(firstItem.getType());
            if (secondItem.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) secondItem.getItemMeta();
                if (bookMeta == null) {
                    return;
                }
                ItemStack newResult = firstItem.clone();
                ItemMeta resultMeta = newResult.getItemMeta();
                if (resultMeta == null) {
                    return;
                }
                for (Map.Entry<Enchantment, Integer> entry : firstItem.getEnchantments().entrySet()) {
                    resultMeta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
                boolean applied = false;
                for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
                    Enchantment enchant = entry.getKey();
                    int level = entry.getValue();
                    if (allowed.contains(enchant)) {
                        resultMeta.addEnchant(enchant, level, true);
                        applied = true;
                    }
                }
                if (applied) {
                    if (resultItem != null && resultItem.hasItemMeta() && 
                        resultItem.getItemMeta().hasDisplayName()) {
                        resultMeta.setDisplayName(resultItem.getItemMeta().getDisplayName());
                    }
                    newResult.setItemMeta(resultMeta);
                    event.setResult(newResult);
                    int baseCost = 1;
                    int enchantCost = bookMeta.getStoredEnchants().size() * 2;
                    event.getView().setRepairCost(baseCost + enchantCost);
                } else {
                    event.setResult(null);
                }
            } else {
                event.setResult(null);
            }
        }
        else if (firstItem != null && secondItem.getType() == Material.ENCHANTED_BOOK && resultItem != null) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) secondItem.getItemMeta();
            if (bookMeta == null) {
                return;
            }
            for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
                Enchantment enchant = entry.getKey();
                if (!enchant.canEnchantItem(firstItem)) {
                    event.setResult(null);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void disableDamage(AnvilDamagedEvent event) {
        AnvilInventory anvilInventory = event.getInventory();
        ItemStack secondSlotItem = anvilInventory.getSecondItem();
        if (secondSlotItem == null) {
            boolean anvilBreak = plugin.getConfig().getBoolean("disable-anvil-damage", true);
            event.setCancelled(anvilBreak);
        }
    }
}
