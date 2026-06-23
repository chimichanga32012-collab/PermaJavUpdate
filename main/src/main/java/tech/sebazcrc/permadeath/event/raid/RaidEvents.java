package tech.sebazcrc.permadeath.event.raid;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tech.sebazcrc.permadeath.Main;

public class RaidEvents implements Listener {

    @EventHandler
    public void onRaidFinish(RaidFinishEvent e) {
        if (Main.getInstance().getDay() < 50) return;
        if (e.getWinners().isEmpty()) return;

        // Uso de Lambda para un código más limpio
        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            for (Player player : e.getWinners()) {
                PotionEffect effect = player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
                
                if (effect != null) {
                    player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);

                    // Corrección: 5 minutos en ticks (5 min * 60 seg * 20 ticks)
                    int durationInTicks = 5 * 60 * 20; 
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, durationInTicks, effect.getAmplifier()));
                }
            }
        }, 10L);
    }
}
