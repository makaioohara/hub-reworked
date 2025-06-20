package me.makaioohara.hubreworked.modifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.makaioohara.hubreworked.HubReworked;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScoreboardModifier implements Listener {

    private final HubReworked plugin;

    public ScoreboardModifier(HubReworked plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("scoreboard-enabled", true)) {
            updateSidebar(event.getPlayer());
        }
    }

    public void updateSidebar(Player player) {
        updateSidebarStatic(plugin, player);
    }

    public static void updateSidebarStatic(HubReworked plugin, Player player) {
        FileConfiguration config = plugin.getConfig();

        boolean scoreboardEnabled = config.getBoolean("scoreboard-enabled", true);
        if (!scoreboardEnabled) return;

        String title = config.getString("scoreboard-features.title", "INFO");
        List<String> lines = config.getStringList("scoreboard-features.lines");

        if (lines.isEmpty()) return;

        String parsedTitle = replacePlaceholders(plugin, player, title);
        Scoreboard board = createScoreboard(plugin, player, lines, parsedTitle);

        if (player.isOnline() && board != null) {
            player.setScoreboard(board);
        } else {
            plugin.getLogger().warning("[HubReworked] Failed to apply scoreboard: player offline or board null.");
        }
    }

    private static Scoreboard createScoreboard(HubReworked plugin, Player player, List<String> lines, String title) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return null;

        Scoreboard board = manager.getNewScoreboard();
        String objectiveName = generateObjectiveName(player);

        Objective objective;
        try {
            objective = board.registerNewObjective(objectiveName, "dummy", stripHexColorCodes(title));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[HubReworked] Could not register objective: " + e.getMessage());
            return null;
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Set<String> usedEntries = new HashSet<>();
        int score = lines.size();
        int blankCounter = 0;

        for (String line : lines) {
            String parsed = parseLine(plugin, player, line, blankCounter);

            if (line.trim().isEmpty()) {
                blankCounter++;
            }

            if (usedEntries.add(parsed)) {
                objective.getScore(parsed).setScore(score--);
            }
        }

        return board;
    }

    private static String parseLine(HubReworked plugin, Player player, String line, int blankCounter) {
        if (line.trim().isEmpty()) {
            // Create a fake blank line that is still unique and not visible
            return ChatColor.RESET.toString() + ChatColor.COLOR_CHAR + Integer.toHexString(blankCounter);
        }

        String parsed = replacePlaceholders(plugin, player, line);
        return stripHexColorCodes(parsed);
    }

    private static String generateObjectiveName(Player player) {
        String uuidStr = player.getUniqueId().toString().replace("-", "");
        return ("hub_" + uuidStr).substring(0, Math.min(16, "hub_".length() + uuidStr.length()));
    }

    private static String replacePlaceholders(HubReworked plugin, Player player, String text) {
        String replaced = text
                .replace("%player%", player.getName())
                .replace("%ping%", String.valueOf(getPlayerPing(player)));

        if (HubReworked.isPlaceholderApiAvailable) {
            try {
                replaced = PlaceholderAPI.setPlaceholders(player, replaced);
            } catch (Exception e) {
                plugin.getLogger().warning("[HubReworked] PlaceholderAPI error: " + e.getMessage());
            }
        }

        return ChatColor.translateAlternateColorCodes('&', replaced);
    }

    private static int getPlayerPing(Player player) {
        try {
            return player.getPing();
        } catch (Throwable e) {
            return -1;
        }
    }

    private static String stripHexColorCodes(String input) {
        return input.replaceAll("(?i)&#[0-9A-F]{6}", "");
    }
}
