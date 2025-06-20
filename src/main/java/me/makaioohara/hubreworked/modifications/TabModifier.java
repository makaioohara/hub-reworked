package me.makaioohara.hubreworked.modifications;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabModifier implements Listener {

    private final Plugin plugin;

    public TabModifier(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Call this method once to register events and update player list names immediately.
     */
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        updatePlayerListNames();
    }

    /**
     * Update all online players' list names with prefixes and color codes.
     */
    public void updatePlayerListNames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String prefix = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
            if (prefix == null || prefix.trim().isEmpty()) {
                prefix = "&7"; // default gray prefix
            }

            String formattedName = translateColors(prefix + player.getName());
            player.setPlayerListName(formattedName);
        }
    }

    /**
     * Translates hex color codes (&#FFFFFF) and legacy '&' codes into ChatColor format.
     *
     * @param input The raw string with placeholders and color codes.
     * @return Colored string with proper Minecraft color codes.
     */
    private String translateColors(String input) {
        // First replace all hex color codes using regex
        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            // Replace with BungeeCord ChatColor of hex value
            matcher.appendReplacement(sb, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(sb);

        // Now translate remaining legacy '&' color codes to '§'
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Update all player list names when a player joins (including the joiner)
        updatePlayerListNames();
    }
}
