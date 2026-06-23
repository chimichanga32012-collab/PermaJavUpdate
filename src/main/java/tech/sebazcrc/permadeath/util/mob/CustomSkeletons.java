package tech.sebazcrc.permadeath.util.mob;

import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import tech.sebazcrc.permadeath.Main;

public class CustomSkeletons implements Listener {

    private final Main plugin;

    public CustomSkeletons(Main instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onH(ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Skeleton)) return;

        Skeleton s = (Skeleton) e.getEntity().getShooter();

        if (plugin.getDay() >= 60 && s.getPersistentDataContainer().has(new NamespacedKey(plugin, "demon_skeleton"))) {
            if (e.getHitEntity() != null) {
                Entity h = e.getHitEntity();
                h.getWorld().createExplosion(h.getLocation(), 3f, true, true, s);
            } else if (e.getHitBlock() != null) {
                e.getEntity().getWorld().createExplosion(e.getHitBlock().getLocation(), 3f, true, true, s);
            }
        }
    }

    @EventHandler
    public void onNDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            if (arrow.getShooter() instanceof Skeleton) {
                Skeleton s = (Skeleton) arrow.getShooter();
                if (s.getPersistentDataContainer().has(new NamespacedKey(plugin, "skeleton_definitivo"))) {
                    if (e.getEntity() instanceof LivingEntity) {
                        LivingEntity victim = (LivingEntity) e.getEntity();
                        victim.damage(victim.getHealth());
                    }
                }
            }
        }
        if (e.getDamager() instanceof ShulkerBullet) {
            ShulkerBullet b = (ShulkerBullet) e.getDamager();
            if (b.getShooter() instanceof Shulker && ((Shulker) b.getShooter()).getColor() == DyeColor.RED && e.getEntityType() == EntityType.CAVE_SPIDER) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Shulker && ((Shulker) e.getEntity()).getColor() == DyeColor.RED && e.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
            e.setCancelled(true);
        }
    }
}
nt.DamageCause.MAGIC) {
            e.setCancelled(true);
        }
    }
}
