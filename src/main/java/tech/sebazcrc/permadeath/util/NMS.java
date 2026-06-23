package tech.sebazcrc.permadeath.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import tech.sebazcrc.permadeath.util.interfaces.DeathModule;
import tech.sebazcrc.permadeath.util.interfaces.InfernalNetheriteBlock;
import tech.sebazcrc.permadeath.util.interfaces.NMSAccessor;
import tech.sebazcrc.permadeath.util.interfaces.NMSHandler;

import java.lang.reflect.InvocationTargetException;

public class NMS {
    @Getter
    private static NMSAccessor accessor;
    @Getter
    private static NMSHandler handler;
    @Getter
    private static InfernalNetheriteBlock netheriteBlock;

    private static Class<?> deathModuleClass;
    
    static {
        try {
            deathModuleClass = Class.forName(search("entity.DeathModuleImpl"));
        } catch (Exception e) {
            Bukkit.getLogger().warning("[Permadeath] No se pudo mapear de forma estática DeathModuleImpl. Se intentará en la inicialización.");
        }
    }

    public static void loadNMSAccessor() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String path = search("NMSAccessorImpl");
        accessor = (NMSAccessor) Class.forName(path).getConstructor().newInstance();
    }

    public static void loadNMSHandler() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String path = search("NMSHandlerImpl");
        handler = (NMSHandler) Class.forName(path).getConstructor().newInstance();
    }

    public static void loadInfernalNetheriteBlock() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String path = search("block.InfernalNetheriteBlockImpl");
        netheriteBlock = (InfernalNetheriteBlock) Class.forName(path).getConstructor().newInstance();
    }

    public static String search(String classPath) {
        // En lugar de depender ciegamente de una revisión dinámica obsoleta de Bukkit (_R1),
        // usamos un puente controlado a través de VersionManager o un valor fijo para la 1.21.
        String rev = VersionManager.getRev();
        if (rev == null || rev.isEmpty() || rev.equalsIgnoreCase("UNKNOWN")) {
            rev = "1_21_R1"; // Fallback por defecto para la arquitectura de la versión actual
        }
        return search(rev, classPath);
    }

    public static String search(String rev, String classPath) {
        return String.format("tech.sebazcrc.permadeath.nms.v%s.%s", rev, classPath);
    }

    public static void spawnDeathModule(Location location) {
        if (location == null) return;
        try {
            if (deathModuleClass == null) {
                deathModuleClass = Class.forName(search("entity.DeathModuleImpl"));
            }
            DeathModule module = (DeathModule) deathModuleClass.getConstructor().newInstance();
            module.spawn(location);
        } catch (Exception x) {
            Bukkit.getLogger().severe("[Permadeath] Error crítico al intentar spawnear el DeathModule mediante NMS: " + x.getMessage());
        }
    }
}
