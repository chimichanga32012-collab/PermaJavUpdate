package tech.sebazcrc.permadeath.data;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.Language;
import tech.sebazcrc.permadeath.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class PlayerDataManager {

    private final String name;
    private String banDay;
    private String banTime;
    private String banCause;
    private String coords;

    private final File playersFile;
    private final FileConfiguration config;
    private final Main instance;

    public PlayerDataManager(String playerName, Main instance) {
        this.name = playerName;
        this.instance = instance;

        this.playersFile = new File(instance.getDataFolder(), "jugadores.yml");

        if (!playersFile.exists()) {
            try {
                if (instance.getDataFolder().exists() || instance.getDataFolder().mkdirs()) {
                    playersFile.createNewFile();
                }
            } catch (IOException e) {
                instance.getLogger().severe("Ha ocurrido un error al crear el archivo 'jugadores.yml'");
            }
        }

        this.config = YamlConfiguration.loadConfiguration(playersFile);

        if (config.contains("Players." + playerName)) {
            this.banDay = config.getString("Players." + playerName + ".banDay");
            this.banTime = config.getString("Players." + playerName + ".banTime");
            this.banCause = config.getString("Players." + playerName + ".banCause");
            this.coords = config.getString("Players." + playerName + ".coords");
        } else {
            this.banTime = "";
            this.banDay = "";
            this.banCause = "";
            this.coords = "";
        }

        if (Bukkit.getPlayer(playerName) != null) {
            addDefault("Players." + getName() + ".UUID", Bukkit.getPlayer(playerName).getUniqueId().toString());
        }

        if (!config.contains("Players." + getName() + ".HP")) {
            config.set("Players." + getName() + ".HP", 0);
        }

        saveFile();
        reloadFile();
    }

    private void addDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        } else {
            if (path.equalsIgnoreCase("Players." + getName() + ".Idioma")) {
                String idioma = config.getString("Players." + getName() + ".Idioma");

                if (idioma == null || (!idioma.equalsIgnoreCase("SPANISH") && !idioma.equalsIgnoreCase("ENGLISH"))) {
                    config.set("Players." + getName() + ".Idioma", "SPANISH");
                    saveFile();
                    reloadFile();
                }
            }
        }
    }

    public Language getLanguage() {
        addDefault("Players." + getName() + ".Idioma", "SPANISH");
        String langStr = config.getString("Players." + getName() + ".Idioma", "SPANISH");
        try {
            return Language.valueOf(langStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Language.SPANISH;
        }
    }

    public void setLanguage(Language language) {
        config.set("Players." + name + ".Idioma", language.toString());
        saveFile();
        reloadFile();
    }

    public void generateDayData() {
        long days = instance.getDay();
        if (config.contains("Players." + name + ".LastDay")) return;

        setLastDay(days);
    }

    public void setLastDay(long days) {
        config.set("Players." + name + ".LastDay", days);
        saveFile();
        reloadFile();
    }

    public long getLastDay() {
        generateDayData();
        return config.getLong("Players." + name + ".LastDay");
    }

    public ItemStack craftHead() {
        ItemStack s = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) s.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(name));
            meta.setDisplayName(TextUtils.format("&c&l" + name));
            meta.setLore(Arrays.asList(
                    TextUtils.format("&c&lHA SIDO PERMABANEADO"),
                    TextUtils.format(" "),
                    TextUtils.format("&7Fecha del Baneo: &c" + banDay),
                    TextUtils.format("&7Hora del Baneo: &c" + banTime),
                    TextUtils.format("&7Causa del Baneo: " + banCause)
            ));
            s.setItemMeta(meta);
        }

        return s;
    }

    public ItemStack craftHead(ItemStack s) {
        SkullMeta meta = (SkullMeta) s.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(name));
            meta.setDisplayName(TextUtils.format("&c&l" + name));
            meta.setLore(Arrays.asList(
                    TextUtils.format("&c&lHA SIDO PERMABANEADO"),
                    TextUtils.format(" "),
                    TextUtils.format("&7Fecha del Baneo: &c" + banDay),
                    TextUtils.format("&7Hora del Baneo: &c" + banTime),
                    TextUtils.format("&7Causa de Muerte: " + banCause)
            ));
            s.setItemMeta(meta);
        }

        return s;
    }

    public void setExtraHP(int hp) {
        config.set("Players." + getName() + ".HP", hp);
        saveFile();
        reloadFile();
    }

    public int getExtraHP() {
        return config.getInt("Players." + getName() + ".HP");
    }

    public void setDeathDay() {
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        setBanDay(formattedDate);
    }

    public void setDeathTime() {
        String formattedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        setBanTime(formattedTime);
    }

    public void setAutoDeathCause(EntityDamageEvent.DamageCause lastDamage) {
        String s;

        if (lastDamage == EntityDamageEvent.DamageCause.WITHER) {
            s = "&0Efecto Wither";
        } else if (lastDamage == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || lastDamage == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            s = "Explosión";
        } else if (lastDamage == EntityDamageEvent.DamageCause.DRAGON_BREATH) {
            s = "&dEnder Dragon (Breath)";
        } else if (lastDamage == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            s = "Mobs";
        } else if (lastDamage == EntityDamageEvent.DamageCause.DROWNING) {
            s = "Ahogamiento";
        } else if (lastDamage == EntityDamageEvent.DamageCause.FALL) {
            s = "Caída";
        } else if (lastDamage == EntityDamageEvent.DamageCause.FIRE || lastDamage == EntityDamageEvent.DamageCause.FIRE_TICK) {
            s = "Fuego";
        } else if (lastDamage == EntityDamageEvent.DamageCause.HOT_FLOOR || lastDamage == EntityDamageEvent.DamageCause.LAVA) {
            s = "Lava";
        } else if (lastDamage == EntityDamageEvent.DamageCause.LIGHTNING) {
            s = "Trueno";
        } else if (lastDamage == EntityDamageEvent.DamageCause.POISON) {
            s = "Veneno";
        } else if (lastDamage == EntityDamageEvent.DamageCause.VOID) {
            s = "Vacío";
        } else if (lastDamage == EntityDamageEvent.DamageCause.SUFFOCATION) {
            s = "Sofocado";
        } else if (lastDamage == EntityDamageEvent.DamageCause.SUICIDE) {
            s = "Suicidio";
        } else if (lastDamage == EntityDamageEvent.DamageCause.THORNS) {
            s = "Espinas";
        } else if (lastDamage == EntityDamageEvent.DamageCause.PROJECTILE) {
            s = "Proyectil";
        } else {
            s = "Causa desconocida.";
        }

        setBanCause(s);
    }

    public String getName() {
        return name;
    }

    public String getBanDay() {
        return config.getString("Players." + name + ".banDay", "");
    }

    public void setBanDay(String banDay) {
        this.banDay = banDay;
        config.set("Players." + getName() + ".banDay", banDay);
        saveFile();
        reloadFile();
    }

    public void setDeathCoords(Location where) {
        if (where == null) return;
        
        int x = where.getBlockX();
        int y = where.getBlockY();
        int z = where.getBlockZ();

        String s = x + " " + y + " " + z;
        this.coords = s;

        config.set("Players." + getName() + ".coords", s);
        saveFile();
        reloadFile();
    }

    public String getBanTime() {
        return config.getString("Players." + name + ".banTime", "");
    }

    public void setBanTime(String banTime) {
        this.banTime = banTime;
        config.set("Players." + getName() + ".banTime", banTime);
        saveFile();
        reloadFile();
    }

    public String getBanCause() {
        return config.getString("Players." + name + ".banCause", "");
    }

    public void setBanCause(String banCause) {
        this.banCause = banCause;
        config.set("Players." + getName() + ".banCause", banCause);
        saveFile();
        reloadFile();
    }

    public void saveFile() {
        try {
            config.save(playersFile);
        } catch (IOException e) {
            instance.getLogger().severe("Ha ocurrido un error al guardar el archivo 'jugadores.yml'");
        }
    }

    public void reloadFile() {
        try {
            config.load(playersFile);
        } catch (IOException | InvalidConfigurationException e) {
            instance.getLogger().severe("Ha ocurrido un error al cargar/recargar el archivo 'jugadores.yml'");
        }
    }
}
xception e) {
            System.out.println("[ERROR] Ha ocurrido un error al guardar el archivo 'players.yml'");
        } catch (InvalidConfigurationException e) {
            System.out.println("[ERROR] Ha ocurrido un error al guardar el archivo 'players.yml'");
        }
    }
}
