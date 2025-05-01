package org.example.zandar.customAnvil.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.example.zandar.customAnvil.CustomAnvil;
import org.example.zandar.customAnvil.listeners.AnvilListener;
import org.example.zandar.customAnvil.utils.MessageUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomAnvilCommand implements CommandExecutor, TabCompleter {
    private final CustomAnvil plugin;
    private final AnvilListener listener;

    public CustomAnvilCommand(CustomAnvil plugin, AnvilListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            Map<String, String> placeholders = MessageUtils.createPlaceholders();
            placeholders.put("command", label);
            MessageUtils.sendMessage(sender, "usage", placeholders);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("customanvil.reload")) {
                MessageUtils.sendMessage(sender, "reload-permission-error");
                return true;
            }
            plugin.reloadConfig();
            MessageUtils.reloadConfig();
            listener.loadCustomEnchantments();
            MessageUtils.sendMessage(sender, "reload-success");
            return true;
        }
        Map<String, String> placeholders = MessageUtils.createPlaceholders();
        placeholders.put("command", label);
        MessageUtils.sendMessage(sender, "unknown-command", placeholders);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                tab.add("reload");
            }
        }
        return tab;
    }
}
