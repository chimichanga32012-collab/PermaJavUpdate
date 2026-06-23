package tech.sebazcrc.permadeath.event.paper;

import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent;
import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.EndGateway;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.TextUtils;
import tech.sebazcrc.permadeath.end.demon.DemonPhase;

import java.util.ArrayList;
import java.util.SplittableRandom;

public class PaperListeners implements Listener {

    private final Main main;
    private final SplittableRandom random = new SplittableRandom();

    public PaperListeners(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onProjectileHit(EnderDragonFireballHitEvent e) {
        AreaEffectCloud a = e.getAreaEffectCloud();
        if (main.getTask() != null) {

            ArrayList<Block> toChange = new ArrayList<>();
            Block b = main.endWorld.getHighestBlockAt(a.getLocation());
            Location highest = b.getLocation();

            int structure = random.nextInt(5); // Ajustado a 5 ya que antes evaluaba hasta structure == 4
            switch (structure) {
                case 0 -> {
                    toChange.add(b.getRelative(BlockFace.NORTH));
                    toChange.add(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST));
                    toChange.add(b.getRelative(BlockFace.SOUTH));
                    toChange.add(b.getRelative(BlockFace.SOUTH_EAST));
                    toChange.add(b.getRelative(BlockFace.SOUTH_WEST));
                    toChange.add(b.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.SOUTH));
                    toChange.add(b.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.NORTH));
                    toChange.add(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH));
                }
                case 1 -> {
                    toChange.add(b.getRelative(BlockFace.NORTH));
                    toChange.add(b.getRelative(BlockFace.NORTH_EAST));
                    toChange.add(b);
                }
                case 2 -> {
                    toChange.add(b.getRelative(BlockFace.SOUTH));
                    toChange.add(b.getRelative(BlockFace.SOUTH_WEST));
                    toChange.add(b);
                }
                case 3 -> {
                    toChange.add(b.getRelative(BlockFace.NORTH));
                    toChange.add(b.getRelative(BlockFace.NORTH_EAST));
                    toChange.add(b);
                    toChange.add(b.getRelative(BlockFace.SOUTH));
                    toChange.add(b.getRelative(BlockFace.EAST));
                }
                case 4 -> {
                    toChange.add(b.getRelative(BlockFace.SOUTH));
                    toChange.add(b.getRelative(BlockFace.NORTH_WEST));
                    toChange.add(b);
                    toChange.add(b.getRelative(BlockFace.NORTH));
                    toChange.add(b.getRelative(BlockFace.WEST));
                }
            }

            if (main.getTask().getCurrentDemonPhase() == DemonPhase.NORMAL) {
                if (highest.getY() > 0) {
                    for (Block all : toChange) {
                        Location used = main.endWorld.getHighestBlockAt(new Location(main.endWorld, all.getX(), all.getY(), all.getZ())).getLocation();
                        Block now = main.endWorld.getBlockAt(used);
                        if (now.getType() != Material.AIR) {
                            now.setType(Material.BEDROCK);
                        }
                    }
                }
            } else {
                if (random.nextBoolean()) {
                    a.setParticle(Particle.SMOKE);
                    a.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 20, 1), false);
                } else {
                    if (highest.getY() > 0) {
                        for (Block all : toChange) {
                            Location used = main.endWorld.getHighestBlockAt(new Location(main.endWorld, all.getX(), all.getY(), all.getZ())).getLocation();
                            Block now = main.endWorld.getBlockAt(used);
                            if (now.getType() != Material.AIR) {
                                now.setType(Material.BEDROCK);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGatewayTeleport(EntityTeleportEndGatewayEvent e) {
        int day = main.getDay();
        if (day < 40) return;

        if (day >= 50) {
            if (main.getBeginningManager().isClosed()) {
                e.setCancelled(true);
                return;
            }

            Entity entity = e.getEntity();
            if (entity instanceof Player) return;
            
            e.setCancelled(true);
            Location from = e.getFrom();
            World world = from.getWorld();
            if (world == null) return;

            final Vector direction = entity.getLocation().getDirection();
            final Vector velocity = entity.getVelocity();
            float pitch = entity.getLocation().getPitch();
            float yaw = entity.getLocation().getYaw();

            if (world.getName().equalsIgnoreCase(main.world.getName())) {
                Location loc = main.getBeData().getBeginningPortal();
                loc.setDirection(direction);
                loc.setPitch(pitch);
                loc.setYaw(yaw);
                entity.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                entity.setVelocity(velocity);
            }

            if (world.getName().equalsIgnoreCase("pdc_the_beginning")) {
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    Location loc = main.world.getSpawnLocation();
                    loc.setDirection(direction);
                    loc.setPitch(pitch);
                    loc.setYaw(yaw);
                    entity.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    entity.setVelocity(velocity);
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onGatewayTeleport(PlayerTeleportEndGatewayEvent e) {
        int day = main.getDay();
        if (day < 40) return;

        Player p = e.getPlayer();
        World playerWorld = p.getWorld();

        if (day < 50) {
            if (playerWorld.getName().equalsIgnoreCase(main.world.getName()) || playerWorld.getName().equalsIgnoreCase(main.getBeginningManager().getBeginningWorld().getName())) {
                p.setNoDamageTicks(p.getMaximumNoDamageTicks());
                p.damage(p.getHealth() + 1.0D, null);
                p.setNoDamageTicks(0);
                Bukkit.broadcastMessage(TextUtils.format("&c&lEl jugador &4&l" + p.getName() + " &c&lentró a TheBeginning antes de tiempo."));
            }
            return;
        }

        if (day >= 50) {
            if (main.getBeginningManager().isClosed()) {
                e.setCancelled(true);
                return;
            }

            EndGateway gateway = e.getGateway();
            Location from = e.getFrom();
            World world = from.getWorld();
            if (world == null) return;

            gateway.setExitLocation(gateway.getLocation());
            gateway.update();
            e.setCancelled(true);

            final Vector direction = p.getLocation().getDirection();
            final Vector velocity = p.getVelocity();

            if (world.getName().equalsIgnoreCase(main.world.getName())) {
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    Location loc = main.getBeData().getBeginningPortal();
                    loc.setDirection(direction);
                    p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    p.setVelocity(velocity);
                }, 1L);
            }

            if (world.getName().equalsIgnoreCase("pdc_the_beginning")) {
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    Location loc = main.world.getSpawnLocation();
                    loc.setDirection(direction);
                    p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    p.setVelocity(velocity);
                }, 1L);
            }
        }
    }
}
Cause.PLUGIN);
                        p.setVelocity(velocity);
                    }
                }, 1L);
            }
        }
    }
}
