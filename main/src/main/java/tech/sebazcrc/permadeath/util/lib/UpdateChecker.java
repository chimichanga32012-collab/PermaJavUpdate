package tech.sebazcrc.permadeath.util.lib;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import tech.sebazcrc.permadeath.util.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer; // Se cambió por el Consumer nativo de Java

public class UpdateChecker {
    private final Plugin plugin;
    private boolean hasInternetConnection = true;

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            // Se actualizó la creación de la URL usando URI para cumplir con Java 21+
            try (InputStream inputStream = URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + Utils.RESOURCE_ID).toURL().openStream(); 
                 Scanner scanner = new Scanner(inputStream)) {
                 
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                this.hasInternetConnection = false;
            }
        });
    }

    public boolean hasInternetConnection() {
        return hasInternetConnection;
    }
}
