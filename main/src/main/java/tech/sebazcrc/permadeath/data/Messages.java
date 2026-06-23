package tech.sebazcrc.permadeath.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import tech.sebazcrc.permadeath.Language;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.lib.FileAPI;
import tech.sebazcrc.permadeath.util.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Messages {
    private final Main instance;
    private final Map<Language, FileConfiguration> loadedConfigs = new HashMap<>();

    public Messages(Main instance) {
        this.instance = instance;

        saveDataES();
        saveDataEN();
        reloadFiles();
    }

    public void saveDataES() {
        new FileAPI.FileOut(instance, "mensajes_ES", "mensajes/", false);

        File f = new File(instance.getDataFolder(), "mensajes/mensajes_ES.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(f);

        FileAPI.UtilFile c = FileAPI.select(instance, f, config);

        c.set("Server-Messages.coords-msg-enable", true);
        c.set("Server-Messages.OnJoin", "&e%player% se ha unido al servidor.");
        c.set("Server-Messages.OnLeave", "&e%player% ha abandonado el servidor.");
        c.set("Server-Messages.StormEnd", "&cLa tormenta ha llegado a su fin.");
        c.set("Server-Messages.Sleep", "&7%player% &efue a dormir.");
        c.set("Server-Messages.Sleeping", "&7%player% &eestá durmiendo &e(&b%players%&7/&b%needed%&e)");
        c.set("Server-Messages.DeathMessageTitle", "&c¡Permadeath!");
        c.set("Server-Messages.DeathMessageSubtitle", "%player% ha muerto");
        c.set("Server-Messages.DeathMessageChat", "&c&lEste es el comienzo del sufrimiento eterno de &4&l%player%&c&l. ¡HA SIDO PERMABANEADO!");
        c.set("Server-Messages.DeathTrainMessage", "&c¡Comienza el Death Train con duración de %tiempo% horas!");
        c.set("Server-Messages.DeathTrainMessageMinutes", "&c¡Comienza el Death Train con duración de %tiempo% minutos!");
        c.set("Server-Messages.ActionBarMessage", "&7Quedan %tiempo% de tormenta");

        c.save();
    }

    public void saveDataEN() {
        new FileAPI.FileOut(instance, "mensajes_EN", "mensajes/", false);

        File f = new File(instance.getDataFolder(), "mensajes/mensajes_EN.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(f);

        FileAPI.UtilFile c = FileAPI.select(instance, f, config);

        c.set("Server-Messages.OnJoin", "&e%player% joined the game.");
        c.set("Server-Messages.OnLeave", "&e%player% left the game.");
        c.set("Server-Messages.StormEnd", "&cThe storm has been ended.");
        c.set("Server-Messages.Sleep", "&7%player% &ewent to sleep, Sweet dreams!.");
        c.set("Server-Messages.Sleeping", "&7%player% &eis sleeping &e(&b%players%&7/&b%needed%&e)");
        c.set("Server-Messages.DeathMessageTitle", "&cPermadeath!");
        c.set("Server-Messages.DeathMessageSubtitle", "%player% died");
        c.set("Server-Messages.DeathMessageChat", "&c&lThis is the beginning of the eternal suffering of &4&l%player%&c&l. HAS BEEN PERMA-BANNED!");
        c.set("Server-Messages.DeathTrainMessage", "&cStarting the Death Train with a duration of %tiempo% hours!");
        c.set("Server-Messages.DeathTrainMessageMinutes", "&cStarting the Death Train with a duration of %tiempo% minutes!");
        c.set("Server-Messages.ActionBarMessage", "&7%tiempo% storm left");

        c.save();
    }

    public void reloadFiles() {
        loadedConfigs.put(Language.SPANISH, YamlConfiguration.loadConfiguration(new File(instance.getDataFolder(), "mensajes/mensajes_ES.yml")));
        loadedConfigs.put(Language.ENGLISH, YamlConfiguration.loadConfiguration(new File(instance.getDataFolder(), "mensajes/mensajes_EN.yml")));
    }

    public String getMessageByPlayer(String path, String playerName, List<String> replaces) {
        Map<String, String> r = new HashMap<>();

        if (replaces != null) {
            for (String s : replaces) {
                if (s == null || !s.contains(";;;")) continue;
                String[] l = s.split(";;;");
                if (l.length >= 2) {
                    r.put(l[0], l[1]);
                }
            }
        }

        PlayerDataManager data = new PlayerDataManager(playerName, instance);
        Language lang = data.getLanguage();

        FileConfiguration c = loadedConfigs.getOrDefault(lang, loadedConfigs.get(Language.SPANISH));
        if (c == null) return "";

        String returning = c.getString(path, "Message path not found: " + path);

        for (Map.Entry<String, String> entry : r.entrySet()) {
            returning = returning.replace(entry.getKey(), entry.getValue());
        }

        return TextUtils.format(returning);
    }

    public String getMessageForConsole(String path) {
        FileConfiguration c = loadedConfigs.get(Language.SPANISH);
        if (c == null) return "";

        String returning = c.getString(path, "Message path not found: " + path);
        return TextUtils.format(returning);
    }

    public String getMessageByPlayer(String path, String playerName) {
        PlayerDataManager data = new PlayerDataManager(playerName, instance);
        Language lang = data.getLanguage();

        FileConfiguration c = loadedConfigs.getOrDefault(lang, loadedConfigs.get(Language.SPANISH));
        if (c == null) return "";

        String returning = c.getString(path, "Message path not found: " + path);
        return TextUtils.format(returning);
    }

    public String getMessage(String path, Player player) {
        return getMessageByPlayer("Server-Messages." + path, player.getName());
    }

    public String getMessage(String path, Player player, List<String> l) {
        return getMessageByPlayer("Server-Messages." + path, player.getName(), l);
    }

    public String getMsgForConsole(String path) {
        return getMessageForConsole("Server-Messages." + path);
    }

    public void sendConsole(String mensaje) {
        Bukkit.getConsoleSender().sendMessage(mensaje);
    }
}
urn new File(instance.getDataFolder(), "mensajes/mensajes_ES.yml");
        } else if (lang == Language.ENGLISH) {

            return new File(instance.getDataFolder(), "mensajes/mensajes_EN.yml");
        }

        return null;
    }


    public String getMessage(String path, Player player) {

        return instance.getMessages().getMessageByPlayer("Server-Messages." + path, player.getName());
    }

    public String getMessage(String path, Player player, List l) {

        return instance.getMessages().getMessageByPlayer("Server-Messages." + path, player.getName(), l);
    }

    public String getMsgForConsole(String path) {

        return instance.getMessages().getMessageForConsole("Server-Messages." + path);
    }

    public void sendConsole(String mensaje) {

        Bukkit.getConsoleSender().sendMessage(mensaje);
    }
}
