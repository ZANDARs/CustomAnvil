package org.example.zandar.customAnvil;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.zandar.customAnvil.commands.CustomAnvilCommand;
import org.example.zandar.customAnvil.listeners.AnvilListener;
import org.example.zandar.customAnvil.utils.MessageUtils;

import java.util.Objects;

public final class CustomAnvil extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        AnvilListener listener = new AnvilListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
        CustomAnvilCommand cmd = new CustomAnvilCommand(this, listener);
        Objects.requireNonNull(getCommand("customanvil")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("customanvil")).setTabCompleter(cmd);
        MessageUtils.log("startup");
    }

    @Override
    public void onDisable() {
        MessageUtils.log("shutdown");
    }
}
