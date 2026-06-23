package tech.sebazcrc.permadeath.event.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.item.PermadeathItems;
import tech.sebazcrc.permadeath.data.EndDataManager;
import tech.sebazcrc.permadeath.util.TextUtils;

import java.util.Arrays;
import java.util.List;

public class BlockListener implements Listener {
    
    public static final List<ItemStack> NO_DAMAGE_TOOLS = Arrays.asList(
            PermadeathItems.craftNetheriteAxe(), 
            PermadeathItems.craftNetheriteShovel(), 
            PermadeathItems.craftNetheriteSword(), 
            PermadeathItems.craftNetheritePickaxe(), 
            PermadeathItems.craftNetheriteHoe()
    );

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBurn(BlockBurnEvent e) {
        Main plugin = Main.getInstance();
        if (plugin.getEndData() != null && plugin.getDay() >= 30) {
            EndDataManager ma = plugin.getEndData();
            if (ma.getConfig().contains("RegenZoneLocation")) {
                Location loc = buildLocation(ma.getConfig().getString("RegenZoneLocation"));
                if (loc != null && e.getBlock().getWorld().getName().equalsIgnoreCase(loc.getWorld().getName())) {
                    if (e.getBlock().getLocation().distance(loc) <= 10) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockExplode(EntityExplodeEvent e) {
        Main plugin = Main.getInstance();
        if (plugin.getEndData() != null) {
            EndDataManager ma = plugin.getEndData();
            if (ma.getConfig().contains("RegenZoneLocation")) {
                Location loc = buildLocation(ma.getConfig().getString("RegenZoneLocation"));
                if (loc != null) {
                    // En lugar de cancelar toda la explosión, removemos solo los bloques protegidos
                    e.blockList().removeIf(b -> b.getWorld().getName().equalsIgnoreCase(loc.getWorld().getName()) 
                            && b.getLocation().distance(loc) <= 10);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockCombust(BlockIgniteEvent e) {
        Main plugin = Main.getInstance();
        if (plugin.getEndData() != null && plugin.getDay() >= 30) {
            EndDataManager ma = plugin.getEndData();
            if (ma.getConfig().contains("RegenZoneLocation")) {
                Location loc = buildLocation(ma.getConfig().getString("RegenZoneLocation"));
                if (loc != null && e.getBlock().getWorld().getName().equalsIgnoreCase(loc.getWorld().getName())) {
                    if (e.getBlock().getLocation().distance(loc) <= 3) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent e) {
        Main plugin = Main.getInstance();
        if (plugin.getEndData() != null && plugin.getDay() >= 30) {
            EndDataManager ma = plugin.getEndData();
            if (ma.getConfig().contains("RegenZoneLocation")) {
                Location loc = buildLocation(ma.getConfig().getString("RegenZoneLocation"));
                if (loc != null && e.getBlock().getWorld().getName().equalsIgnoreCase(loc.getWorld().getName())) {
                    if (e.getBlock().getLocation().distance(loc) <= 3) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(TextUtils.format("&cNo puedes colocar bloques cerca de la Zona de Regeneración."));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Main plugin = Main.getInstance();
        if (plugin.getEndData() != null && plugin.getDay() >= 30) {
            EndDataManager ma = plugin.getEndData();
            if (ma.getConfig().contains("RegenZoneLocation")) {
                Location loc = buildLocation(ma.getConfig().getString("RegenZoneLocation"));
                if (loc != null && e.getBlock().getWorld().getName().equalsIgnoreCase(loc.getWorld().getName())) {
                    if (e.getBlock().getLocation().distance(loc) <= 4) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(TextUtils.format("&cNo puedes romper bloques cerca de la Zona de Regeneración."));
                        return;
                    }
                }
            }
        }

        // Daño por picar bloques sin las herramientas Permadeath
        if (plugin.getDay() >= 50) {
            boolean damage = true;
            ItemStack i = e.getPlayer().getInventory().getItemInMainHand();

            for (ItemStack s : NO_DAMAGE_TOOLS) {
                if (i.getType() == s.getType() && i.hasItemMeta() && s.hasItemMeta()) {
                    if (i.getItemMeta().isUnbreakable() && s.getItemMeta().isUnbreakable()) {
                        damage = false;
                        break;
                    }
                }
            }

            if (damage) {
                if (plugin.getDay() < 60) {
                    e.getPlayer().damage(1.0D);
                } else {
                    e.getPlayer().damage(16.0D);
                }
            }
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (Main.getInstance().getDay() >= 50) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFurnace(FurnaceSmeltEvent e) {
        if (Main.getInstance().getDay() >= 50) {
            ItemStack resu = e.getResult();
            if (resu != null) {
                if (resu.getType() == Material.IRON_INGOT) {
                    resu.setType(Material.IRON_NUGGET);
                    e.setResult(resu);
                } else if (resu.getType() == Material.GOLD_INGOT) {
                    resu.setType(Material.GOLD_NUGGET);
                    e.setResult(resu);
                }
            }
        }
    }

    private Location buildLocation(String s) {
        if (s == null || s.isEmpty()) return null;
        
        // X;Y;Z;WORLD
        String[] split = s.split(";");
        if (split.length < 4) return null;

        try {
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            double z = Double.parseDouble(split[2]);
            World w = Bukkit.getWorld(split[3]);

            if (w == null) return null;

            return new Location(w, x, y, z);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
