package tech.sebazcrc.permadeath.util.item;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.lib.LeatherArmorBuilder;
import tech.sebazcrc.permadeath.util.TextUtils;

public final class NetheriteArmor implements Listener {
    private static Color color = Color.fromRGB(6116957);

    private static String helmetName = TextUtils.format("&5Netherite Helmet");
    private static String chestName = TextUtils.format("&5Netherite Chestplate");
    private static String legName = TextUtils.format("&5Netherite Leggings");
    private static String bootName = TextUtils.format("&5Netherite Boots");

    public static ItemStack craftNetheriteHelmet() {
        ItemStack item = new LeatherArmorBuilder(Material.LEATHER_HELMET, 1)
                .setColor(color)
                .setDisplayName(helmetName)
                .build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);

            AttributeModifier modifier = new AttributeModifier(NamespacedKey.fromString("permadeath:netherite_helmet_armor"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD);
            meta.addAttributeModifier(Attribute.ARMOR, modifier);

            AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.fromString("permadeath:netherite_helmet_toughness"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD);
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, modifier2);

            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack craftNetheriteChest() {
        ItemStack item = new LeatherArmorBuilder(Material.LEATHER_CHESTPLATE, 1)
                .setColor(color)
                .setDisplayName(chestName)
                .build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);

            AttributeModifier modifier = new AttributeModifier(NamespacedKey.fromString("permadeath:netherite_chest_armor"), 8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);
            meta.addAttributeModifier(Attribute.ARMOR, modifier);

            AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.fromString("permadeath:netherite_chest_toughness"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, modifier2);

            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack craftNetheriteLegs() {
        ItemStack item = new LeatherArmorBuilder(Material.LEATHER_LEGGINGS, 1)
                .setColor(color)
                .setDisplayName(legName)
                .build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);

            AttributeModifier modifier = new AttributeModifier(NamespacedKey.fromString("permadeath:netherite_legs_armor"), 6, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS);
            meta.addAttributeModifier(Attribute.ARMOR, modifier);

            AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.fromString("permadeath:netherite_legs_toughness"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS);
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, modifier2);

            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack craftNetheriteBoots() {
        ItemStack item = new LeatherArmorBuilder(Material.LEATHER_BOOTS, 1)
                .setColor(color)
                .setDisplayName(bootName)
                .build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);

            AttributeModifier modifier = new AttributeModifier(NamespacedKey.fromString("permadeath:netherite_boots_armor"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET);
            meta.addAttributeModifier(Attribute.ARMOR, modifier);

            AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.fromString("permadeath:netherite_boots_toughness"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET);
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, modifier2);

            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isNetheritePiece(ItemStack s) {
        if (s == null) return false;
        if (s.hasItemMeta()) {
            ItemMeta meta = s.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.isUnbreakable() && ChatColor.stripColor(meta.getDisplayName()).startsWith("Netherite")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInfernalPiece(ItemStack s) {
        if (s == null) return false;
        if (s.hasItemMeta()) {
            ItemMeta m = s.getItemMeta();
            if (m != null && m.hasDisplayName()) {
                if (s.getType() == Material.ELYTRA && ChatColor.stripColor(m.getDisplayName()).startsWith("Elytras")) {
                    return true;
                }
                if (m.isUnbreakable() && ChatColor.stripColor(m.getDisplayName()).startsWith("Infernal")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setupHealth(Player p) {
        Double maxHealth = getAvailableMaxHealth(p);
        if (p.getAttribute(Attribute.MAX_HEALTH) != null) {
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        }
    }

    public static Double getAvailableMaxHealth(Player p) {
        int currentNetheritePieces = 0;
        int currentInfernalPieces = 0;
        boolean doPlayerAteOne = p.getPersistentDataContainer().has(new NamespacedKey(Main.getInstance(), "hyper_one"), PersistentDataType.BYTE);
        boolean doPlayerAteTwo = p.getPersistentDataContainer().has(new NamespacedKey(Main.getInstance(), "hyper_two"), PersistentDataType.BYTE);

        for (ItemStack contents : p.getInventory().getArmorContents()) {
            if (isNetheritePiece(contents)) {
                currentNetheritePieces++;
            }
            if (isInfernalPiece(contents)) {
                currentInfernalPieces++;
            }
        }

        Double maxHealth = 20.0D;

        if (doPlayerAteOne) {
            maxHealth += 4.0;
        }
        if (doPlayerAteTwo) {
            maxHealth += 4.0;
        }

        if (currentNetheritePieces >= 4) {
            maxHealth += 8.0D;
        }

        if (currentInfernalPieces >= 4) {
            maxHealth += 10.0D;
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 3, 0));
        }

        if (Main.getInstance().getDay() >= 40) {
            maxHealth -= 8.0D;
            if (Main.getInstance().getDay() >= 60) {
                maxHealth -= 8.0D;

                boolean hasOrb = checkForOrb(p);
                if (!hasOrb) {
                    maxHealth -= 16.0D;
                }
            }
        }

        return Math.max(maxHealth, 0.000001D);
    }

    public static boolean checkForOrb(Player p) {
        if (Main.getInstance().getOrbEvent().isRunning()) {
            return true;
        } else {
            for (ItemStack stack : p.getInventory().getContents()) {
                if (stack != null && stack.getType() == Material.BROWN_DYE) {
                    ItemMeta meta = stack.getItemMeta();
                    if (meta != null && meta.isUnbreakable()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
Meta() != null && stack.getType() == Material.BROWN_DYE && stack.getItemMeta().isUnbreakable()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
