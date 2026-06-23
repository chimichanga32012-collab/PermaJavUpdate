package tech.sebazcrc.permadeath.util.lib;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherArmorBuilder extends ItemBuilder {
    private LeatherArmorMeta lm;

    public LeatherArmorBuilder(ItemStack itemStack) {
        super(itemStack);
    }

    public LeatherArmorBuilder(Material material, int amount) {
        super(material, amount);
    }

    public LeatherArmorBuilder setColor(Color color) {
        ItemMeta meta = this.is.getItemMeta();
        if (meta instanceof LeatherArmorMeta) {
            this.lm = (LeatherArmorMeta) meta;
            this.lm.setColor(color);
            this.is.setItemMeta(this.lm);
        }
        return this;
    }
}
