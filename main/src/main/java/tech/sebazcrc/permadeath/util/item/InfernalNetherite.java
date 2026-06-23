package tech.sebazcrc.permadeath.util.item;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.sebazcrc.permadeath.util.lib.LeatherArmorBuilder;
import tech.sebazcrc.permadeath.util.TextUtils;

public final class InfernalNetherite implements Listener {
    private static Color color = Color.fromRGB(16711680);

    private static String helmetName = TextUtils.format("&5Infernal Netherite Helmet");
    private static String chestName = TextUtils.format("&5Infernal Netherite Chestplate");
    private static String legName = TextUtils.format("&5Infernal Netherite Leggings");
    private static String bootName = TextUtils.format("&5Infernal Netherite Boots");

    public static ItemStack craftNetheriteHelmet() {
        ItemStack item = new LeatherArmorBuilder(Material.LEATHER_HELMET, 1)
                .setColor(color)
                .setDisplayName(helmetName)
                .build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);

            // En 1.21.5 se usa NamespacedKey y EquipmentSlotGroup obligatoriamente para los Atributos
            AttributeModifier modifier = new AttributeModifier(NamespacedKey.fromString("permadeath:infernal_helmet_armor"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD);
            meta.addAttributeModifier(Attribute.ARMOR, modifier);

            AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.fromString("permadeath:infernal_helmet_toughness"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD);
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

            AttributeModifier modifier = new AttributeModifier(NamespacedKey.fromString("permadeath:infernal_chest_armor"), 8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);
            meta.addAttributeModifier(Attribute.ARMOR, modifier);

            AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.fromString("permadeath:infernal_chest_toughness"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);
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

            AttributeModifier modifier = new AttributeModifier(NamespacedKey.fromString("permadeath:infernal_legs_armor"), 6, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS);
            meta.addAttributeModifier(Attribute.ARMOR, modifier);

            AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.fromString("permadeath:infernal_legs_toughness"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS);
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

            AttributeModifier modifier = new AttributeModifier(NamespacedKey.fromString("permadeath:infernal_boots_armor"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET);
            meta.addAttributeModifier(Attribute.ARMOR, modifier);

            AttributeModifier modifier2 = new AttributeModifier(NamespacedKey.fromString("permadeath:infernal_boots_toughness"), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET);
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, modifier2);

            item.setItemMeta(meta);
        }
        return item;
    }
}
ier2 = new AttributeModifier(UUID.randomUUID(), "generic.armorToughness", 3, AttributeModifier.Operation.ADD_NUMBER, slot);
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, modifier2);

        meta.setUnbreakable(true);

        item.setItemMeta(meta);

        return item;
    }
}
