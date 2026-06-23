package tech.sebazcrc.permadeath.world.beginning;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.data.BeginningDataManager;
import tech.sebazcrc.permadeath.util.TextUtils;
import tech.sebazcrc.permadeath.world.WorldEditPortal;
import tech.sebazcrc.permadeath.world.beginning.generator.BeginningGenerator;
import tech.sebazcrc.permadeath.world.beginning.generator.BeginningLootTable;

public class BeginningManager implements Listener {

    private final Main main;
    private World beginningWorld;
    private final BeginningDataManager data;
    
    @Getter
    private boolean closed = false;

    public BeginningManager(Main main) {
        this.main = main;
        this.beginningWorld = null;
        this.data = main.getBeData();

        if (main.getDay() >= 40) {
            generateWorld();
            main.getServer().getPluginManager().registerEvents(this, main);
        }
    }

    public World getBeginningWorld() {
        return beginningWorld;
    }

    private void generateWorld() {
        this.beginningWorld = Bukkit.getWorld("pdc_the_beginning");
        
        if (this.beginningWorld == null) {
            WorldCreator creator = new WorldCreator("pdc_the_beginning");
            creator.environment(World.Environment.THE_END);
            creator.generator(new BeginningGenerator());
            creator.generateStructures(false);
            this.beginningWorld = creator.createWorld();
            
            if (this.beginningWorld != null) {
                if (main.getConfig().getBoolean("Toggles.Doble-Mob-Cap")) {
                    this.beginningWorld.setMonsterSpawnLimit(140);
                }
                this.beginningWorld.setGameRule(GameRule.MOB_GRIEFING, false);
            }
        }
    }

