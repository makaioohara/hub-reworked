package me.makaioohara.hubreworked;

import me.makaioohara.hubreworked.commands.ReloaderCommand;
import me.makaioohara.hubreworked.commands.SidebarCommand;
import me.makaioohara.hubreworked.handler.EventsHandler;
import me.makaioohara.hubreworked.modifications.*;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public final class HubReworked extends JavaPlugin {

    private Scoreboard sharedScoreboard;
    public Scoreboard getSharedScoreboard() {
        if (sharedScoreboard == null) {
            sharedScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        return sharedScoreboard;
    }

    private final List<LaunchpadModifier> activePlates = new ArrayList<>();
    private LaunchpadModifier launchpadModifier;
    public LaunchpadModifier getLaunchpadModifier() {
        return launchpadModifier;
    }

    private TabModifier tablistUpdater;
    private NametagModifier nameTagUpdater;

    private int sidebarTaskId = -1;
    private int nametagTaskId = -1;

    public static boolean isPlaceholderApiAvailable;
    public static boolean isLuckPermsAvailable;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new EventsHandler(this), this);

        registerCommands();
        detectExternalPlugins();

        sharedScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        reloadFeatures();

        getLogger().info("Hub Reworked has been enabled.");
    }

    @Override
    public void onDisable() {
        stopAllFeatures();
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("Hub Reworked has been disabled.");
    }

    public void reloadFeatures() {
        stopAllFeatures();
        reloadConfig();

        // Launchpad feature
        if (getConfig().getBoolean("launchpad.enabled", true)) {
            launchpadModifier = new LaunchpadModifier(this);
            launchpadModifier.init();
            activePlates.add(launchpadModifier);
        }

        // Sidebar feature
        if (getConfig().getBoolean("scoreboard.enabled", true)) {
            sidebarTaskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
                for (var player : Bukkit.getOnlinePlayers()) {
                    SidebarModifier.updateSidebarStatic(this, player);
                }
            }, 1200L, 1200L).getTaskId();
        }

        // Tablist customization
        if (getConfig().getBoolean("tab-customize.enabled", true)) {
            if (isPlaceholderApiAvailable && isLuckPermsAvailable) {
                tablistUpdater = new TabModifier(this);
                tablistUpdater.start();
            } else {
                getLogger().warning("Tab customization is enabled but PlaceholderAPI or LuckPerms is missing.");
            }
        }

        // Nametag customization
        if (getConfig().getBoolean("nametag-customize.enabled", true)) {
            if (isPlaceholderApiAvailable && isLuckPermsAvailable) {
                nameTagUpdater = new NametagModifier(this);
                nameTagUpdater.start();

                nametagTaskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
                    nameTagUpdater.refresh();
                }, 1200L, 1200L).getTaskId();
            } else {
                getLogger().warning("Nametag customization is enabled but PlaceholderAPI or LuckPerms is missing.");
            }
        }
    }

    private void stopAllFeatures() {
        activePlates.clear();

        if (tablistUpdater != null) {
            tablistUpdater.stop();
            tablistUpdater = null;
        }

        if (nameTagUpdater != null) {
            nameTagUpdater.stop();
            nameTagUpdater = null;
        }

        cancelTask(sidebarTaskId);
        sidebarTaskId = -1;

        cancelTask(nametagTaskId);
        nametagTaskId = -1;
    }

    private void cancelTask(int taskId) {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private void registerCommands() {
        if (getCommand("hwreload") != null) {
            getCommand("hwreload").setExecutor(new ReloaderCommand(this));
        }
        if (getCommand("sidebar") != null) {
            getCommand("sidebar").setExecutor(new SidebarCommand(this));
        }
    }

    private void detectExternalPlugins() {
        isPlaceholderApiAvailable = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null &&
                Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        isLuckPermsAvailable = Bukkit.getPluginManager().getPlugin("LuckPerms") != null &&
                Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
    }
}
