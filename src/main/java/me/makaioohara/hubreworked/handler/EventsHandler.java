package me.makaioohara.hubreworked.handler;

import me.makaioohara.hubreworked.HubReworked;
import me.makaioohara.hubreworked.modifications.SidebarModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class EventsHandler implements Listener {

    private final HubReworked plugin;

    public EventsHandler(HubReworked plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            SidebarModifier.updateSidebarStatic(plugin, player);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            SidebarModifier.updateSidebarStatic(plugin, player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (plugin.getLaunchpadModifier() != null) {
            plugin.getLaunchpadModifier().handlePressurePlate(event.getPlayer(), event.getClickedBlock(), event.getAction());
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getLaunchpadModifier() != null) {
            plugin.getLaunchpadModifier().handleFallDamage(player, event);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (plugin.getLaunchpadModifier() != null) {
            plugin.getLaunchpadModifier().handleChunkLoad(event.getChunk());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getLaunchpadModifier() != null) {
            plugin.getLaunchpadModifier().handleBlockPlace(event.getBlockPlaced());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getLaunchpadModifier() != null) {
            plugin.getLaunchpadModifier().handleBlockBreak(event.getBlock());
        }
    }
}