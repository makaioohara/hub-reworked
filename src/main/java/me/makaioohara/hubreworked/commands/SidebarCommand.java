package me.makaioohara.hubreworked.commands;

import me.makaioohara.hubreworked.HubReworked;
import me.makaioohara.hubreworked.modifications.SidebarModifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SidebarCommand implements CommandExecutor {

    private final HubReworked plugin;

    public SidebarCommand(HubReworked plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        SidebarModifier.toggleSidebar(player);

        if (SidebarModifier.isSidebarEnabled(player)) {
            SidebarModifier.updateSidebarStatic(plugin, player);
            player.sendMessage(ChatColor.GRAY + "Sidebar enabled.");
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            player.sendMessage(ChatColor.GRAY + "Sidebar disabled.");
        }

        return true;
    }
}
