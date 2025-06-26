package me.makaioohara.hubreworked.modifications;

import me.makaioohara.hubreworked.HubReworked;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NametagModifier implements Listener {

    private final HubReworked plugin;
    private final Map<String, Team> teams = new HashMap<>();
    private Scoreboard scoreboard;

    private boolean luckPermsAvailable;
    private Object luckPerms;

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

    public NametagModifier(HubReworked plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        luckPermsAvailable = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");

        if (luckPermsAvailable) {
            try {
                Class<?> lpClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                Method getMethod = lpClass.getMethod("get");
                luckPerms = getMethod.invoke(null);
            } catch (Exception e) {
                luckPermsAvailable = false;
            }
        }

        scoreboard = plugin.getSharedScoreboard();
        refresh();
    }

    public void stop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeNametag(player);
        }

        teams.clear();
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    public void refresh() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyNametag(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyNametag(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeNametag(event.getPlayer());
    }

    private void applyNametag(Player player) {
        String prefix = "";

        if (luckPermsAvailable) {
            try {
                Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
                Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUniqueId());

                if (user != null) {
                    Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
                    Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
                    Object prefixObj = metaData.getClass().getMethod("getPrefix").invoke(metaData);

                    if (prefixObj != null) {
                        prefix = prefixObj.toString();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        String teamName = "nt_" + player.getName().substring(0, Math.min(14, player.getName().length()));
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        Component prefixComponent = deserializeMixedColors(prefix);
        team.prefix(prefixComponent);
        team.addEntry(player.getName());
        teams.put(player.getName(), team);
    }

    private void removeNametag(Player player) {
        String teamName = "nt_" + player.getName().substring(0, Math.min(14, player.getName().length()));

        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
            team.removeEntry(player.getName());
            if (team.getEntries().isEmpty()) {
                team.unregister();
            }
        }

        teams.remove(player.getName());
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
}