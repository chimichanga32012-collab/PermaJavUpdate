package tech.sebazcrc.permadeath.util;

import lombok.Getter;
import org.bukkit.Bukkit;

public class VersionManager {
    @Getter
    private static String version;
    @Getter
    private static MinecraftVersion minecraftVersion;

    static {
        try {
            String bukkitVer = Bukkit.getBukkitVersion();
            
            if (bukkitVer.contains("1.15")) {
                version = "1_15_R1";
                minecraftVersion = MinecraftVersion.v1_15_R1;
            } else if (bukkitVer.contains("1.16")) {
                version = "1_16_R3";
                minecraftVersion = MinecraftVersion.v1_16_R3;
            } else if (bukkitVer.contains("1.20")) {
                version = "1_20_R1";
                minecraftVersion = MinecraftVersion.v1_20_R1;
            } else {
                version = "1_21_R1";
                minecraftVersion = MinecraftVersion.v1_21_R1;
            }
        } catch (Exception e) {
            version = "1_21_R1";
            minecraftVersion = MinecraftVersion.v1_21_R1;
        }
    }

    public static String getRev() {
        return getVersion();
    }

    public static boolean isValidVersionSet() {
        return minecraftVersion != null;
    }

    public static String getFormattedVersion() {
        return minecraftVersion != null ? minecraftVersion.getFormattedName() : "1.21";
    }

    public static boolean isRunningPostNetherUpdate() {
        return true;
    }
}
