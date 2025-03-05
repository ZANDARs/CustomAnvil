package org.example.zandar.customAnvil.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilListener implements Listener {
    @EventHandler
    public void customAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvilInventory = event.getInventory();

        ItemStack secondSlotItem = anvilInventory.getSecondItem();

        if (secondSlotItem == null) {
            event.getView().setRepairCost(1);
        }
    }
}
