package me.makaioohara.hubreworked.modifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.makaioohara.hubreworked.utils.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabModifier implements Listener {

    private final Plugin plugin;

    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(&[0-9a-fk-or])", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> LEGACY_TO_MINI;

    static {
        LEGACY_TO_MINI = new LinkedHashMap<>();
        LEGACY_TO_MINI.put("&0", "<black>");
        LEGACY_TO_MINI.put("&1", "<dark_blue>");
        LEGACY_TO_MINI.put("&2", "<dark_green>");
        LEGACY_TO_MINI.put("&3", "<dark_aqua>");
        LEGACY_TO_MINI.put("&4", "<dark_red>");
        LEGACY_TO_MINI.put("&5", "<dark_purple>");
        LEGACY_TO_MINI.put("&6", "<gold>");
        LEGACY_TO_MINI.put("&7", "<gray>");
        LEGACY_TO_MINI.put("&8", "<dark_gray>");
        LEGACY_TO_MINI.put("&9", "<blue>");
        LEGACY_TO_MINI.put("&a", "<green>");
        LEGACY_TO_MINI.put("&b", "<aqua>");
        LEGACY_TO_MINI.put("&c", "<red>");
        LEGACY_TO_MINI.put("&d", "<light_purple>");
        LEGACY_TO_MINI.put("&e", "<yellow>");
        LEGACY_TO_MINI.put("&f", "<white>");
        LEGACY_TO_MINI.put("&k", "<obfuscated>");
        LEGACY_TO_MINI.put("&l", "<bold>");
        LEGACY_TO_MINI.put("&m", "<strikethrough>");
        LEGACY_TO_MINI.put("&n", "<underline>");
        LEGACY_TO_MINI.put("&o", "<italic>");
        LEGACY_TO_MINI.put("&r", "<reset>");
    }

    public TabModifier(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        updatePlayerListNames();
    }

    public void stop() {
        HandlerList.unregisterAll(this);
    }

    public void updatePlayerListNames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String prefix = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
            if (prefix == null || prefix.trim().isEmpty()) {
                prefix = "&7";
            }

            String rawName = prefix + "%player% ";
            rawName = PlaceholderUtil.applyBuiltInPlaceholders(player, rawName);
            rawName = PlaceholderAPI.setPlaceholders(player, rawName);
            Component nameComponent = deserializeMixedColors(rawName);
            player.playerListName(nameComponent);
        }
    }

    private Component deserializeMixedColors(String input) {
        String converted = convertHexToMiniMessage(input);
        converted = convertLegacyToMiniMessage(converted);
        return MiniMessage.miniMessage().deserialize(converted);
    }

    private String convertHexToMiniMessage(String input) {
        return input.replaceAll("(?i)&#([0-9A-Fa-f]{6})", "<#$1>");
    }

    private String convertLegacyToMiniMessage(String input) {
        Matcher matcher = LEGACY_CODE_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String code = matcher.group(1).toLowerCase();
            String replacement = LEGACY_TO_MINI.getOrDefault(code, "");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerListNames();
    }
}