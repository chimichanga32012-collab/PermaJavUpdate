package tech.sebazcrc.permadeath.event.player;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.TextUtils;

public class AnvilListener implements Listener {
    private final Main plugin;

    private final String helmetName;
    private final String chestName;
    private final String legName;
    private final String bootName;
    private final Color infernalColor = Color.fromRGB(16711680);

    public AnvilListener(Main instance) {
        this.plugin = instance;

        this.helmetName = TextUtils.format("&5Netherite Helmet");
        this.chestName = TextUtils.format("&5Netherite Chestplate");
        this.legName = TextUtils.format("&5Netherite Leggings");
        this.bootName = TextUtils.format("&5Netherite Boots");
    }

    @EventHandler
    public void onAnvil(InventoryClickEvent e) {
        ItemStack currentItem = e.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) return;

        ItemMeta meta = currentItem.getItemMeta();
        if (meta == null) return;

        if (e.getInventory().getType() == InventoryType.ANVIL && e.getSlotType() == InventoryType.SlotType.RESULT && meta.hasDisplayName()) {

            Material type = currentItem.getType();
            if (type.name().startsWith("DIAMOND_") && meta.isUnbreakable()) {
                String name = switch (type) {
                    case DIAMOND_SWORD -> "Espada de Netherite";
                    case DIAMOND_PICKAXE -> "Pico de Netherite";
                    case DIAMOND_AXE -> "Hacha de Netherite";
                    case DIAMOND_HOE -> "Azada de Netherite";
                    case DIAMOND_SHOVEL -> "Pala de Netherite";
                    default -> "";
                };

                if (!name.isEmpty()) {
                    meta.setDisplayName(TextUtils.format("&6" + name));
                    currentItem.setItemMeta(meta);
                }
            }

            if (meta instanceof LeatherArmorMeta leatherMeta) {
                formatLeatherArmor(currentItem, leatherMeta);
            }
        } else {
            if (meta instanceof LeatherArmorMeta leatherMeta && meta.isUnbreakable()) {
                formatLeatherArmor(currentItem, leatherMeta);
            }
        }
    }

    private void formatLeatherArmor(ItemStack item, LeatherArmorMeta meta) {
        String name = switch (item.getType()) {
            case LEATHER_HELMET -> meta.isUnbreakable() ? helmetName : "";
            case LEATHER_CHESTPLATE -> meta.isUnbreakable() ? chestName : "";
            case LEATHER_LEGGINGS -> meta.isUnbreakable() ? legName : "";
            case LEATHER_BOOTS -> meta.isUnbreakable() ? bootName : "";
            default -> "";
        };

        if (!name.isEmpty()) {
            if (meta.getColor().equals(infernalColor)) {
                name = TextUtils.format("&5Infernal " + ChatColor.stripColor(name));
            }
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
    }
}
       } else if (meta.isUnbreakable() && type == Material.LEATHER_LEGGINGS) {
                    name = legName;
                }

                if (meta.getColor().equals(Color.fromRGB(16711680)) || meta.getColor() == Color.fromRGB(16711680)) {
                    if (!name.isEmpty()) {
                        name = TextUtils.format("&5Infernal " + ChatColor.stripColor(name));
                    }
                }

                if (!name.isEmpty()) {
                    meta.setDisplayName(name);
                    e.getCurrentItem().setItemMeta(meta);
                }
            }
        } else {

            if (e.getCurrentItem().getType() == Material.LEATHER_HELMET || e.getCurrentItem().getType() == Material.LEATHER_CHESTPLATE || e.getCurrentItem().getType() == Material.LEATHER_LEGGINGS || e.getCurrentItem().getType() == Material.LEATHER_BOOTS) {

                if (e.getCurrentItem().getItemMeta().isUnbreakable()) {

                    LeatherArmorMeta meta = (LeatherArmorMeta) e.getCurrentItem().getItemMeta();
                    ItemStack item = e.getCurrentItem();
                    String name = "";
                    Material type = item.getType();

                    if (meta.isUnbreakable() && type == Material.LEATHER_BOOTS) {
                        name = bootName;
                    } else if (meta.isUnbreakable() && type == Material.LEATHER_HELMET) {
                        name = helmetName;
                    } else if (meta.isUnbreakable() && type == Material.LEATHER_CHESTPLATE) {
                        name = chestName;
                    } else if (meta.isUnbreakable() && type == Material.LEATHER_LEGGINGS) {
                        name = legName;
                    }

                    if (meta.getColor().equals(Color.fromRGB(16711680)) || meta.getColor() == Color.fromRGB(16711680)) {
                        if (!name.isEmpty()) {
                            name = TextUtils.format("&5Infernal " + ChatColor.stripColor(name));
                        }
                    }

                    if (!name.isEmpty()) {
                        meta.setDisplayName(name);
                        e.getCurrentItem().setItemMeta(meta);
                    }
                }
            }
        }
    }
}
