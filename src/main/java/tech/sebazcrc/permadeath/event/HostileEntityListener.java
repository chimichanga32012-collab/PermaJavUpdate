package tech.sebazcrc.permadeath.event;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.Utils;

public class HostileEntityListener implements Listener {
    private final Main instance;

    public HostileEntityListener(Main instance) {
        this.instance = instance;
        initialize();
    }

    public void initialize() {
        if (instance.getDay() >= 20) {
            for (World w : Bukkit.getWorlds()) {
                for (LivingEntity entity : w.getLivingEntities()) {
                    EntityType type = entity.getType();

                    if (!Utils.isHostileMob(type) && type != EntityType.ENDERMAN) {
                        injectHostileBehavior(entity);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled()) return;

        EntityType type = e.getEntityType();
        if (instance.getDay() >= 20 && !Utils.isHostileMob(type) && type != EntityType.ARMOR_STAND && type != EntityType.ENDERMAN) {
            injectHostileBehavior(e.getEntity());
        }
    }

    private void injectHostileBehavior(LivingEntity entity) {
        instance.getNmsAccessor().injectHostilePathfinders(entity);
        if (entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) == null) {
            instance.getNmsAccessor().registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 8.0D, entity);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (instance.getDay() < 20 || e.isNewChunk()) return;

        for (Entity entity : e.getChunk().getEntities()) {
            if (!entity.isValid() || entity.isDead()) continue;
            if (!(entity instanceof LivingEntity livingEntity) || entity instanceof Player) continue;

            if (livingEntity instanceof Villager && instance.getDay() >= 60) {
                entity.getWorld().spawn(entity.getLocation(), Vindicator.class);
                entity.remove();
                continue; 
            }

            if (!Utils.isHostileMob(livingEntity.getType()) && livingEntity.getType() != EntityType.ENDERMAN) {
                injectHostileBehavior(livingEntity);
            }
        }
    }
}
