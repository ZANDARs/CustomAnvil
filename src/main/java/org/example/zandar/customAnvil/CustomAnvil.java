package org.example.zandar.customAnvil;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.zandar.customAnvil.listeners.AnvilListener;

public final class CustomAnvil extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(new AnvilListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
