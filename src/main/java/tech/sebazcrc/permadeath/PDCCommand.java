package tech.sebazcrc.permadeath;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tech.sebazcrc.permadeath.util.NMS;
import tech.sebazcrc.permadeath.util.item.InfernalNetherite;
import tech.sebazcrc.permadeath.util.item.NetheriteArmor;
import tech.sebazcrc.permadeath.util.item.PermadeathItems;
import tech.sebazcrc.permadeath.util.lib.ItemBuilder;
import tech.sebazcrc.permadeath.data.DateManager;
import tech.sebazcrc.permadeath.data.PlayerDataManager;
import tech.sebazcrc.permadeath.util.TextUtils;
import tech.sebazcrc.permadeath.discord.DiscordPortal;
import tech.sebazcrc.permadeath.util.VersionManager;
import tech.sebazcrc.permadeath.world.beginning.generator.EmptyGenerator;

import java.time.LocalTime;
import java.util.Random;
import java.util.stream.Collectors;

public class PDCCommand implements CommandExecutor {

    private final Main instance;

    public PDCCommand(Main instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("pdc")) {
            return false;
        }

        World world = instance.world;

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "awake":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(TextUtils.format("&cNecesitas usar este comando en el juego."));
                    return true;
                }
                Player pAwake = (Player) sender;
                int timeAwake = pAwake.getStatistic(Statistic.TIME_SINCE_REST) / 20;

                long days = timeAwake / 86400;
                long hours = (timeAwake % 86400) / 3600;
                long minutes = (timeAwake % 3600) / 60;
                long seconds = timeAwake % 60;

                String awakeStr = (days >= 1 ? days + " días " : "") + String.format("%02d:%02d:%02d", hours, minutes, seconds);
                sender.sendMessage(instance.prefix + ChatColor.RED + "Tiempo despierto: " + ChatColor.GRAY + awakeStr);
                break;

            case "duracion":
                if (world.hasStorm()) {
                    int stormTicks = world.getWeatherDuration();
                    int totalSeconds = stormTicks / 20;

                    long stormDays = totalSeconds / 86400;
                    long remainingSeconds = totalSeconds % 86400;

                    LocalTime timeOfDay = LocalTime.ofSecondOfDay(remainingSeconds);
                    String timeStr = timeOfDay.toString();

                    if (stormDays > 0) {
                        sender.sendMessage(instance.prefix + ChatColor.RED + "Quedan " + ChatColor.GRAY + stormDays + "d " + timeStr);
                    } else {
                        sender.sendMessage(instance.prefix + ChatColor.RED + "Quedan " + ChatColor.GRAY + timeStr);
                    }
                } else {
                    sender.sendMessage(instance.prefix + ChatColor.RED + "¡No hay ninguna tormenta en marcha!");
                }
                break;

            case "idioma":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(TextUtils.format("&cNecesitas usar este comando en el juego."));
                    return true;
                }
                Player pLang = (Player) sender;

                if (args.length == 1) {
                    sender.sendMessage(TextUtils.format("&ePor favor ingresa un idioma."));
                    sender.sendMessage(TextUtils.format("&7Ejemplo: &b/pdc idioma es"));
                    sender.sendMessage(TextUtils.format("&eArgumentos válidos: &b<es, en>"));
                    return true;
                }

                String lang = args[1].toLowerCase();
                PlayerDataManager dataLang = new PlayerDataManager(pLang.getName(), instance);

                if (lang.equals("es")) {
                    if (dataLang.getLanguage() == Language.SPANISH) {
                        sender.sendMessage(TextUtils.format("&c¡Ya estás usando el idioma español!"));
                        return true;
                    }
                    dataLang.setLanguage(Language.SPANISH);
                    sender.sendMessage(TextUtils.format("&eHas cambiado tu idioma a: &bEspañol"));
                } else if (lang.equals("en")) {
                    if (dataLang.getLanguage() == Language.ENGLISH) {
                        sender.sendMessage(TextUtils.format("&cYour language is already set to english"));
                        return true;
                    }
                    dataLang.setLanguage(Language.ENGLISH);
                    sender.sendMessage(TextUtils.format("&eYour language has been set to: &bEnglish"));
                } else {
                    sender.sendMessage(TextUtils.format("&cNo has ingresado un idioma válido."));
                }
                break;

            case "cambiardia":
                if (!sender.hasPermission("permadeathcore.cambiardia")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permiso para hacer esto"));
                    return true;
                }
                if (Main.SPEED_RUN_MODE) {
                    sender.sendMessage(TextUtils.format("&cNo puedes hacer esto porque el modo SpeedRun está activo."));
                    return true;
                }
                if (args.length <= 1) {
                    sender.sendMessage(TextUtils.format("&cNecesitas agregar un día"));
                    sender.sendMessage(TextUtils.format("&eEjemplo: &7/pdc cambiarDia <día>"));
                    return true;
                }
                DateManager.getInstance().setDay(sender, args[1]);
                break;

            case "reload":
                if (!sender.hasPermission("permadeathcore.reload")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permiso para utilizar este comando."));
                    return true;
                }
                instance.reload(sender);
                break;

            case "debug":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(TextUtils.format("&cNecesitas usar este comando en el juego."));
                    return true;
                }
                Player pDebug = (Player) sender;

                if (!pDebug.hasPermission("permadeathcore.admin")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permisos para utilizar este comando (&f&npermadeathcore.admin&c)."));
                    return true;
                }

                if (args.length == 1) {
                    sender.sendMessage(TextUtils.format(instance.prefix + "&eEste comando te servirá en nuestro soporte si tienes problemas"));
                    sender.sendMessage(TextUtils.format("&b&nSub comandos:"));
                    sender.sendMessage(TextUtils.format("&7/pdc debug info&f&l- &eInformación importante acerca del plugin, suele usarse en soporte."));
                    sender.sendMessage(TextUtils.format("&7/pdc debug generate_beginning&f&l- &eGeneración manual de The Beginning."));
                    return true;
                }

                String debugSub = args[1].toLowerCase();
                switch (debugSub) {
                    case "info":
                        sender.sendMessage(TextUtils.format(instance.prefix + "&6&lMostrando información debug para soporte"));
                        sender.sendMessage("");
                        sender.sendMessage(TextUtils.format("&fDía actual: &a" + DateManager.getInstance().getDay()));
                        sender.sendMessage(TextUtils.format("&fWorldEdit: " + (Bukkit.getPluginManager().getPlugin("WorldEdit") == null ? "&cNo instalado" : "&aInstalado, &eversión: &b" + Bukkit.getPluginManager().getPlugin("WorldEdit").getDescription().getVersion())));
                        sender.sendMessage(TextUtils.format("&fVersión del Plugin: &a" + this.instance.getDescription().getVersion()));
                        sender.sendMessage(TextUtils.format("&fVersión del Servidor: &a" + VersionManager.getFormattedVersion() + " &b(" + VersionManager.getVersion() + ")"));
                        sender.sendMessage(TextUtils.format("&fMundo de overworld: &a" + instance.world.getName()));
                        sender.sendMessage(TextUtils.format("&fMundo de end: &a" + instance.endWorld.getName()));
                        sender.sendMessage("");
                        sender.sendMessage(TextUtils.format("&eEsta información es brindada en nuestro discord, &f&nhttps://discord.gg/w58wzrcJU8"));
                        break;

                    case "toggle":
                        Main.DEBUG = !Main.DEBUG;
                        sender.sendMessage("Debug cambiado a " + Main.DEBUG);
                        break;

                    case "emptyworld":
                        World w = new WorldCreator("empty_world").generator(new EmptyGenerator()).createWorld();
                        if (w != null) {
                            w.setSpawnLocation(w.getBlockAt(0, 201, 0).getLocation());
                            w.getSpawnLocation().getBlock().getRelative(BlockFace.DOWN).setType(Material.GLASS);
                            pDebug.teleport(w.getSpawnLocation());
                        }
                        break;

                    case "module":
                        NMS.spawnDeathModule(pDebug.getLocation());
                        break;

                    case "health":
                        sender.sendMessage("Vida máxima: " + NetheriteArmor.getAvailableMaxHealth(pDebug));
                        break;

                    case "events":
                        sender.sendMessage("Eventos:");
                        sender.sendMessage("Shulker shell: " + (Main.getInstance().getShulkerEvent().isRunning() ? "corriendo, tiempo: " + Main.getInstance().getShulkerEvent().getTimeLeft() + ", bossbar:" + Main.getInstance().getShulkerEvent().getBossBar().getTitle() : "no corriendo"));
                        sender.sendMessage("Life Orb: " + (Main.getInstance().getOrbEvent().isRunning() ? "corriendo, tiempo: " + Main.getInstance().getOrbEvent().getTimeLeft() + ", bossbar:" + Main.getInstance().getOrbEvent().getBossBar().getTitle() : "no corriendo"));
                        break;

                    case "hasorb":
                        boolean hasOrb = instance.getOrbEvent().isRunning() || NetheriteArmor.checkForOrb(pDebug);
                        pDebug.sendMessage("Orb: " + hasOrb);
                        break;

                    case "hyper":
                        boolean doPlayerAteOne = pDebug.getPersistentDataContainer().has(new NamespacedKey(Main.getInstance(), "hyper_one"), PersistentDataType.BYTE);
                        boolean doPlayerAteTwo = pDebug.getPersistentDataContainer().has(new NamespacedKey(Main.getInstance(), "hyper_two"), PersistentDataType.BYTE);
                        pDebug.sendMessage("Gap 1: " + doPlayerAteOne + " | Gap2:" + doPlayerAteTwo);
                        break;

                    case "removegaps":
                        pDebug.getPersistentDataContainer().remove(new NamespacedKey(Main.getInstance(), "hyper_one"));
                        pDebug.getPersistentDataContainer().remove(new NamespacedKey(Main.getInstance(), "hyper_two"));
                        break;

                    case "showhealthskeleton":
                        if (args.length > 2) {
                            try {
                                int d = Integer.parseInt(args[2]);
                                pDebug.sendMessage("Actual health: " + (d < 50 ? 25 : d < 60 ? 40 : 110));
                            } catch (NumberFormatException ex) {
                                pDebug.sendMessage(ChatColor.RED + "Día inválido.");
                            }
                        }
                        break;

                    case "withertime":
                        pDebug.sendMessage("tiempo: " + pDebug.getPersistentDataContainer().get(new NamespacedKey(instance, "wither"), PersistentDataType.INTEGER));
                        break;

                    case "testtotems":
                        pDebug.sendMessage("Totems sin offhand debug: " + pDebug.getInventory().all(Material.TOTEM_OF_UNDYING).size());
                        break;

                    case "testtotemsb":
                        int totems = pDebug.getInventory().all(Material.TOTEM_OF_UNDYING).size();
                        if (pDebug.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
                            totems++;
                        }
                        pDebug.sendMessage("Totems con offhand debug: " + totems);
                        break;

                    case "testlingering":
                        pDebug.sendMessage("Lingering: " + Main.DISABLED_LINGERING);
                        break;

                    case "summonske":
                        WitherSkeleton skeleton = pDebug.getWorld().spawn(pDebug.getLocation().clone(), WitherSkeleton.class);
                        if (skeleton.getEquipment() != null) {
                            skeleton.getEquipment().setItemInMainHand(new ItemBuilder(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 32765).build());
                            skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
                        }
                        skeleton.setRemoveWhenFarAway(false);
                        skeleton.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                        skeleton.setCustomName(ChatColor.translateAlternateColorCodes('&', "&6Ultra Esqueleto Definitivo"));
                        instance.getNmsAccessor().setMaxHealth(skeleton, 400.0D, true);
                        break;

                    case "testwither":
                        pDebug.getPersistentDataContainer().set(new NamespacedKey(instance, "wither"), PersistentDataType.INTEGER, 3595);
                        break;

                    case "spawncreeper":
                        Location l = pDebug.getLocation().clone();
                        int y = l.getBlockY();
                        while (y < l.getWorld().getMaxHeight() - 1 && l.getWorld().getBlockAt(l.getBlockX(), y, l.getBlockZ()).getType() != Material.AIR) {
                            y++;
                        }

                        Random r = new Random();
                        int pX = (r.nextBoolean() ? -1 : 1) * r.nextInt(19);
                        int pZ = (r.nextBoolean() ? -1 : 1) * r.nextInt(19);
                        if (y == l.getWorld().getMaxHeight() - 1) y = l.getWorld().getHighestBlockYAt(pX, pZ);
                        Location f = new Location(l.getWorld(), l.getX() + pX, y, l.getZ() + pZ);

                        if (f.getBlock().getType() == Material.AIR && f.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                            instance.getFactory().spawnEnderQuantumCreeper(f, null);
                            pDebug.sendMessage("x: " + f.getBlockX() + " y: " + f.getBlockY() + " z: " + f.getBlockZ());
                        }
                        break;

                    case "addtimespeedrun":
                        if (args.length > 2) {
                            try {
                                instance.setPlayTime(instance.getPlayTime() + Integer.parseInt(args[2]));
                            } catch (NumberFormatException ignored) {}
                        }
                        break;

                    case "muerte":
                        if (args.length > 3) {
                            boolean b = Boolean.parseBoolean(args[3]);
                            DiscordPortal.banPlayer(Bukkit.getOfflinePlayer(args[2]), b);
                        }
                        break;

                    default:
                        sender.sendMessage(TextUtils.format("&c¡No existe ese sub-comando!"));
                        break;
                }
                break;

            case "mensaje":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(TextUtils.format("&cNecesitas usar este comando en el juego."));
                    return true;
                }
                Player pMsg = (Player) sender;

                if (args.length == 1) {
                    sender.sendMessage(TextUtils.format("&cDebes escribir un mensaje, ejemplo: /pdc mensaje He muerto"));
                    if (instance.getConfig().contains("Server-Messages.CustomDeathMessages." + pMsg.getName())) {
                        sender.sendMessage(TextUtils.format("&eTu mensaje de muerte actual es: &7" + instance.getConfig().getString("Server-Messages.CustomDeathMessages." + pMsg.getName())));
                    } else {
                        sender.sendMessage(TextUtils.format("&eTu mensaje de muerte actual es: &7" + instance.getConfig().getString("Server-Messages.DefaultDeathMessage")));
                    }
                    return true;
                }

                StringBuilder msgBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    msgBuilder.append(args[i]).append(" ");
                }
                String msg = msgBuilder.toString().trim();

                if (msg.contains("&")) {
                    sender.sendMessage(ChatColor.RED + "No se admite el uso de " + ChatColor.GOLD + "&");
                    return true;
                }

                instance.getConfig().set("Server-Messages.CustomDeathMessages." + pMsg.getName(), "&7" + msg);
                instance.saveConfig();
                instance.reloadConfig();

                pMsg.playSound(pMsg.getLocation(), Sound.ENTITY_BLAZE_DEATH, 10F, 0.5F);
                sender.sendMessage(TextUtils.format("&eHas cambiado tu mensaje de muerte a: &7" + msg));
                break;

            case "dias":
                if (instance.getDay() < 1) {
                    sender.sendMessage(instance.prefix + ChatColor.DARK_RED + "[ERROR] Se ha producido un error al cargar el dia, config.yml mal configurado.");
                } else {
                    if (Main.SPEED_RUN_MODE) {
                        sender.sendMessage(instance.prefix + ChatColor.RED + "Estamos en la hora: " + ChatColor.GRAY + instance.getDay());
                    } else {
                        sender.sendMessage(instance.prefix + ChatColor.RED + "Estamos en el día: " + ChatColor.GRAY + instance.getDay());
                    }
                }
                break;

            case "info":
                sender.sendMessage(instance.prefix + ChatColor.RED + "Version Info:");
                sender.sendMessage(ChatColor.GRAY + "- Nombre: " + ChatColor.GREEN + "PermaDeathCore.jar");
                sender.sendMessage(ChatColor.GRAY + "- Versión: " + ChatColor.GREEN + "PermaDeathCore v" + instance.getDescription().getVersion());
                sender.sendMessage(ChatColor.GRAY + "- Dificultades: " + ChatColor.GREEN + "Soportado de día 1 a día 60");
                sender.sendMessage(ChatColor.GRAY + "- Autor: " + ChatColor.GREEN + "Equipo de InfernalCore (Desarrollador principal: SebazCRC)");
                break;

            case "discord":
                sender.sendMessage(instance.prefix + ChatColor.BLUE + "https://discord.gg/w58wzrcJU8 | https://discord.gg/infernalcore");
                break;

            case "cambios":
                sender.sendMessage(TextUtils.format("&eEste plugin contiene &c&lTODOS &r&elos cambios de PermaDeath."));
                sender.sendMessage(TextUtils.format("&eMás información aquí:"));
                sender.sendMessage(TextUtils.format("&b> &f&lhttps://twitter.com/permadeathsmp"));
                sender.sendMessage(TextUtils.format("&b> &f&lhttps://permadeath.fandom.com/es/wiki/Cambios_de_dificultad"));
                break;

            case "beginning":
                if (!sender.hasPermission("permadeathcore.admin")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permiso para ejecutar este comando."));
                    return true;
                }

                if (args.length == 1) {
                    sender.sendMessage(TextUtils.format(instance.prefix + "&cLista de comandos para The Beginning"));
                    sender.sendMessage(TextUtils.format("&7/pdc beginning bendicion <jugador> &f&l- &cOtorga la bendición de The Beginning a un jugador."));
                    sender.sendMessage(TextUtils.format("&7/pdc beginning maldicion <jugador> &f&l- &cOtorga la maldición de The Beginning a un jugador."));
                    return true;
                }

                if (args.length == 2) {
                    sender.sendMessage(TextUtils.format("&cEscribe el nombre de un jugador."));
                    return true;
                }

                Player off = Bukkit.getPlayer(args[2]);
                if (off == null) {
                    sender.sendMessage(TextUtils.format("&c¡No hemos podido encontrar a ese jugador!"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("bendicion")) {
                    sender.sendMessage(TextUtils.format("&aSe ha otorgado la bendición de The Beginning a &b" + off.getName()));
                    Bukkit.broadcastMessage(TextUtils.format(Main.prefix + "&d&lEnhorabuena " + off.getName() + " has recibido la bendición del comienzo por entrar primero a The Beginning. Suerte."));
                    off.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (60 * 60 * 12 * 20), 1));
                } else if (args[1].equalsIgnoreCase("maldicion")) {
                    sender.sendMessage(TextUtils.format("&aSe ha otorgado la maldición de The Beginning a &b" + off.getName()));
                    Bukkit.broadcastMessage(TextUtils.format(Main.prefix + "&d&l" + off.getName() + ", ¡Desgracia! has recibido la maldición de The Beginning por entrar de último."));
                    Bukkit.broadcastMessage(TextUtils.format("&d&l¡Sufre y muere por lento! NO puedes usar cubos de leche dentro de Permadeath por 12 horas o serás PERMABANEADO."));
                    off.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (60 * 60 * 12 * 20), 0));
                    off.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (60 * 60 * 12 * 20), 0));
                }
                break;

            case "speedrun":
                if (!sender.hasPermission("permadeathcore.admin")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permiso para ejecutar este comando."));
                    return true;
                }

                if (args.length == 1) {
                    sendSpeedRunHelp(sender);
                    return true;
                }

                String spSub = args[1].toLowerCase();
                if (spSub.equals("toggle")) {
                    Main.SPEED_RUN_MODE = !Main.SPEED_RUN_MODE;
                    sender.sendMessage(TextUtils.format(Main.prefix + (Main.SPEED_RUN_MODE ? "&aHas activado el modo SpeedRun" : "&cHas desactivado el modo SpeedRun")));
                } else if (spSub.equals("tiempo")) {
                    sender.sendMessage(TextUtils.format(Main.prefix + "&eEl tiempo de juego actual es de: &b" + TextUtils.formatInterval(instance.getPlayTime())));
                } else if (spSub.equals("reset")) {
                    sender.sendMessage(TextUtils.format(Main.prefix + "&aHas reiniciado el tiempo del modo SpeedRun."));
                    instance.setPlayTime(0);
                } else {
                    sendSpeedRunHelp(sender);
                }
                break;

            case "event":
                if (!sender.hasPermission("permadeathcore.event")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permiso para ejecutar este comando."));
                    return true;
                }

                if (args.length == 1) {
                    sender.sendMessage(TextUtils.format("&cPor favor introduce un evento, ejemplo: &e/pdc event shulkershell"));
                    return true;
                }

                String evSub = args[1].toLowerCase();
                if (evSub.equals("shulkershell")) {
                    if (instance.getShulkerEvent().isRunning()) {
                        sender.sendMessage(TextUtils.format("&cEse evento ya está en ejecución."));
                        return true;
                    }
                    instance.getShulkerEvent().setRunning(true);
                    sender.sendMessage(TextUtils.format("&aSe ha iniciado el evento correctamente."));
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        instance.getShulkerEvent().addPlayer(online);
                    }
                } else if (evSub.equals("lifeorb")) {
                    if (instance.getOrbEvent().isRunning()) {
                        sender.sendMessage(TextUtils.format("&cEse evento ya está en ejecución."));
                        return true;
                    }
                    if (instance.getDay() < 60) {
                        sender.sendMessage(TextUtils.format("&cEste evento solo puede ser iniciado en días superiores a 60."));
                        return true;
                    }
                    instance.getOrbEvent().setRunning(true);
                    sender.sendMessage(TextUtils.format("&aSe ha iniciado el evento correctamente."));
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        instance.getOrbEvent().addPlayer(online);
                    }
                } else {
                    sender.sendMessage(TextUtils.format("&cNo hemos podido encontrar ese evento."));
                }
                break;

            case "locate":
                if (!sender.hasPermission("permadeathcore.locate")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permiso para ejecutar este comando."));
                    return true;
                }

                if (args.length == 1 || !args[1].equalsIgnoreCase("beginning")) {
                    sender.sendMessage(TextUtils.format("&eDebes introducir una palabra clave válida. Ejemplo: &7/pdc locate beginning"));
                    return true;
                }

                if (instance.getDay() < 40) {
                    sender.sendMessage(TextUtils.format("&c&lERROR&7: &eNo existe el portal a The Beginning, porque estamos en el día &b" + instance.getDay()));
                    return true;
                }

                if (instance.getBeData() == null) {
                    sender.sendMessage(TextUtils.format("&c&lERROR&7: &eNo pudimos encontrar The Beginning, por favor reinicia el servidor."));
                    return true;
                }

                World currentWorld = (sender instanceof Player) ? ((Player) sender).getWorld() : null;

                if (currentWorld == null || currentWorld.getName().equalsIgnoreCase(instance.world.getName())) {
                    if (!instance.getBeData().generatedOverWorldBeginningPortal()) {
                        sender.sendMessage(TextUtils.format("&c&lERROR&7: &eNo se ha generado el portal a The Beginning aún."));
                        return true;
                    }
                    int x = (int) instance.getBeData().getOverWorldPortal().getX();
                    int y = (int) instance.getBeData().getOverWorldPortal().getY();
                    int z = (int) instance.getBeData().getOverWorldPortal().getZ();
                    sender.sendMessage(TextUtils.format("&eCoordenadas del portal a The Beginning (overworld): &b" + x + " " + y + " " + z));
                } else if (currentWorld.getName().equalsIgnoreCase("pdc_the_beginning")) {
                    if (!instance.getBeData().generatedBeginningPortal()) {
                        sender.sendMessage(TextUtils.format("&c&lERROR&7: &eNo se ha generado el portal en la dimensión aún."));
                        return true;
                    }
                    int x = (int) instance.getBeData().getBeginningPortal().getX();
                    int y = (int) instance.getBeData().getBeginningPortal().getY();
                    int z = (int) instance.getBeData().getBeginningPortal().getZ();
                    sender.sendMessage(TextUtils.format("&eCoordenadas del portal a The Beginning (dimensión): &b" + x + " " + y + " " + z));
                } else {
                    sender.sendMessage(TextUtils.format("&c&lERROR&7: &eEste comando no puede ser ejecutado en tu mundo actual."));
                }
                break;

            case "give":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(TextUtils.format("&cNecesitas ser un jugador."));
                    return true;
                }
                Player pGive = (Player) sender;

                if (!pGive.hasPermission("permadeathcore.give")) {
                    pGive.sendMessage(TextUtils.format("&cNo tienes permisos."));
                    return true;
                }

                if (args.length == 1) {
                    pGive.sendMessage(TextUtils.format("&ePor favor introduce el ítem deseado"));
                    pGive.sendMessage(TextUtils.format("&eEjemplos: &7medalla - netheriteArmor - infernalArmor - infernalBlock - netheriteTools - lifeOrb - endRelic - beginningRelic"));
                    return true;
                }

                String itemTarget = args[1].toLowerCase();
                switch (itemTarget) {
                    case "netheritearmor":
                        pGive.getInventory().addItem(NetheriteArmor.craftNetheriteHelmet(), NetheriteArmor.craftNetheriteChest(), NetheriteArmor.craftNetheriteLegs(), NetheriteArmor.craftNetheriteBoots());
                        pGive.sendMessage(TextUtils.format("&eHas recibido la armadura de Netherite."));
                        break;
                    case "medalla":
                        String medalla = TextUtils.format("&4&l[&c&l☠&4&l] &e&ki &r&6&lMedalla de Superviviente &e&ki &r&4&l[&c&l☠&4&l]");
                        pGive.getInventory().addItem(new ItemBuilder(Material.TOTEM_OF_UNDYING).setUnbrekeable(true).addItemFlag(ItemFlag.HIDE_UNBREAKABLE).setDisplayName(medalla).build());
                        pGive.sendMessage(TextUtils.format("&eHas recibido la medalla de superviviente."));
                        break;
                    case "infernalarmor":
                        pGive.getInventory().addItem(InfernalNetherite.craftNetheriteHelmet(), InfernalNetherite.craftNetheriteChest(), InfernalNetherite.craftNetheriteLegs(), InfernalNetherite.craftNetheriteBoots());
                        pGive.sendMessage(TextUtils.format("&eHas recibido la armadura de Netherite Infernal."));
                        break;
                    case "netheritetools":
                        pGive.getInventory().addItem(PermadeathItems.craftNetheritePickaxe(), PermadeathItems.craftNetheriteSword(), PermadeathItems.craftNetheriteAxe(), PermadeathItems.craftNetheriteShovel(), PermadeathItems.craftNetheriteHoe());
                        pGive.sendMessage(TextUtils.format("&eHas recibido las herramientas de Netherite."));
                        break;
                    case "infernalblock":
                        pGive.getInventory().addItem(PermadeathItems.craftInfernalNetheriteIngot());
                        pGive.sendMessage(TextUtils.format("&eHas recibido el Bloque de Netherite Infernal."));
                        break;
                    case "lifeorb":
                        pGive.getInventory().addItem(PermadeathItems.createLifeOrb());
                        pGive.sendMessage(TextUtils.format("&eHas recibido el Orbe de Vida."));
                        break;
                    case "endrelic":
                        pGive.getInventory().addItem(PermadeathItems.crearReliquia());
                        pGive.sendMessage(TextUtils.format("&eHas recibido la Reliquia del Fin."));
                        break;
                    case "beginningrelic":
                        pGive.getInventory().addItem(PermadeathItems.createBeginningRelic());
                        pGive.sendMessage(TextUtils.format("&eHas recibido la Reliquia del Comienzo."));
                        break;
                    default:
                        pGive.sendMessage(TextUtils.format("&eItem no reconocido."));
                        break;
                }
                break;

            case "afk":
                if (!sender.hasPermission("permadeathcore.admin")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permisos para ejecutar este comando."));
                    return true;
                }

                if (args.length == 1) {
                    sender.sendMessage(TextUtils.format("&cLista de comandos disponibles para el sistema Anti-AFK"));
                    sender.sendMessage(TextUtils.format("&7/pdc afk unban <jugador> &f&l- &cRevoca un baneo por AFK."));
                    sender.sendMessage(TextUtils.format("&7/pdc afk bypass <add/remove> <jugador> &f&l- &cAgrega o elimina un jugador a la lista de personas inmunes."));
                    return true;
                }

                String afkSub = args[1].toLowerCase();
                if (afkSub.equals("unban")) {
                    if (args.length == 2) {
                        sender.sendMessage(TextUtils.format("&cPor favor, ingresa a un jugador."));
                        return true;
                    }
                    String paramPlayer = args[2];
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pardon " + paramPlayer);

                    PlayerDataManager data = new PlayerDataManager(paramPlayer, instance);
                    data.setLastDay(instance.getDay());
                    sender.sendMessage(TextUtils.format("&aAhora el jugador &e" + paramPlayer + " &apodrá volver a jugar."));

                } else if (afkSub.equals("bypass")) {
                    if (args.length <= 3) {
                        sender.sendMessage(TextUtils.format("&ePor favor, ingresa todos los argumentos del comando."));
                        return true;
                    }
                    String action = args[2].toLowerCase();
                    String paramPlayer = args[3];
                    java.util.List<String> list = instance.getConfig().getStringList("AntiAFK.Bypass");

                    if (action.equals("add")) {
                        if (list.contains(paramPlayer)) {
                            sender.sendMessage(TextUtils.format("&cEl jugador &e" + paramPlayer + " &cya se encuentra en la lista de jugadores inmunes."));
                            return true;
                        }
                        list.add(paramPlayer);
                        instance.getConfig().set("AntiAFK.Bypass", list);
                        instance.saveConfig();
                        sender.sendMessage(TextUtils.format("&aEl jugador &e" + paramPlayer + " &aha sido agregado a la lista de jugadores inmunes."));
                    } else if (action.equals("remove")) {
                        if (!list.contains(paramPlayer)) {
                            sender.sendMessage(TextUtils.format("&cEl jugador &e" + paramPlayer + " &cno se encuentra en la lista de jugadores inmunes."));
                            return true;
                        }
                        list.remove(paramPlayer);
                        instance.getConfig().set("AntiAFK.Bypass", list);
                        instance.saveConfig();
                        sender.sendMessage(TextUtils.format("&aEl jugador &e" + paramPlayer + " &aha sido eliminado de la lista de jugadores inmunes."));
                    } else {
                        sender.sendMessage(TextUtils.format("&cAcción &e" + action + " &cno reconocida."));
                    }
                }
                break;

            case "storm":
                if (!sender.hasPermission("permadeathcore.admin")) {
                    sender.sendMessage(TextUtils.format("&cNo tienes permisos para ejecutar este comando."));
                    return true;
                }

                if (args.length <= 2) {
                    sender.sendMessage(TextUtils.format("&cLista de comandos disponibles para la tormenta."));
                    sender.sendMessage(TextUtils.format("&7/pdc storm removeHours <horas> &f&l- &cElimina cantidad de horas de la tormenta."));
                    sender.sendMessage(TextUtils.format("&7/pdc storm addHours <horas> &f&l- &cAgrega cantidad de horas a la tormenta."));
                    return true;
                }

                String operation = args[1];
                int hoursAmount = 1;

                try {
                    hoursAmount = Math.max(1, Integer.parseInt(args[2]));
                } catch (NumberFormatException x) {
                    sender.sendMessage(TextUtils.format("&cIngresa una cantidad válida."));
                    return true;
                }

                for (World w : Bukkit.getWorlds().stream().filter(world1 -> world1.getEnvironment() == World.Environment.NORMAL).collect(Collectors.toList())) {
                    if (operation.equalsIgnoreCase("addHours")) {
                        int stormDuration = w.getWeatherDuration();
                        int stormTicksToSeconds = stormDuration / 20;
                        int intsTicks = hoursAmount * 3600;

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather thunder");

                        if (w.hasStorm() || w.isThundering()) {
                            w.setWeatherDuration((stormTicksToSeconds + intsTicks) * 20);
                        } else {
                            w.setWeatherDuration(intsTicks * 20);
                        }
                        sender.sendMessage(TextUtils.format("&aOperación completada exitosamente."));
                    } else if (operation.equalsIgnoreCase("removeHours")) {
                        if (w.hasStorm() || w.isThundering()) {
                            int stormDuration = w.getWeatherDuration();
                            int stormTicksToSeconds = stormDuration / 20;
                            int newStormTime = Math.max(1, stormTicksToSeconds - (hoursAmount * 3600));
                            w.setWeatherDuration(newStormTime * 20);
                            sender.sendMessage(TextUtils.format("&aTormenta reducida con éxito."));
                        } else {
                            sender.sendMessage(TextUtils.format("&cNo hay ninguna tormenta en marcha."));
                        }
                    }
                }
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(instance.prefix + ChatColor.RED + "Comandos disponibles:");
        sender.sendMessage(ChatColor.RED + "/pdc idioma <es, en> " + ChatColor.GRAY + ChatColor.ITALIC + "(Cambia tu idioma)");
        sender.sendMessage(ChatColor.RED + "/pdc dias " + ChatColor.GRAY + ChatColor.ITALIC + "(Muestra el día en el que está el plugin)");
        sender.sendMessage(ChatColor.RED + "/pdc duracion " + ChatColor.GRAY + ChatColor.ITALIC + "(Muestra la duración de la tormenta)");
        sender.sendMessage(ChatColor.RED + "/pdc cambios " + ChatColor.GRAY + ChatColor.ITALIC + "(Muestra los cambios de dificultad disponibles)");

        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "/pdc mensaje <mensaje> " + ChatColor.GRAY + ChatColor.ITALIC + "(Cambia tu mensaje de muerte)");
            sender.sendMessage(ChatColor.RED + "/pdc awake " + ChatColor.GRAY + ChatColor.ITALIC + "(Muestra el tiempo despierto)");
        }
        sender.sendMessage(ChatColor.RED + "/pdc info " + ChatColor.GRAY + ChatColor.ITALIC + "(Información general)");
        sender.sendMessage(ChatColor.RED + "/pdc discord " + ChatColor.GRAY + ChatColor.ITALIC + "(Discord oficial del plugin)");

        if (sender.hasPermission("permadeathcore.admin")) {
            sender.sendMessage("");
            sender.sendMessage(instance.prefix + ChatColor.RED + "Comandos de administrador:");
            sender.sendMessage(ChatColor.RED + "/pdc debug " + ChatColor.GRAY + ChatColor.ITALIC + "(Información importante para el soporte)");
            sender.sendMessage(ChatColor.RED + "/pdc reload " + ChatColor.GRAY + ChatColor.ITALIC + "(Recarga el archivo config.yml)");
            sender.sendMessage(ChatColor.RED + "/pdc afk " + ChatColor.GRAY + ChatColor.ITALIC + "(Administra el sistema Anti-AFK)");
            sender.sendMessage(ChatColor.RED + "/pdc storm " + ChatColor.GRAY + ChatColor.ITALIC + "(Administra la tormenta)");
            sender.sendMessage(ChatColor.RED + "/pdc cambiarDia <dia> " + ChatColor.GRAY + ChatColor.ITALIC + "(Cambia el día actual)");
            sender.sendMessage(ChatColor.RED + "/pdc speedrun " + ChatColor.GRAY + ChatColor.ITALIC + "(Comandos del modo SpeedRun)");
            sender.sendMessage(ChatColor.RED + "/pdc beginning " + ChatColor.GRAY + ChatColor.ITALIC + "(Comandos de TheBeginning)");
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "/pdc give <item> " + ChatColor.GRAY + ChatColor.ITALIC + "(Obtén ítems especiales)");
            }
            sender.sendMessage(ChatColor.RED + "/pdc event <evento> " + ChatColor.GRAY + ChatColor.ITALIC + "(Comienza un evento)");
            sender.sendMessage(ChatColor.RED + "/pdc locate <beginning> " + ChatColor.GRAY + ChatColor.ITALIC + "(Localiza el portal a The Beginning)");
        }
    }

    private void sendSpeedRunHelp(CommandSender sender) {
        sender.sendMessage(TextUtils.format(instance.prefix + "&cLista de comandos para el modo SpeedRun"));
        sender.sendMessage(TextUtils.format("&7/pdc speedrun toggle &f&l- &cActiva o desactiva el modo SpeedRun."));
        sender.sendMessage(TextUtils.format("&7/pdc speedrun tiempo &f&l- &cObtén el tiempo de juego total."));
        sender.sendMessage(TextUtils.format("&7/pdc speedrun reset &f&l- &cReinicia el tiempo."));
    }
}
               int hours = 1;

                    try {
                        hours = Integer.parseInt(args[2]);

                        if (hours < 1) {
                            hours = 1;
                        }
                    } catch (Exception x) {
                        player.sendMessage(TextUtils.format("&cIngresa una cantidad válida."));
                    }

                    for (World w : Bukkit.getWorlds()
                            .stream()
                            .filter(world1 -> world1.getEnvironment() == World.Environment.NORMAL)
                            .collect(Collectors.toList())) {

                        if (operation.equalsIgnoreCase("addHours")) {
                            int stormDuration = w.getWeatherDuration();
                            int stormTicksToSeconds = stormDuration / 20;
                            long stormIncrement = stormTicksToSeconds + hours * 3600;
                            int intsTicks = (int) hours * 3600;
                            int inc = (int) stormIncrement;

                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:weather thunder");

                            if (w.hasStorm() || w.isThundering()) {
                                instance.world.setWeatherDuration(inc * 20);
                            } else {
                                instance.world.setWeatherDuration(intsTicks * 20);
                            }

                            player.sendMessage(TextUtils.format("&aOperación completada exitosamente."));
                        } else {

                            if (w.hasStorm() || w.isThundering()) {
                                int stormDuration = w.getWeatherDuration();
                                int stormTicksToSeconds = stormDuration / 20;
                                int newStormTime = Math.max(1, stormTicksToSeconds - hours * 3600);
                                instance.world.setWeatherDuration(newStormTime * 20);
                            } else {
                                player.sendMessage(TextUtils.format("&cNo hay ninguna tormenta en marcha."));
                            }
                        }
                    }

                } else {
                    sendHelp(player);
                }
            }
        }

        return false;
    }

    private void sendHelp(CommandSender sender) {

        sender.sendMessage(instance.prefix + ChatColor.RED + "Comandos disponibles:");
        sender.sendMessage(ChatColor.RED + "/pdc idioma <es, en>" + ChatColor.GRAY + ChatColor.ITALIC + "(Cambia tu idioma)");
        sender.sendMessage(ChatColor.RED + "/pdc dias " + ChatColor.GRAY + ChatColor.ITALIC + "(Muestra el día en el que está el plugin)");
        sender.sendMessage(ChatColor.RED + "/pdc duracion " + ChatColor.GRAY + ChatColor.ITALIC + "(Muestra la duración de la tormenta)");
        sender.sendMessage(ChatColor.RED + "/pdc cambios " + ChatColor.GRAY + ChatColor.ITALIC + "(Muestra los cambios de dificultad disponibles)");

        if (sender instanceof Player) {

            sender.sendMessage(ChatColor.RED + "/pdc mensaje <mensaje> " + ChatColor.GRAY + ChatColor.ITALIC + "(Cambia tu mensaje de muerte)");
            sender.sendMessage(ChatColor.RED + "/pdc awake " + ChatColor.GRAY + ChatColor.ITALIC + "(Muestra el tiempo despierto)");
        }
        sender.sendMessage(ChatColor.RED + "/pdc info " + ChatColor.GRAY + ChatColor.ITALIC + "(Información general)");
        sender.sendMessage(ChatColor.RED + "/pdc discord " + ChatColor.GRAY + ChatColor.ITALIC + "(Discord oficial del plugin)");

        if (sender.hasPermission("permadeathcore.admin")) {

            sender.sendMessage("");
            sender.sendMessage(instance.prefix + ChatColor.RED + "Comandos de administrador:");
            sender.sendMessage(ChatColor.RED + "/pdc debug " + ChatColor.GRAY + ChatColor.ITALIC + "(Información importante para el soporte)");
            sender.sendMessage(ChatColor.RED + "/pdc reload " + ChatColor.GRAY + ChatColor.ITALIC + "(Recarga el archivo config.yml)");
            sender.sendMessage(ChatColor.RED + "/pdc afk " + ChatColor.GRAY + ChatColor.ITALIC + "(Administra el sistema Anti-AFK)");
            sender.sendMessage(ChatColor.RED + "/pdc storm " + ChatColor.GRAY + ChatColor.ITALIC + "(Administra la tormenta)");
            sender.sendMessage(ChatColor.RED + "/pdc cambiarDia <dia> " + ChatColor.GRAY + ChatColor.ITALIC + "(Cambia el día actual, pd: puede que requiera un reinicio)");
            sender.sendMessage(ChatColor.RED + "/pdc speedrun " + ChatColor.GRAY + ChatColor.ITALIC + "(Comandos del modo SpeedRun, cada día es una hora)");
            sender.sendMessage(ChatColor.RED + "/pdc beginning " + ChatColor.GRAY + ChatColor.ITALIC + "(Comandos de TheBeginning)");
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "/pdc give <medalla, netheriteArmor, infernalArmor, infernalBlock, netheriteTools> " + ChatColor.GRAY + ChatColor.ITALIC + "(Obtén ítems especiales de Permadeath)");
            }
            sender.sendMessage(ChatColor.RED + "/pdc event <shulkershell, lifeorb> " + ChatColor.GRAY + ChatColor.ITALIC + "(Comienza un evento)");
            sender.sendMessage(ChatColor.RED + "/pdc locate <beginning> " + ChatColor.GRAY + ChatColor.ITALIC + "(Localiza el portal a The Beginning)");
        }
    }
}
