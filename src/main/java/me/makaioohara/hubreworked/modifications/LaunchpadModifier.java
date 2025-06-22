package me.makaioohara.hubreworked.modifications;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LaunchpadModifier implements Listener {

    private final JavaPlugin plugin;
    private final World targetWorld;
    private final Set<String> allPlates = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> pressedPlates = Collections.synchronizedSet(new HashSet<>());
    private boolean allActivated = false;
    private boolean cooldown = false;
    private boolean launchPadActive = true;

    public LaunchpadModifier(JavaPlugin plugin, World world) {
        this.plugin = plugin;
        this.targetWorld = world;

        if (!plugin.getConfig().getBoolean("launchpad.enabled", true)) {
            this.launchPadActive = false;
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void init() {
        if (!launchPadActive) return;
        scanForPlates();
    }

    private void scanForPlates() {
        for (Chunk chunk : targetWorld.getLoadedChunks()) {
            scanChunkForPlates(chunk);
        }
    }

    private void scanChunkForPlates(Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 20; y < 80; y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (isPressurePlate(block.getType())) {
                        allPlates.add(getBlockKey(block.getLocation()));
                    }
                }
            }
        }
    }

    private boolean isPressurePlate(Material material) {
        return material.name().endsWith("PRESSURE_PLATE");
    }

    private String getBlockKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    @EventHandler
    public void onPressurePlate(PlayerInteractEvent event) {
        if (!launchPadActive) return;
        if (event.getAction() != Action.PHYSICAL || event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        if (!isPressurePlate(block.getType()) || block.getWorld() != targetWorld) return;

        String key = getBlockKey(block.getLocation());
        pressedPlates.add(key);

        if (!allActivated && pressedPlates.containsAll(allPlates) && !cooldown) {
            allActivated = true;
            cooldown = true;
            launchAllPlayers();
            startResetCooldown();
        }
    }

    private void launchAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Vector direction = player.getLocation().getDirection().normalize().multiply(2);
            direction.setY(1);
            player.setVelocity(direction);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
            player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 2);
            player.setMetadata("ignoreFallDamage", new FixedMetadataValue(plugin, true));
        }
    }

    private void startResetCooldown() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pressedPlates.clear();
            allActivated = false;
            cooldown = false;
        }, 10 * 20L); // 10 seconds cooldown
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!launchPadActive) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                player.hasMetadata("ignoreFallDamage")) {
            event.setCancelled(true);
            player.removeMetadata("ignoreFallDamage", plugin);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!launchPadActive) return;
        if (event.getWorld() != targetWorld) return;
        scanChunkForPlates(event.getChunk());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!launchPadActive) return;
        Block block = event.getBlockPlaced();
        if (block.getWorld() != targetWorld) return;

        if (isPressurePlate(block.getType())) {
            allPlates.add(getBlockKey(block.getLocation()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!launchPadActive) return;
        Block block = event.getBlock();
        if (!block.getWorld().equals(targetWorld)) return;

        if (isPressurePlate(block.getType())) {
            String key = getBlockKey(block.getLocation());
            allPlates.remove(key);
            pressedPlates.remove(key);
        }
    }
}