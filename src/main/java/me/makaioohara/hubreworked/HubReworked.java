package me.makaioohara.hubreworked;

import me.makaioohara.hubreworked.modifications.LaunchPad;
import me.makaioohara.hubreworked.modifications.ScoreboardModifier;
import me.makaioohara.hubreworked.modifications.TabModifier;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class HubReworked extends JavaPlugin {

    private final List<LaunchPad> activePlates = new ArrayList<>();
    private TabModifier tabListUpdater;

    public static boolean isPlaceholderApiAvailable;
    public static boolean isLuckPermsAvailable;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("hubreworkedreload").setExecutor(new ReloadCommand(this));

        // Plugin availability checks
        isPlaceholderApiAvailable = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        isLuckPermsAvailable = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;

        // Launchpad feature
        if (getConfig().getBoolean("launchpad-enabled", true)) {
            for (var worldName : getConfig().getStringList("launchpad-enabled-worlds")) {
                var world = Bukkit.getWorld(worldName);
                if (world != null) {
                    var plate = new LaunchPad(this, world);
                    plate.init();
                    activePlates.add(plate);
                } else {
                    getLogger().warning("World '" + worldName + "' not found.");
                }
            }
        }

        // ScoreboardModifier feature - requires only PlaceholderAPI
        if (getConfig().getBoolean("scoreboard-enabled", true)) {
            if (isPlaceholderApiAvailable) {
                getServer().getPluginManager().registerEvents(new ScoreboardModifier(this), this);

                Bukkit.getScheduler().runTaskTimer(this, () -> {
                    for (var player : Bukkit.getOnlinePlayers()) {
                        ScoreboardModifier.updateSidebarStatic(this, player);
                    }
                }, 20L, 20L);
            } else {
                getLogger().warning("Scoreboard feature is enabled but PlaceholderAPI is not found. Disabling scoreboard.");
            }
        }

        // TABModifier feature - requires both PlaceholderAPI and LuckPerms
        if (getConfig().getBoolean("tab-modify-enabled", true)) {
            if (isPlaceholderApiAvailable && isLuckPermsAvailable) {
                tabListUpdater = new TabModifier(this);
                tabListUpdater.start();
            } else {
                getLogger().warning("Tab list feature is enabled but either PlaceholderAPI or LuckPerms is missing. Disabling tab list.");
            }
        }

        getLogger().info("Hub Reworked has been enabled.");
    }

    @Override
    public void onDisable() {
        activePlates.forEach(HandlerList::unregisterAll);
        getLogger().info("Hub Reworked has been disabled.");
    }
}
