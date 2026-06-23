package tech.sebazcrc.permadeath.event.world;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import tech.sebazcrc.permadeath.Main;

public class WorldEvents implements Listener {

    @EventHandler
    public void onWeatherStorm(WeatherChangeEvent event) {
        Main plugin = Main.getInstance();

        if (!event.toWeatherState()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                String msg = Main.prefix + plugin.getMessages().getMessage("StormEnd", p);
                p.sendMessage(msg);
            }
            plugin.getMessages().sendConsole(plugin.getMessages().getMsgForConsole("StormEnd"));
            
            if (plugin.getDay() >= 50) {
                if (plugin.getBeginningManager() != null) {
                    plugin.getBeginningManager().setClosed(false);
                }
                
                for (World w : Bukkit.getWorlds()) {
                    w.setGameRule(GameRule.NATURAL_REGENERATION, true);
                }
            }
            return;
        }

        World world = event.getWorld();
        if (world.getEnvironment() == World.Environment.NORMAL && plugin.getDay() >= 25) {
            for (LivingEntity entity : world.getLivingEntities()) {
                plugin.deathTrainEffects(entity);
            }
        }
    }
}
             }
                }
            }
        }
    }
}
