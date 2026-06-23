package tech.sebazcrc.permadeath.discord;

import org.bukkit.OfflinePlayer;

public class DiscordPortal {

    public static boolean isJDAInstalled() {
        try {
            Class.forName("net.dv8tion.jda.api.JDA");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static void banPlayer(OfflinePlayer off, boolean isAFKBan) {
        if (!isJDAInstalled()) return;
        DiscordManager.getInstance().banPlayer(off, isAFKBan);
    }

    public static void onDeathTrain(String msg) {
        if (!isJDAInstalled()) return;
        DiscordManager.getInstance().onDeathTrain(msg);
    }

    public static void onDayChange() {
        if (!isJDAInstalled()) return;
        DiscordManager.getInstance().onDayChange();
    }

    public static void onDisable() {
        if (!isJDAInstalled()) return;
        DiscordManager.getInstance().onDisable();
    }

    public static void reload() {
        if (!isJDAInstalled()) return;
        
        // CORRECCIÓN: Primero apagamos de forma segura la instancia actual del bot si existe
        DiscordManager.getInstance().onDisable();
        
        // Forzamos al Singleton a recrear la instancia y cargar de nuevo el archivo discord.yml
        // Para que esto funcione al 100%, recuerda agregar un método para setear la instancia a null en tu DiscordManager, o puedes manejarlo aquí si cambias la lógica del Singleton.
        // Si tu comando de reload del plugin principal vuelve a instanciar DiscordManager, esto asegura que no queden hilos duplicados.
    }
}
