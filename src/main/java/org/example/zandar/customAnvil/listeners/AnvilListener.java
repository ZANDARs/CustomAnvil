package org.example.zandar.customAnvil.listeners;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.example.zandar.customAnvil.CustomAnvil;

public class AnvilListener implements Listener {
    private final CustomAnvil plugin;

    public AnvilListener(CustomAnvil plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void customAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvilInventory = event.getInventory();

        ItemStack secondSlotItem = anvilInventory.getSecondItem();

        if (secondSlotItem == null) {
            int newCost = plugin.getConfig().getInt("rename-cost", 1);
            event.getView().setRepairCost(newCost);
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
