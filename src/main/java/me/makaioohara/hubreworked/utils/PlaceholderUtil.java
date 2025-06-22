package me.makaioohara.hubreworked.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaceholderUtil {

    public static String applyBuiltInPlaceholders(Player player, String text) {
        Location loc = player.getLocation();

        return text
                .replace("%player%", player.getName())
                .replace("%world%", loc.getWorld().getName())
                .replace("%gamemode%", player.getGameMode().name())
                .replace("%time%", getTime())
                .replace("%location%", formatLocation(loc))
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()))
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
    }

    private static String formatLocation(Location loc) {
        return String.format("%d, %d, %d",
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ());
    }

    private static String getTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}
