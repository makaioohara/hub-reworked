package me.makaioohara.hubreworked.modifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.makaioohara.hubreworked.utils.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NametagModifier implements Listener {

    private final Plugin plugin;
    private final Map<String, Team> playerTeams = new HashMap<>();

    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(&[0-9a-fk-or])", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> LEGACY_TO_MINI;

    static {
        LEGACY_TO_MINI = Map.ofEntries(
                Map.entry("&0", "<black>"),
                Map.entry("&1", "<dark_blue>"),
                Map.entry("&2", "<dark_green>"),
                Map.entry("&3", "<dark_aqua>"),
                Map.entry("&4", "<dark_red>"),
                Map.entry("&5", "<dark_purple>"),
                Map.entry("&6", "<gold>"),
                Map.entry("&7", "<gray>"),
                Map.entry("&8", "<dark_gray>"),
                Map.entry("&9", "<blue>"),
                Map.entry("&a", "<green>"),
                Map.entry("&b", "<aqua>"),
                Map.entry("&c", "<red>"),
                Map.entry("&d", "<light_purple>"),
                Map.entry("&e", "<yellow>"),
                Map.entry("&f", "<white>"),
                Map.entry("&k", "<obfuscated>"),
                Map.entry("&l", "<bold>"),
                Map.entry("&m", "<strikethrough>"),
                Map.entry("&n", "<underline>"),
                Map.entry("&o", "<italic>"),
                Map.entry("&r", "<reset>")
        );
    }

    public NametagModifier(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateNametag(player);
        }
    }

    public void stop() {
        HandlerList.unregisterAll(this);
        playerTeams.clear();
    }

    public void updateNametag(Player player) {
        if (Bukkit.getOnlinePlayers().size() <= 1) {
            clearNametag(player);
            return;
        }

        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        Team team = scoreboard.getTeam(player.getName());
        if (team == null) {
            team = scoreboard.registerNewTeam(player.getName());
            playerTeams.put(player.getName(), team);
        }

        String prefix = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "&7";
        }

        String formatted = PlaceholderUtil.applyBuiltInPlaceholders(player, prefix + "%player%");
        formatted = PlaceholderAPI.setPlaceholders(player, formatted);

        String miniMessage = convertHexAndLegacyToMiniMessage(formatted);
        Component component = MiniMessage.miniMessage().deserialize(miniMessage);
        String legacyColored = LegacyComponentSerializer.legacySection().serialize(component);

        team.setPrefix(legacyColored);
        team.setSuffix("");

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        try {
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        } catch (NoSuchMethodError ignored) {
        }

        player.setScoreboard(scoreboard);
    }

    private void clearNametag(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard != null) {
            Team team = scoreboard.getTeam(player.getName());
            if (team != null) {
                team.removeEntry(player.getName());
                team.unregister();
            }
        }
        playerTeams.remove(player.getName());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private String convertHexAndLegacyToMiniMessage(String input) {
        String converted = input.replaceAll("(?i)&#([0-9A-Fa-f]{6})", "<#$1>");
        return convertLegacyToMiniMessage(converted);
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
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                updateNametag(online);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clearNametag(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                updateNametag(online);
            }
        }, 1L);
    }
}
