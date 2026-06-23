package tech.sebazcrc.permadeath.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tech.sebazcrc.permadeath.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BeginningDataManager {

    private final File beginningFile;
    private final FileConfiguration config;
    private final Main instance;

    public BeginningDataManager(Main instance) {
        this.instance = instance;

        this.beginningFile = new File(instance.getDataFolder(), "theBeginning.yml");
        
        if (!beginningFile.exists()) {
            try {
                // Asegurar que la carpeta de datos exista antes de crear el archivo
                if (instance.getDataFolder().exists() || instance.getDataFolder().mkdirs()) {
                    beginningFile.createNewFile();
                }
            } catch (IOException e) {
                instance.getLogger().severe("Ha ocurrido un error al crear el archivo 'theBeginning.yml'");
            }
        }

        this.config = YamlConfiguration.loadConfiguration(beginningFile);

        if (!config.contains("GeneratedOverWorldBeginningPortal")) {
            config.set("GeneratedOverWorldBeginningPortal", false);
        }

        if (!config.contains("GeneratedBeginningPortal")) {
            config.set("GeneratedBeginningPortal", false);
        }

        if (!config.contains("OverWorldPortal")) {
            config.set("OverWorldPortal", "");
        }

        if (!config.contains("BeginningPortal")) {
            config.set("BeginningPortal", "");
        }

        if (!config.contains("KilledED")) {
            config.set("KilledED", false);
        }

        if (!config.contains("PopulatedChests")) {
            config.set("PopulatedChests", new ArrayList<String>());
        }

        saveFile();
        reloadFile();
    }

    public boolean hasPopulatedChest(Location l) {
        if (l == null) return false;
        String s = locationToString(l);
        return config.getStringList("PopulatedChests").contains(s);
    }

    public void addPopulatedChest(Location l) {
        if (l == null) return;
        
        List<String> chests = config.getStringList("PopulatedChests");
        String s = locationToString(l);
        
        if (!chests.contains(s)) {
            chests.add(s);
            config.set("PopulatedChests", chests);
            saveFile();
            reloadFile();
        }
    }

    public boolean generatedOverWorldBeginningPortal() {
        return config.getBoolean("GeneratedOverWorldBeginningPortal");
    }

    public boolean generatedBeginningPortal() {
        return config.getBoolean("GeneratedBeginningPortal");
    }

    public Location getBeginningPortal() {
        if (!generatedBeginningPortal()) {
            return null;
        }
        return buildLocation(config.getString("BeginningPortal"));
    }

    public void setBeginningPortal(Location loc) {
        if (loc == null || generatedBeginningPortal()) {
            return;
        }

        config.set("GeneratedBeginningPortal", true);
        config.set("BeginningPortal", locationToString(loc));

        saveFile();
        reloadFile();
    }

    public Location getOverWorldPortal() {
        if (!generatedOverWorldBeginningPortal()) {
            return null;
        }
        return buildLocation(config.getString("OverWorldPortal"));
    }

    public void setOverWorldPortal(Location loc) {
        if (loc == null || generatedOverWorldBeginningPortal()) {
            return;
        }

        config.set("GeneratedOverWorldBeginningPortal", true);
        config.set("OverWorldPortal", locationToString(loc));

        saveFile();
        reloadFile();
    }

    public boolean killedED() {
        return config.getBoolean("KilledED");
    }

    public void setKilledED() {
        config.set("KilledED", true);

        saveFile();
        reloadFile();
    }

    public static Location buildLocation(String s) {
        if (s == null || s.isEmpty()) return null;
        
        // X;Y;Z;WORLD
        String[] split = s.split(";");
        if (split.length < 4) return null;

        try {
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            double z = Double.parseDouble(split[2]);
            World w = Bukkit.getWorld(split[3]);

            if (w == null) return null;

            return new Location(w, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getName();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveFile() {
        try {
            config.save(beginningFile);
        } catch (IOException e) {
            instance.getLogger().severe("Ha ocurrido un error al guardar el archivo 'theBeginning.yml'");
        }
    }

    public void reloadFile() {
        try {
            config.load(beginningFile);
        } catch (IOException | InvalidConfigurationException e) {
            instance.getLogger().severe("Ha ocurrido un error al cargar/recargar el archivo 'theBeginning.yml'");
        }
    }
}
