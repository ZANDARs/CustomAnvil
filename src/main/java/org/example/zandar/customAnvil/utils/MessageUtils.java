package org.example.zandar.customAnvil.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final JavaPlugin plugin;
    private static FileConfiguration config;

    static {
        plugin = JavaPlugin.getProvidingPlugin(MessageUtils.class);
        config = plugin.getConfig();
    }

    public static void reloadConfig() {
        config = plugin.getConfig();
    }

    public static String getPrefix() {
        return config.getString("messages.prefix", "&6[CustomAnvil]&r ");
    }

    public static String formatMessage(String key, Map<String, String> placeholders) {
        String message = config.getString("messages." + key, key);
        if (placeholders != null) {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String placeholder = matcher.group(1);
                String replacement = placeholders.getOrDefault(placeholder, matcher.group());
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            message = sb.toString();
        }
        return translateColorCodes(message);
    }

    public static void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        String prefix = translateColorCodes(getPrefix());
        String message = formatMessage(key, placeholders);
        sender.sendMessage(prefix + message);
    }

    public static void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, null);
    }

    public static Map<String, String> createPlaceholders() {
        return new HashMap<>();
    }

    private static String translateColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(sb, ChatColor.COLOR_CHAR + "x" 
                + ChatColor.COLOR_CHAR + hexColor.charAt(0) 
                + ChatColor.COLOR_CHAR + hexColor.charAt(1)
                + ChatColor.COLOR_CHAR + hexColor.charAt(2)
                + ChatColor.COLOR_CHAR + hexColor.charAt(3)
                + ChatColor.COLOR_CHAR + hexColor.charAt(4)
                + ChatColor.COLOR_CHAR + hexColor.charAt(5));
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static void log(String key, Map<String, String> placeholders) {
        String message = config.getString("messages." + key, key);
        if (placeholders != null) {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String placeholder = matcher.group(1);
                String replacement = placeholders.getOrDefault(placeholder, matcher.group());
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            message = sb.toString();
        }
        message = ChatColor.stripColor(translateColorCodes(message));
        plugin.getLogger().info(message);
    }

    public static void log(String key) {
        log(key, null);
    }
}
