package tech.sebazcrc.permadeath.event.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.TextUtils;

import java.util.SplittableRandom;

public class SlotBlockListener implements Listener {
    
    private final Main main;
    private final SplittableRandom random = new SplittableRandom();

    public SlotBlockListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType() == Material.STRUCTURE_VOID) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClickVoid(InventoryClickEvent e) {
        if (e.isCancelled()) return;

        ItemStack current = e.getCurrentItem();
        if (current != null && current.getType() == Material.STRUCTURE_VOID) {
            e.setCancelled(true);
            if (e.getClick() == ClickType.NUMBER_KEY) {
                e.getInventory().remove(Material.STRUCTURE_VOID);
            }
            return;
        }

        ItemStack cursor = e.getCursor();
        if (cursor != null && cursor.getType() == Material.STRUCTURE_VOID) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent e) {
        ItemStack result = e.getInventory().getResult();
        if (result != null && (result.getType() == Material.TORCH || result.getType() == Material.REDSTONE_TORCH)) {
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.STRUCTURE_VOID) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (e.isCancelled()) return;

        if ((e.getOffHandItem() != null && e.getOffHandItem().getType() == Material.STRUCTURE_VOID) ||
            (e.getMainHandItem() != null && e.getMainHandItem().getType() == Material.STRUCTURE_VOID)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMoveItem(InventoryMoveItemEvent e) {
        if (e.isCancelled()) return;

        if (e.getItem() != null && e.getItem().getType() == Material.STRUCTURE_VOID) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(InventoryPickupItemEvent e) {
        if (e.isCancelled()) return;

        if (e.getItem().getItemStack().getType() == Material.STRUCTURE_VOID) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWitchThrow(ProjectileLaunchEvent e) {
        if (main.getDay() < 40) return;

        if (e.getEntity().getShooter() instanceof Witch && e.getEntity() instanceof ThrownPotion potion) {
            ItemStack s = new ItemStack(Material.SPLASH_POTION);
            
            if (s.getItemMeta() instanceof PotionMeta meta) {
                meta.clearCustomEffects(); // Elimina de forma directa y optimizada cualquier efecto previo

                // Corrección: random.nextInt(3) otorga 0, 1 o 2. Soluciona el bug del tercer efecto inaccesible.
                switch (random.nextInt(3)) {
                    case 0 -> meta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 20, 3), true);
                    case 1 -> meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 60 * 5 * 20, 2), true);
                    case 2 -> meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 20, 4), true);
                }

                s.setItemMeta(meta);
                potion.setItem(s);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        for (ItemStack s : e.getNewItems().values()) {
            if (s != null && s.getType() == Material.STRUCTURE_VOID) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onIntWithEndRelic(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        
        if (esReliquia(p, p.getInventory().getItemInMainHand()) || esReliquia(p, p.getInventory().getItemInOffHand())) {
            e.setCancelled(true);
        }
    }

    public boolean esReliquia(Player p, ItemStack stack) {
        if (stack == null || !stack.hasItemMeta() || stack.getType() != Material.LIGHT_BLUE_DYE) {
            return false;
        }
        
        String displayName = stack.getItemMeta().getDisplayName();
        return displayName.endsWith(TextUtils.format("&6Reliquia Del Fin"));
    }
}
          ItemStack s = new ItemStack(Material.SPLASH_POTION);
                    PotionMeta meta = (PotionMeta) s.getItemMeta();

                    if (!meta.getCustomEffects().isEmpty() || meta.getCustomEffects().size() >= 1) {
                        for (PotionEffect effect : meta.getCustomEffects()) {
                            meta.removeCustomEffect(effect.getType());
                        }
                    }

                    meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 20, 4), true);
                    s.setItemMeta(meta);
                    potion.setItem(s);
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!e.getNewItems().isEmpty()) {
            for (int i : e.getNewItems().keySet()) {
                ItemStack s = e.getNewItems().get(i);

                if (s != null) {

                    if (s.getType() == Material.STRUCTURE_VOID) {

                        e.getInventory().removeItem(s);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onIntWithEndRelic(PlayerInteractEvent e) {
        if (e.getPlayer().getInventory().getItemInMainHand() != null) {
            if (esReliquia(e.getPlayer(), e.getPlayer().getInventory().getItemInMainHand())) {
                e.setCancelled(true);
            }
        }

        if (e.getPlayer().getInventory().getItemInOffHand() != null) {
            if (esReliquia(e.getPlayer(), e.getPlayer().getInventory().getItemInOffHand())) {
                e.setCancelled(true);
            }
        }
    }

    public boolean esReliquia(Player p, ItemStack stack) {
        if (stack == null) return false;
        if (!stack.hasItemMeta()) return false;

        if (stack.getType() == Material.LIGHT_BLUE_DYE && stack.getItemMeta().getDisplayName().endsWith(TextUtils.format("&6Reliquia Del Fin"))) {
            return true;
        }
        return false;
    }
}
