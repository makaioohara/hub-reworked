package me.makaioohara.hubreworked.modifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.makaioohara.hubreworked.HubReworked;
import me.makaioohara.hubreworked.utils.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SidebarModifier {

    private static final Set<UUID> disabledPlayers = new HashSet<>();

    public static void toggleSidebar(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabledPlayers.contains(uuid)) {
            disabledPlayers.remove(uuid);
        } else {
            disabledPlayers.add(uuid);
        }
    }

    public static boolean isSidebarEnabled(Player player) {
        return !disabledPlayers.contains(player.getUniqueId());
    }

    public static void updateSidebarStatic(HubReworked plugin, Player player) {
        if (!isSidebarEnabled(player)) return;

        Scoreboard scoreboard = plugin.getSharedScoreboard();

        // Clear old sidebar
        Objective old = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (old != null) old.unregister();

        String rawTitle = plugin.getConfig().getString("sidebar.features.title", "&aHub Sidebar");
        String parsedTitle = PlaceholderUtil.applyBuiltInPlaceholders(player, rawTitle);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            parsedTitle = PlaceholderAPI.setPlaceholders(player, parsedTitle);
        }

        parsedTitle = ChatColor.translateAlternateColorCodes('&', parsedTitle);
        parsedTitle = stripHexColorCodes(parsedTitle);
        parsedTitle = preserveColorsAndUppercase(parsedTitle);

        Objective objective = scoreboard.registerNewObjective("sidebar", "dummy", parsedTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = plugin.getConfig().getStringList("sidebar.features.lines");

        int score = lines.size();
        Set<String> usedLines = new HashSet<>();

        for (String rawLine : lines) {
            String parsedLine = PlaceholderUtil.applyBuiltInPlaceholders(player, rawLine);

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                parsedLine = PlaceholderAPI.setPlaceholders(player, parsedLine);
            }

            parsedLine = stripHexColorCodes(parsedLine);
            parsedLine = " " + ChatColor.translateAlternateColorCodes('&', parsedLine);

            if (parsedLine.trim().isEmpty()) {
                parsedLine = ChatColor.values()[score % ChatColor.values().length] + " ";
            }

            if (parsedLine.length() > 40) {
                parsedLine = parsedLine.substring(0, 40);
            }

            if (usedLines.add(parsedLine)) {
                objective.getScore(parsedLine).setScore(score--);
            }
        }

        player.setScoreboard(scoreboard);
    }

    private static String stripHexColorCodes(String input) {
        return input.replaceAll("(?i)&#[0-9A-F]{6}", "");
    }

    private static String preserveColorsAndUppercase(String input) {
        StringBuilder result = new StringBuilder();
        boolean colorCode = false;

        for (char c : input.toCharArray()) {
            if (colorCode) {
                result.append(c);
                colorCode = false;
            } else if (c == ChatColor.COLOR_CHAR) {
                result.append(c);
                colorCode = true;
            } else {
                result.append(Character.toUpperCase(c));
            }
        }

        return result.toString();
    }
}