    public void closeBeginning() {
        if (beginningWorld == null) return;
        beginningWorld.getPlayers().forEach(p -> {
            p.teleport(main.world.getSpawnLocation());
            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.0F);
        });
        Bukkit.broadcastMessage(TextUtils.format(Main.prefix + "&eThe Beginning ha cerrado temporalmente (DeathTrain)."));
        this.closed = true;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        if (main.isRunningPaperSpigot()) {
            return;
        }
        Player p = e.getPlayer();
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.END_GATEWAY) return;
        
        if (p.getWorld().getName().equalsIgnoreCase(main.world.getName())) {
            e.setCanCreatePortal(false);
        }

        if (beginningWorld != null && p.getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
            if (p.getLocation().getBlock().getState() instanceof EndGateway) {
                EndGateway gateway = (EndGateway) p.getLocation().getBlock().getState();
                gateway.setExitLocation(null);
                gateway.update();
            }
            e.setCanCreatePortal(false);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();

        if (e.getCause() != PlayerTeleportEvent.TeleportCause.END_GATEWAY) return;

        if (isClosed()) {
            e.setCancelled(true);
            return;
        }

        if (beginningWorld == null) return;

        if (main.getDay() < 50) {
            if (p.getWorld().getName().equalsIgnoreCase(main.world.getName()) || p.getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
                p.setNoDamageTicks(p.getMaximumNoDamageTicks());
                p.damage(p.getHealth() + 1.0D);
                p.setNoDamageTicks(0);
                Bukkit.broadcastMessage(TextUtils.format("&c&lEl jugador &4&l" + p.getName() + " &c&lentró a TheBeginning antes de tiempo."));
            }
            return;
        }

        if (p.getWorld().getName().equalsIgnoreCase(main.world.getName())) {
            p.sendMessage(TextUtils.format("&eBienvenido a The Beginning."));
            p.teleport(beginningWorld.getSpawnLocation());
            e.setCancelled(true);

            Bukkit.getScheduler().runTaskLater(main, () -> p.teleport(beginningWorld.getSpawnLocation()), 20L);
            return;
        }

        int x = p.getLocation().getBlockX();
        int z = p.getLocation().getBlockZ();
        if (p.getWorld().getName().equalsIgnoreCase(beginningWorld.getName()) && x != 200 && z != 200) {
            if (p.getLocation().getBlock().getState() instanceof EndGateway) {
                EndGateway gateway = (EndGateway) p.getLocation().getBlock().getState();
                gateway.setExitLocation(null);
                gateway.update();
            }

            p.teleport(main.world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (beginningWorld != null && e.getPlayer().getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
            if (e.getBlock().getState() instanceof Chest) {
                Chest chest = (Chest) e.getBlock().getState();
                populateChest(chest);
            }
        }
        if (e.getBlock().getType() == Material.SPAWNER && main.getNetheriteBlock() != null) {
            if (main.getDay() < 60) {
                main.getNetheriteBlock().onBlockBreak(e);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getClickedBlock() != null && beginningWorld != null && p.getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
            if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getClickedBlock().getState() instanceof Chest) {
                    Chest chest = (Chest) e.getClickedBlock().getState();
                    populateChest(chest);
                }
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && main.getNetheriteBlock() != null) {
            if (e.getClickedBlock().getType() == Material.CHEST) return;
            ItemStack ih = p.getInventory().getItemInMainHand();
            if (ih.getType() == Material.DIAMOND && ih.hasItemMeta() && ih.getItemMeta().isUnbreakable()) {
                main.getNetheriteBlock().placeCustomBlock(main.getNetheriteBlock().blockFaceToLocation(e.getClickedBlock(), e.getBlockFace()));
                if (ih.getAmount() > 1) {
                    ih.setAmount(ih.getAmount() - 1);
                } else {
                    ih = null;
                }
                ItemStack finalIh = ih;
                Bukkit.getScheduler().runTask(main, () -> {
                    p.getInventory().setItemInMainHand(finalIh);
                    p.updateInventory();
                });
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCreatePortal(PortalCreateEvent e) {
        if (beginningWorld != null && e.getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
            for (BlockState s : e.getBlocks()) {
                Block b = s.getBlock();
                if (b.getType() == Material.END_GATEWAY || b.getType() == Material.BEDROCK || s instanceof EndGateway) {
                    if (b.getChunk().getX() == 0 && b.getChunk().getZ() == 0) {
                        e.getBlocks().remove(s);
                        s.setType(Material.AIR);
                    }
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent e) {
        if (beginningWorld == null) return;
        if (e.getPlayer().getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWater(BlockDispenseEvent e) {
        if (beginningWorld == null) return;
        if (e.getItem().getType() == Material.BUCKET || e.getItem().getType() == Material.WATER_BUCKET) {
            if (e.getBlock().getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (beginningWorld == null) return;
        if (e.isCancelled() || e.getSpawner() == null) return;
        CreatureSpawner spawner = e.getSpawner();
        if (e.getEntity().getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
            if (e.getEntityType() != EntityType.ARMOR_STAND) {
                spawner.setSpawnedType(e.getEntityType());
                spawner.update();
            }
            if (e.getEntityType() == EntityType.WITHER) {
                Wither w = (Wither) e.getEntity();
                w.setRemoveWhenFarAway(true);
            }
            if (e.getEntityType() == EntityType.GHAST) {
                Ghast g = (Ghast) main.getNmsHandler().spawnCustomGhast(e.getLocation().add(0, 5, 0), CreatureSpawnEvent.SpawnReason.CUSTOM, true);
                if (g != null) {
                    g.setCustomName(TextUtils.format("&6Ender Ghast Definitivo"));
                    main.getNmsAccessor().setMaxHealth(g, 150.0D, true);
                }
                e.setCancelled(true);
            }
            if (e.getEntityType() == EntityType.CREEPER) {
                Creeper c = (Creeper) e.getEntity();
                c.setCustomName(TextUtils.format("&6Quantum Creeper"));
                c.setPowered(true);
                c.getPersistentDataContainer().set(new NamespacedKey(main, "quantum_creeper"), PersistentDataType.BYTE, (byte) 1);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (beginningWorld == null) return;

        if (e.getPlayer().getWorld().getName().equalsIgnoreCase(beginningWorld.getName())) {
            if (e.getBlock().getState() instanceof Chest) {
                data.addPopulatedChest(e.getBlock().getLocation());
            }
        }
    }

    private void populateChest(Chest chest) {
        if (data.getConfig().contains("PopulatedChests")) {
            if (data.hasPopulatedChest(chest.getLocation())) return;
            if (main.getDay() < 60) {
                new BeginningLootTable(this).populateChest(chest);
            }
            data.addPopulatedChest(chest.getLocation());
        }
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void generatePortal(boolean overworld, Location location) {
        WorldEditPortal.generatePortal(overworld, location);
    }
}
 if (data.getConfig().contains("PopulatedChests")) {
            if (data.hasPopulatedChest(chest.getLocation())) return;
            if (main.getDay() < 60) {
                new BeginningLootTable(this).populateChest(chest);
            }
            data.addPopulatedChest(chest.getLocation());
        }
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void generatePortal(boolean overworld, Location location) {
        WorldEditPortal.generatePortal(overworld, location);
    }
}
