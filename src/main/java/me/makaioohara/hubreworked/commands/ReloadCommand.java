package me.makaioohara.hubreworked;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabExecutor {

    private final HubReworked plugin;

    public ReloadCommand(HubReworked plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubreworked.reload")) {
            sender.sendMessage("§cYou don't have permission to reload this plugin.");
            return true;
        }

        plugin.reloadConfig();
        plugin.getLogger().info("Config reloaded by " + sender.getName());

        // Re-init features based on updated config
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.onEnable());

        sender.sendMessage("§aHubReworked config reloaded successfully.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
