package me.makaioohara.hubreworked;

import me.makaioohara.hubreworked.commands.ReloaderCommand;
import me.makaioohara.hubreworked.commands.SidebarCommand;
import me.makaioohara.hubreworked.modifications.LaunchpadModifier;
import me.makaioohara.hubreworked.modifications.NametagModifier;
import me.makaioohara.hubreworked.modifications.SidebarModifier;
import me.makaioohara.hubreworked.modifications.TabModifier;
import me.makaioohara.hubreworked.modifications.NametagModifier;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class HubReworked extends JavaPlugin {

    private final List<LaunchpadModifier> activePlates = new ArrayList<>();
    private TabModifier tablistUpdater;
    private NametagModifier nameTagUpdater;

    public static boolean isPlaceholderApiAvailable;
    public static boolean isLuckPermsAvailable;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("hwreload").setExecutor(new ReloaderCommand(this));
        getCommand("sidebar").setExecutor(new SidebarCommand(this));

        isPlaceholderApiAvailable = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        isLuckPermsAvailable = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;

        reloadFeatures();

        getLogger().info("Hub Reworked has been enabled.");
    }

    @Override
    public void onDisable() {
        activePlates.clear();

        if (tablistUpdater != null) {
            tablistUpdater.stop();
            tablistUpdater = null;
        }

        if (nameTagUpdater != null) {
            nameTagUpdater.stop();
            nameTagUpdater = null;
        }

        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("Hub Reworked has been disabled.");
    }

    public void reloadFeatures() {
        activePlates.clear();

        Bukkit.getScheduler().cancelTasks(this);

        if (tablistUpdater != null) {
            tablistUpdater.stop();
            tablistUpdater = null;
        }

        if (nameTagUpdater != null) {
            nameTagUpdater.stop();
            nameTagUpdater = null;
        }

        reloadConfig();

        // Launchpad feature
        if (getConfig().getBoolean("launchpad.enabled", true)) {
            for (var worldName : getConfig().getStringList("launchpad.worlds")) {
                var world = Bukkit.getWorld(worldName);
                if (world != null) {
                    var plate = new LaunchpadModifier(this, world);
                    plate.init();
                    activePlates.add(plate);
                } else {
                    getLogger().warning("World '" + worldName + "' not found.");
                }
            }
        }

        // Scoreboard (Sidebar) feature — schedule update task only
        if (getConfig().getBoolean("scoreboard.enabled", true)) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                for (var player : Bukkit.getOnlinePlayers()) {
                    SidebarModifier.updateSidebarStatic(this, player);
                }
            }, 20L, 20L);  // run every 1 second (20 ticks)
        }

        // TABModifier feature
        if (getConfig().getBoolean("tab-customize.enabled", true)) {
            if (isPlaceholderApiAvailable && isLuckPermsAvailable) {
                tablistUpdater = new TabModifier(this);
                tablistUpdater.start();
            }
        }

        // NameTagModifier feature
        if (getConfig().getBoolean("nametag-customize.enabled", true)) {
            if (isPlaceholderApiAvailable && isLuckPermsAvailable) {
                nameTagUpdater = new NametagModifier(this);
                nameTagUpdater.start();
            }
        }
    }
}
