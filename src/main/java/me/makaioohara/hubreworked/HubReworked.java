package me.makaioohara.hubreworked;

import me.makaioohara.hubreworked.handler.EventsHandler;
import me.makaioohara.hubreworked.commands.ReloaderCommand;
import me.makaioohara.hubreworked.commands.SidebarCommand;
import me.makaioohara.hubreworked.modifications.LaunchpadModifier;
import me.makaioohara.hubreworked.modifications.NametagModifier;
import me.makaioohara.hubreworked.modifications.SidebarModifier;
import me.makaioohara.hubreworked.modifications.TabModifier;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class HubReworked extends JavaPlugin {

    private final List<LaunchpadModifier> activePlates = new ArrayList<>();
    private LaunchpadModifier launchpadModifier;
    public LaunchpadModifier getLaunchpadModifier() {
        return launchpadModifier;
    }

    private TabModifier tablistUpdater;

    private NametagModifier nameTagUpdater;

    private int sidebarTaskId = -1;

    public static boolean isPlaceholderApiAvailable;
    public static boolean isLuckPermsAvailable;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new EventsHandler(this), this);

        if (getCommand("hwreload") != null) {
            getCommand("hwreload").setExecutor(new ReloaderCommand(this));
        }
        if (getCommand("sidebar") != null) {
            getCommand("sidebar").setExecutor(new SidebarCommand(this));
        }

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

        if (sidebarTaskId != -1) {
            Bukkit.getScheduler().cancelTask(sidebarTaskId);
            sidebarTaskId = -1;
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

        if (sidebarTaskId != -1) {
            Bukkit.getScheduler().cancelTask(sidebarTaskId);
            sidebarTaskId = -1;
        }

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
                var players = new ArrayList<>(Bukkit.getOnlinePlayers());
                for (var player : players) {
                    SidebarModifier.updateSidebarStatic(this, player);
                }
            }, 1200L, 1200L).getTaskId();
        }

        // TABModifier feature
        if (getConfig().getBoolean("tab-customize.enabled", true)) {
            if (isPlaceholderApiAvailable && isLuckPermsAvailable) {
                tablistUpdater = new TabModifier(this);
                tablistUpdater.start();
            } else {
                getLogger().warning("Tab customization is enabled but PlaceholderAPI or LuckPerms is missing.");
            }
        }

        // NameTagModifier feature
        if (getConfig().getBoolean("nametag-customize.enabled", true)) {
            if (isPlaceholderApiAvailable && isLuckPermsAvailable) {
                nameTagUpdater = new NametagModifier(this);
                nameTagUpdater.start();
            } else {
                getLogger().warning("Nametag customization is enabled but PlaceholderAPI or LuckPerms is missing.");
            }
        }
    }
}