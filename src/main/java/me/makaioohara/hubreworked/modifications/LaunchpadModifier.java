package me.makaioohara.hubreworked.modifications;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class LaunchpadModifier {
    private final JavaPlugin plugin;
    private final Set<String> allowedWorlds = new HashSet<>();
    private final Set<BlockPos> allPlates = new HashSet<>();
    private final Set<BlockPos> pressedPlates = new HashSet<>();
    private boolean allActivated = false;
    private boolean cooldown = false;
    private boolean launchPadActive;

    public LaunchpadModifier(JavaPlugin plugin) {
        this.plugin = plugin;
        this.launchPadActive = plugin.getConfig().getBoolean("launchpad.enabled", true);
        if (!launchPadActive) return;
        this.allowedWorlds.addAll(plugin.getConfig().getStringList("launchpad.worlds"));
    }

    public void init() {
        if (!launchPadActive) return;
        allPlates.clear();
        pressedPlates.clear();
        scanForPlates();
    }

    private void scanForPlates() {
        for (World world : Bukkit.getWorlds()) {
            if (!allowedWorlds.contains(world.getName())) continue;
            for (Chunk chunk : world.getLoadedChunks()) {
                scanChunkForPlates(chunk);
            }
        }
    }

    private void scanChunkForPlates(Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 20; y < 80; y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (isPressurePlate(block.getType())) {
                        allPlates.add(new BlockPos(block.getX(), block.getY(), block.getZ(), block.getWorld().getName()));
                    }
                }
            }
        }
    }

    private boolean isPressurePlate(Material material) {
        return material.name().endsWith("PRESSURE_PLATE");
    }

    public void handlePressurePlate(Player player, Block block, Action action) {
        if (!launchPadActive || action != Action.PHYSICAL || block == null) return;

        String worldName = block.getWorld().getName();
        if (!allowedWorlds.contains(worldName)) return;
        if (!isPressurePlate(block.getType())) return;

        BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ(), worldName);
        pressedPlates.add(pos);

        if (!allActivated && pressedPlates.size() == allPlates.size()) {
            allActivated = true;
            cooldown = true;
            launchAllPlayers();
            startResetCooldown();
        }
    }

    private void launchAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!allowedWorlds.contains(player.getWorld().getName())) continue;

            Vector direction = player.getLocation().getDirection().normalize().multiply(2);
            direction.setY(1);
            player.setVelocity(direction);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
            player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 10, 0.2, 0.2, 0.2, 0);

            player.setMetadata("ignoreFallDamage", new FixedMetadataValue(plugin, System.currentTimeMillis()));
        }
    }

    private void startResetCooldown() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pressedPlates.clear();
            allActivated = false;
            cooldown = false;
        }, 3 * 20L);
    }

    public void handleFallDamage(Player player, EntityDamageEvent event) {
        if (!launchPadActive) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                player.hasMetadata("ignoreFallDamage")) {
            event.setCancelled(true);
            player.removeMetadata("ignoreFallDamage", plugin);
        }
    }

    public void handleChunkLoad(Chunk chunk) {
        if (!launchPadActive || !allowedWorlds.contains(chunk.getWorld().getName())) return;
        scanChunkForPlates(chunk);
    }

    public void handleBlockPlace(Block block) {
        if (!launchPadActive || !allowedWorlds.contains(block.getWorld().getName())) return;
        if (isPressurePlate(block.getType())) {
            allPlates.add(new BlockPos(block.getX(), block.getY(), block.getZ(), block.getWorld().getName()));
        }
    }

    public void handleBlockBreak(Block block) {
        if (!launchPadActive || !allowedWorlds.contains(block.getWorld().getName())) return;
        if (isPressurePlate(block.getType())) {
            BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
            allPlates.remove(pos);
            pressedPlates.remove(pos);
        }
    }

    private static class BlockPos {
        final int x, y, z;
        final String world;

        BlockPos(int x, int y, int z, String world) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlockPos other)) return false;
            return x == other.x && y == other.y && z == other.z && Objects.equals(world, other.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, world);
        }
    }
}
