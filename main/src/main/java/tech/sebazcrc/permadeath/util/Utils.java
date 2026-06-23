package tech.sebazcrc.permadeath.util;

import org.bukkit.entity.EntityType;

public class Utils {
    public static final String RESOURCE_PACK_LINK = "https://www.dropbox.com/s/h3v77ga72l9vhpg/PermaDeathCore%20RP%20v1.2.zip?dl=1";
    public static final String DISCORD_LINK = "https://discord.gg/w58wzrcJU8";
    public static final int RESOURCE_ID = 112343;
    public static final String SPIGOT_LINK = "https://www.spigotmc.org/resources/permadeath-%E2%98%A0%EF%B8%8F.112343/";

    public static boolean isHostileMob(EntityType type) {
        if (type == null) return false;

        switch (type) {
            // Mobs clásicos y jefes
            case ENDER_DRAGON:
            case WITHER:
            case BLAZE:
            case CREEPER:
            case GHAST:
            case MAGMA_CUBE:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case ZOMBIE:
            case ZOMBIE_VILLAGER:
            case DROWNED:
            case WITHER_SKELETON:
            case WITCH:
            case HUSK:
            case STRAY:
            case PHANTOM:
            
            // Illegers, Raids y derivados
            case PILLAGER:
            case EVOKER:
            case VINDICATOR:
            case RAVAGER:
            case VEX:
            
            // Ocean Monuments y End
            case GUARDIAN:
            case ELDER_GUARDIAN:
            case SHULKER:
            
            // Actualización de Cavernas y Deep Dark (1.17 - 1.19)
            case WARDEN:
            
            // Mobs Hostiles Modernos Añadidos (Hasta la 1.21.5)
            case BREEZE:
            case BOGGED:
            case PIGLIN_BRUTE:
            case HOGLIN:
            case ZOGLIN:
                return true;
                
            default:
                return false;
        }
    }
}
