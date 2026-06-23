package tech.sebazcrc.permadeath.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import tech.sebazcrc.permadeath.Main;

public class MobFactory {
    private final Main instance;

    public MobFactory(Main instance) {
        this.instance = instance;
    }

    public Creeper spawnEnderCreeper(Location l, @Nullable Creeper c) {
        if (l == null || l.getWorld() == null) return null;
        if (c == null) {
            c = l.getWorld().spawn(l, Creeper.class);
        }

        // Se actualizó la creación del efecto de poción para cumplir con la API moderna (partículas invisibles y ambiente)
        c.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));

        c.setCustomName(format("&6Ender Creeper"));
        c.setCustomNameVisible(true);
        addPersistentData(c, "ender_creeper");

        return c;
    }

    public Creeper spawnQuantumCreeper(Location l, @Nullable Creeper c) {
        if (l == null || l.getWorld() == null) return null;
        if (c == null) {
            c = l.getWorld().spawn(l, Creeper.class);
        }
        c.setCustomName(format("&6Quantum Creeper"));
        c.setCustomNameVisible(true);
        addPersistentData(c, "quantum_creeper");
        c.setExplosionRadius(instance.getConfig().getInt("Toggles.Quantum-Explosion-Power", 5));
        return c;
    }

    public Creeper spawnEnderQuantumCreeper(Location l, @Nullable Creeper c) {
        if (l == null || l.getWorld() == null) return null;
        if (c == null) {
            c = l.getWorld().spawn(l, Creeper.class);
        }
        c.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        c.setCustomName(format("&6Ender Quantum Creeper"));
        c.setCustomNameVisible(true);
        addPersistentData(c, "ender_quantum_creeper");
        c.setExplosionRadius(instance.getConfig().getInt("Toggles.Quantum-Explosion-Power", 5));

        return c;
    }

    public boolean hasData(Entity entity, String id) {
        if (entity == null) return false;
        // Se actualizó el método .has() para adaptarse a la API de persistencia actual de Bukkit
        return entity.getPersistentDataContainer().has(new NamespacedKey(instance, id));
    }

    public void addPersistentData(Entity entity, String id) {
        if (entity == null) return;
        entity.getPersistentDataContainer().set(new NamespacedKey(instance, id), PersistentDataType.BYTE, (byte) 1);
    }

    public String format(String s) {
        return TextUtils.format(s);
    }
}
