package me.makaioohara.hubreworked.commands;

import me.makaioohara.hubreworked.HubReworked;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;

public class ReloaderCommand implements CommandExecutor, TabExecutor {

    private final HubReworked plugin;

    public ReloaderCommand(HubReworked plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubreworked.reload")) {
            return true;
        }

        plugin.reloadConfig();
        plugin.reloadFeatures();

        sender.sendMessage(ChatColor.GRAY + "Hub Reworked has been reloaded.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}