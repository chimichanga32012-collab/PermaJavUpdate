package tech.sebazcrc.permadeath.event.player;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.Utils;
import tech.sebazcrc.permadeath.util.item.InfernalNetherite;
import tech.sebazcrc.permadeath.util.item.NetheriteArmor;
import tech.sebazcrc.permadeath.util.item.PermadeathItems;
import tech.sebazcrc.permadeath.util.lib.HiddenStringUtils;
import tech.sebazcrc.permadeath.util.lib.ItemBuilder;
import tech.sebazcrc.permadeath.util.lib.UpdateChecker;
import tech.sebazcrc.permadeath.data.EndDataManager;
import tech.sebazcrc.permadeath.data.PlayerDataManager;
import tech.sebazcrc.permadeath.util.TextUtils;
import tech.sebazcrc.permadeath.discord.DiscordPortal;

import java.util.*;

public class PlayerListener implements Listener {

    private final ArrayList<Player> sleeping = new ArrayList<>();
    private final ArrayList<Player> globalSleeping = new ArrayList<>();

    private long stormTicks;
    private long stormHours;

    public PlayerListener() {
        loadTicks();
    }

    public void loadTicks() {
        long day = Main.getInstance().getDay();
        if (day <= 24) {
            this.stormTicks = day * 3600;
        } else if (day < 50) {
            this.stormTicks = (day - 24) * 3600;
        } else if (day == 50) {
            this.stormTicks = 3600 / 2;
        } else if (day < 75) {
            this.stormTicks = ((day - 49) * 3600) / 2;
        }
        this.stormHours = stormTicks / 3600;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        String victim = p.getName();
        boolean weather = Main.instance.world.hasStorm();
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String msg = Main.getInstance().getMessages().getMessage("DeathMessageChat", player).replace("%player%", victim);
            player.sendMessage(msg);

            String serverMessageTitle = Main.getInstance().getMessages().getMessage("DeathMessageTitle", player);
            String serverMessageSubtitle = Main.getInstance().getMessages().getMessage("DeathMessageSubtitle", player);

            player.sendTitle(serverMessageTitle, serverMessageSubtitle.replace("%player%", victim), 20, 100, 20);
            if (Main.instance.getConfig().getBoolean("Toggles.DefaultDeathSoundsEnabled")) {
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, Float.MAX_VALUE, -0.1f);
            }
            player.playSound(player.getLocation(), "pdc_muerte", Float.MAX_VALUE, 1.0F);
        }

        loadTicks();
        int stormDuration = Main.instance.world.getWeatherDuration();
        long stormIncrement = (stormDuration / 20) + this.stormTicks;

        boolean doEnableOP = Main.instance.getConfig().getBoolean("Toggles.OP-Ban");
        boolean causingProblems = doEnableOP || !p.hasPermission("permadeathcore.banoverride");

        if (causingProblems) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:weather thunder");

            if (weather) {
                Main.instance.world.setWeatherDuration((int) (stormIncrement * 20));
            } else {
                Main.instance.world.setWeatherDuration((int) (this.stormTicks * 20));
            }

            if (Main.instance.getDay() >= 25) {
                for (World w : Bukkit.getWorlds()) {
                    for (LivingEntity l : w.getLivingEntities()) {
                        Main.instance.deathTrainEffects(l);
                    }
                }
            }

            if (Main.instance.getDay() >= 50) {
                if (Main.instance.getBeginningManager() != null) {
                    Main.instance.getBeginningManager().closeBeginning();
                }
                Bukkit.broadcastMessage(TextUtils.format(Main.prefix + "&e¡Ha comenzado el modo UHC!"));
                Main.instance.world.setGameRule(GameRule.NATURAL_REGENERATION, false);
            }

            scheduler.runTaskLater(Main.instance, () -> {
                loadTicks();
                long hours = stormTicks / 3600;
                long minutes = (stormTicks / 60) % 60;
                String ct = String.valueOf(hours);
                String path = "DeathTrainMessage";

                if (minutes == 30 || minutes == 60) {
                    path += "Minutes";
                    ct = hours >= 1 ? hours + " horas y " + minutes : String.valueOf(minutes);
                } else if (minutes == 0) {
                    ct = String.valueOf(hours);
                }

                String time = ct;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    String msg = Main.getInstance().getMessages().getMessage(path, online).replace("%tiempo%", time);
                    online.sendMessage(msg);
                    if (Main.instance.getConfig().getBoolean("Toggles.DefaultDeathSoundsEnabled")) {
                        online.playSound(online.getLocation(), Sound.ENTITY_SKELETON_HORSE_DEATH, 10, 1);
                    }
                }

                String consoleMsg = Main.getInstance().getMessages().getMsgForConsole(path).replace("%tiempo%", time);
                Main.getInstance().getMessages().sendConsole(consoleMsg);
                DiscordPortal.onDeathTrain(consoleMsg);
            }, 100L);
        } else {
            Bukkit.broadcastMessage(TextUtils.format(Main.instance.prefix + "&eEl jugador &b" + p.getName() + " &eno puede dar más horas de tormenta."));
        }

        PlayerDataManager man = new PlayerDataManager(p.getName(), Main.instance);
        if (p.getLastDamageCause() != null) {
            man.setAutoDeathCause(p.getLastDamageCause().getCause());
        }
        man.setDeathTime();
        man.setDeathDay();
        man.setDeathCoords(p.getLocation());
        DiscordPortal.banPlayer(p, false);

        String configPath = "Server-Messages.CustomDeathMessages." + p.getName();
        String rawMsg = Main.instance.getConfig().contains(configPath) ? Main.instance.getConfig().getString(configPath) : Main.instance.getConfig().getString("Server-Messages.DefaultDeathMessage");
        if (rawMsg != null) {
            String msg = TextUtils.format(rawMsg.replace("%player%", p.getName()));
            Bukkit.broadcastMessage(StringUtils.capitalize(msg) + (msg.endsWith(".") ? "" : "."));
        }

        Main.getInstance().getMessages().sendConsole(Main.getInstance().getMessages().getMsgForConsole("DeathMessageChat").replace("%player%", victim));
        if (Main.instance.getConfig().getBoolean("Server-Messages.coords-msg-enable")) {
            Location loc = p.getLocation();
            Bukkit.broadcastMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "X: " + loc.getBlockX() + " || Y: " + loc.getBlockY() + " || Z: " + loc.getBlockZ() + ChatColor.RESET);
        }

        p.setGameMode(GameMode.SPECTATOR);
        scheduler.runTaskLater(Main.instance, () -> {
            boolean isban = doEnableOP || !p.hasPermission("permadeathcore.banoverride");
            if (Main.instance.getConfig().getBoolean("ban-enabled") && isban) {
                if (p.isOnline()) {
                    p.kickPlayer(ChatColor.RED + "Has sido PERMABANEADO");
                }
                Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(), ChatColor.RED + "Has sido PERMABANEADO", null, "console");
            }
        }, 40L);

        scheduler.runTaskLater(Main.getInstance(), () -> {
            if (Main.getInstance().getConfig().getBoolean("Toggles.Player-Skulls")) {
                Location l = p.getEyeLocation().clone();
                if (l.getY() < 3) l.setY(3);
                Block skullBlock = l.getBlock();
                skullBlock.setType(Material.PLAYER_HEAD);

                if (skullBlock.getState() instanceof Skull skullState) {
                    skullState.setOwningPlayer(p);
                    skullState.update();
                }

                if (skullBlock.getBlockData() instanceof Rotatable rotatable) {
                    rotatable.setRotation(getRotation(p));
                    skullBlock.setBlockData(rotatable);
                }

                skullBlock.getRelative(BlockFace.DOWN).setType(Material.NETHER_BRICK_FENCE);
                skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
            }
        }, 10L);
    }

    public BlockFace getRotation(Player player) {
        float rotation = player.getLocation().getYaw();
        if (rotation < 0) rotation += 360.0;

        if (rotation < 22.5) return BlockFace.NORTH;
        if (rotation < 67.5) return BlockFace.NORTH_EAST;
        if (rotation < 112.5) return BlockFace.EAST;
        if (rotation < 157.5) return BlockFace.SOUTH_EAST;
        if (rotation < 202.5) return BlockFace.SOUTH;
        if (rotation < 247.5) return BlockFace.SOUTH_WEST;
        if (rotation < 292.5) return BlockFace.WEST;
        if (rotation < 337.5) return BlockFace.NORTH_WEST;
        return BlockFace.NORTH;
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(TextUtils.format("&cSolo puedes dormir en el Overworld."));
            return;
        }

        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            player.sendMessage(TextUtils.format("&cNo puedes dormir ahora."));
            return;
        }

        long day = Main.getInstance().getDay();
        if (day >= 20) {
            Location playerbed = event.getBed().getLocation().add(0, 1, 0);
            Main.instance.world.playSound(playerbed, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
            Main.instance.world.spawnParticle(Particle.HUGE_EXPLOSION, playerbed, 1);

            if (day < 50 || new SplittableRandom().nextInt(100) + 1 <= 10) {
                player.sendMessage(TextUtils.format(Main.prefix + " &aHas restablecido el contador de Phantoms."));
                player.setStatistic(Statistic.TIME_SINCE_REST, 0);
            }

            event.setCancelled(true);
            return;
        }

        long time = Main.instance.world.getTime();
        int neededPlayers = day >= 10 ? 4 : 1;

        if (Bukkit.getOnlinePlayers().size() < neededPlayers) {
            player.sendMessage(TextUtils.format("&cNo puedes dormir porque no hay suficientes personas en línea (" + neededPlayers + ")."));
            event.setCancelled(true);
            return;
        }

        if (time < 13000) {
            player.sendMessage(TextUtils.format("&cSolo puedes dormir de noche."));
            event.setCancelled(true);
            return;
        }

        if (day < 10) {
            ArrayList<Player> sent = new ArrayList<>();
            Bukkit.getServer().getScheduler().runTaskLater(Main.instance, () -> {
                event.getPlayer().getWorld().setTime(0L);
                player.setStatistic(Statistic.TIME_SINCE_REST, 0);

                if (!sent.contains(player)) {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        String msg = Main.getInstance().getMessages().getMessage("Sleep", p).replace("%player%", player.getName());
                        p.sendMessage(msg);
                    });
                    Main.getInstance().getMessages().sendConsole(Main.getInstance().getMessages().getMsgForConsole("Sleep").replace("%player%", player.getName()));
                    sent.add(player);
                    player.damage(0.1);
                }
            }, 60L);
        } else if (day <= 19) {
            globalSleeping.add(player);
            Bukkit.getOnlinePlayers().forEach(p -> {
                String msg = Main.getInstance().getMessages().getMessage("Sleeping", p).replace("%needed%", "4").replace("%players%", String.valueOf(globalSleeping.size())).replace("%player%", player.getName());
                p.sendMessage(msg);
            });

            Main.getInstance().getMessages().sendConsole(Main.getInstance().getMessages().getMsgForConsole("Sleeping").replace("%needed%", "4").replace("%players%", String.valueOf(globalSleeping.size())).replace("%player%", player.getName()));

            if (globalSleeping.size() >= neededPlayers && globalSleeping.size() < Bukkit.getOnlinePlayers().size()) {
                Bukkit.getServer().getScheduler().runTaskLater(Main.instance, () -> {
                    if (globalSleeping.size() >= 4) {
                        event.getPlayer().getWorld().setTime(0L);
                        for (Player all : Bukkit.getOnlinePlayers()) {
                            if (all.isSleeping()) {
                                all.setStatistic(Statistic.TIME_SINCE_REST, 0);
                                all.damage(0.1);
                                Bukkit.broadcastMessage(TextUtils.format(Objects.requireNonNull(Main.instance.getConfig().getString("Server-Messages.Sleep").replace("%player%", all.getName()))));
                            }
                        }
                        Bukkit.broadcastMessage(TextUtils.format("&eHan dormido suficientes jugadores (&b4&e)."));
                        globalSleeping.clear();
                    }
                }, 40L);
            }

            if (globalSleeping.size() == Bukkit.getOnlinePlayers().size()) {
                event.getPlayer().getWorld().setTime(0L);
                for (Player all : Bukkit.getOnlinePlayers()) {
                    all.setStatistic(Statistic.TIME_SINCE_REST, 0);
                    all.damage(0.1);
                    Bukkit.broadcastMessage(TextUtils.format(Objects.requireNonNull(Main.instance.getConfig().getString("Server-Messages.Sleep").replace("%player%", all.getName()))));
                }
                Bukkit.broadcastMessage(TextUtils.format("&eHan dormido todos los jugadores."));
                globalSleeping.clear();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent e) {
        Player p = e.getPlayer();
        if (p.getWorld().getEnvironment() != World.Environment.NORMAL) return;

        sleeping.remove(p);
        globalSleeping.remove(p);

        if (p.getWorld().getTime() < 13000) return;
        p.sendMessage(TextUtils.format("&eHas abandonado la cama, ya no contarás para pasar la noche."));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        e.setJoinMessage(null);

        Bukkit.getOnlinePlayers().forEach(p -> {
            String joinMessage = Main.getInstance().getMessages().getMessage("OnJoin", p).replace("%player%", player.getName());
            p.sendMessage(joinMessage);
        });

        Main.getInstance().getMessages().sendConsole(Main.getInstance().getMessages().getMsgForConsole("OnJoin").replace("%player%", player.getName()));

        if (Main.instance.getShulkerEvent().isRunning()) {
            Main.instance.getShulkerEvent().addPlayer(player);
        }

        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            if (!player.isOnline()) return;

            player.sendMessage(TextUtils.format("&e&m-------------------------------------------"));
            player.sendMessage(TextUtils.format("        &c&lPERMA&7&lDEATH"));
            player.sendMessage(TextUtils.format(" "));
            player.sendMessage(TextUtils.format("&b&l - Servidor de Discord con soporte del Desarrollador: -"));
            player.sendMessage(TextUtils.format("&7Se ofrece soporte en caso de problemas"));
            player.sendMessage(TextUtils.format(" "));
            player.sendMessage(TextUtils.format("&e&nInvitación a Discord&r&7 (soporte, noticias y proyectos):"));
            player.sendMessage(TextUtils.format("&9" + Utils.DISCORD_LINK));
            player.sendMessage(TextUtils.format("&e&m-------------------------------------------"));
            if (!Main.optifineItemsEnabled())
                player.sendMessage(TextUtils.format("&cRecuerda aceptar los paquetes de Recursos para ver los ítems y texturas personalizadas."));
            player.sendMessage(Main.prefix + TextUtils.format("&eEjecuta el comando &f&l/pdc &r&epara más información."));

            if (!player.hasPlayedBefore()) {
                player.sendTitle(TextUtils.format("&c&lPERMA&7&lDEATH"), TextUtils.format("&7Desarrollador: &b@SebazCRC"), 1, 100, 1);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100.0F, 100.0F);
            }
        }, 300L);

        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            if (!player.isOnline()) return;
            if (!player.hasPlayedBefore()) {
                player.sendTitle(TextUtils.format("&c&lPERMA&7&lDEATH"), TextUtils.format("&7Discord: &9https://discord.gg/8evPbuxPke"), 1, 100, 1);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100.0F, 100.0F);
            }

            if (player.isOp()) {
                new UpdateChecker(Main.getInstance()).getVersion(version -> {
                    if (Main.getInstance().getDescription().getVersion().equalsIgnoreCase(version)) {
                        player.sendMessage(TextUtils.format(Main.prefix + "&3Estás utilizando la versión más reciente del Plugin."));
                    } else {
                        player.sendMessage(TextUtils.format(Main.prefix + "&3Se ha encontrado una nueva versión del Plugin"));
                        player.sendMessage(TextUtils.format(Main.prefix + "&eDescarga en: &7" + Utils.SPIGOT_LINK));
                    }
                });
            }
        }, 400L);

        if (!Main.optifineItemsEnabled())
            player.setResourcePack(Utils.RESOURCE_PACK_LINK);

        if (Main.instance.getBeginningManager() != null && Main.instance.getBeginningManager().getBeginningWorld() != null) {
            if (Main.instance.getBeginningManager().isClosed() && player.getWorld().getName().equalsIgnoreCase(Main.instance.getBeginningManager().getBeginningWorld().getName())) {
                player.teleport(Main.instance.world.getSpawnLocation());
            }

            if (Main.worldEditFound) {
                if (!Main.instance.getBeData().generatedOverWorldBeginningPortal()) {
                    Main.instance.getBeginningManager().generatePortal(true, null);
                }
                if (!Main.instance.getBeData().generatedBeginningPortal()) {
                    Location endLoc = new Location(Main.instance.getBeginningManager().getBeginningWorld(), 50, 140, 50);
                    Main.instance.getBeginningManager().generatePortal(false, endLoc);
                    Main.instance.getBeginningManager().getBeginningWorld().setSpawnLocation(endLoc);
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.setQuitMessage(null);
        Bukkit.getOnlinePlayers().forEach(p -> {
            String joinMessage = Main.getInstance().getMessages().getMessage("OnLeave", p).replace("%player%", player.getName());
            p.sendMessage(joinMessage);
        });

        Main.getInstance().getMessages().sendConsole(Main.getInstance().getMessages().getMsgForConsole("OnLeave").replace("%player%", player.getName()));
        Main.instance.getShulkerEvent().removePlayer(player);
        Main.instance.getOrbEvent().removePlayer(player);

        sleeping.remove(player);
        globalSleeping.remove(player);
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (Main.instance.getConfig().getBoolean("anti-afk-enabled")) {
            if (Main.instance.getConfig().getStringList("AntiAFK.Bypass").contains(e.getName())) return;

            PlayerDataManager dataManager = new PlayerDataManager(e.getName(), Main.instance);
            long actualDay = Main.instance.getDay();
            long lastConnection = dataManager.getLastDay();

            if (actualDay < lastConnection) {
                dataManager.setLastDay(actualDay);
                return;
            }

            OfflinePlayer off = Bukkit.getOfflinePlayer(e.getName());
            if (off.isBanned() || !off.isWhitelisted()) return;

            if ((actualDay - lastConnection) >= Main.instance.getConfig().getInt("AntiAFK.DaysForBan")) {
                String reason = TextUtils.format("&c&lHas sido PERMABANEADO\n&eRazón: AFK\n&7Si crees que es un\n&7error, contacta un\n&7administrador.");
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, reason);
                Bukkit.getBanList(BanList.Type.NAME).addBan(e.getName(), reason, null, "console");
                DiscordPortal.banPlayer(off, true);
            } else {
                dataManager.setLastDay(actualDay);
            }
        }
    }

    @EventHandler
    public void onAirChange(EntityAirChangeEvent e) {
        if (!(e.getEntity() instanceof Player p) || Main.instance.getDay() < 50) return;
        if (p.getRemainingAir() < e.getAmount()) return;

        int speed = Main.instance.getDay() < 60 ? 5 : 10;
        double damage = Main.instance.getDay() < 60 ? 5.0D : 10.0D;

        if (e.getAmount() < 20) return;
        int remain = (e.getAmount() / 20) / speed;
        int newAmount = remain * 20;

        if (remain <= 0) {
            e.setAmount(0);
            Main.instance.getNmsAccessor().drown(p, damage);
            return;
        }
        e.setAmount(newAmount);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Player p = e.getPlayer();
        long day = Main.getInstance().getDay();

        if (day >= 40 && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            if (displayName.equalsIgnoreCase(TextUtils.format("&6Super Golden Apple +"))) {
                if (!p.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 60 * 5, 0));
                }
            } else if (displayName.equalsIgnoreCase(TextUtils.format("&6Hyper Golden Apple +"))) {
                NamespacedKey keyOne = new NamespacedKey(Main.getInstance(), "hyper_one");
                NamespacedKey keyTwo = new NamespacedKey(Main.getInstance(), "hyper_two");

                if (day < 60) {
                    if (p.getPersistentDataContainer().has(keyOne, PersistentDataType.BYTE)) {
                        p.sendMessage(TextUtils.format(Main.instance.prefix + "&c¡Ya has comido una Hyper Golden Apple!"));
                        return;
                    }
                    p.sendMessage(TextUtils.format(Main.instance.prefix + "&a¡Has obtenido contenedores de vida extra!"));
                    p.getPersistentDataContainer().set(keyOne, PersistentDataType.BYTE, (byte) 1);
                } else {
                    boolean ateOne = p.getPersistentDataContainer().has(keyOne, PersistentDataType.BYTE);
                    boolean ateTwo = p.getPersistentDataContainer().has(keyTwo, PersistentDataType.BYTE);

                    if (!ateOne) {
                        p.sendMessage(TextUtils.format(Main.instance.prefix + "&a¡Has obtenido contenedores de vida extra! &e(Hyper Golden Apple 1/2)"));
                        p.getPersistentDataContainer().set(keyOne, PersistentDataType.BYTE, (byte) 1);
                    } else {
                        if (ateTwo) {
                            p.sendMessage(TextUtils.format(Main.instance.prefix + "&c¡Ya has comido una Hyper Golden Apple #2!"));
                            return;
                        }
                        p.sendMessage(TextUtils.format(Main.instance.prefix + "&a¡Has obtenido contenedores de vida extra! &e(Hyper Golden Apple 2/2)"));
                        p.getPersistentDataContainer().set(keyTwo, PersistentDataType.BYTE, (byte) 1);
                    }
                }
            }
        }

        if (day >= 50) {
            Material mat = item.getType();
            if (mat == Material.MILK_BUCKET) {
                if (p.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                    PotionEffect effect = p.getPotionEffect(PotionEffectType.MINING_FATIGUE);
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> { if (effect != null) p.addPotionEffect(effect); }, 10L);
                }
            } else if (mat == Material.PUMPKIN_PIE) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 5, 0));
            } else if (mat == Material.SPIDER_EYE) {
                Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                    p.removePotionEffect(PotionEffectType.POISON);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 0));
                }, 5L);
            } else if (mat == Material.PUFFERFISH) {
                Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                    p.removePotionEffect(PotionEffectType.NAUSEA);
                    p.removePotionEffect(PotionEffectType.POISON);
                    p.removePotionEffect(PotionEffectType.HUNGER);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 3));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 2));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, Integer.MAX_VALUE, 1));
                }, 5L);
            } else if (mat == Material.ROTTEN_FLESH) {
                Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                    p.removePotionEffect(PotionEffectType.HUNGER);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 1));
                }, 5L);
            } else if (mat == Material.POISONOUS_POTATO) {
                Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                    p.removePotionEffect(PotionEffectType.POISON);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 0));
                }, 5L);
            }
        }

        if (day >= 60 && item.getType() == Material.PUMPKIN_PIE) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 3));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && Main.instance.getDay() >= 60) {
            e.getPlayer().setCooldown(Material.ENDER_PEARL, 120);
        }
    }

    @EventHandler
    public void onWC(PlayerChangedWorldEvent e) {
        if (e.getPlayer().getWorld().getName().equalsIgnoreCase(Main.getInstance().endWorld.getName())) {
            createRegenZone(e.getPlayer().getLocation());
        }
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent e) {
        if (Main.getInstance().getDay() >= 40 && e.getChunk().getWorld().getName().equalsIgnoreCase(Main.getInstance().endWorld.getName())) {
            for (Entity entity : e.getChunk().getEntities()) {
                if (entity instanceof ItemFrame frame && frame.getItem().getType() == Material.ELYTRA) {
                    ItemStack s = new ItemBuilder(Material.ELYTRA).build();
                    if (s.getItemMeta() instanceof Damageable dmg) {
                        dmg.setDamage(431);
                        s.setItemMeta(dmg);
                    }
                    frame.setItem(s);
                }
            }
        }
    }

    private void createRegenZone(Location playerZone) {
        EndDataManager ma = Main.getInstance().getEndData();
        if (!ma.getConfig().getBoolean("CreatedRegenZone")) {
            Location added = playerZone.add(-10, 0, 0);
            Location toGenerate = Main.getInstance().endWorld.getHighestBlockAt(added).getLocation();

            if (toGenerate.getY() == -1) toGenerate.setY(playerZone.getY());

            Block centerBlock = Main.getInstance().endWorld.getBlockAt(toGenerate);
            generateBlocks(true, toGenerate);
            generateBlocks(false, toGenerate);

            centerBlock.getRelative(BlockFace.UP).setType(Material.RED_CARPET);
            centerBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.SEA_LANTERN);
            centerBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.RED_CARPET);

            AreaEffectCloud a = (AreaEffectCloud) Main.getInstance().endWorld.spawnEntity(centerBlock.getRelative(BlockFace.UP).getLocation(), EntityType.AREA_EFFECT_CLOUD);
            a.addCustomEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 5, 0), false);
            a.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 0), false);
            a.setDuration(999999);
            a.setParticle(Particle.BLOCK, Material.AIR.createBlockData());
            a.setRadius(4.0F);

            ma.getConfig().set("CreatedRegenZone", true);
            ma.getConfig().set("RegenZoneLocation", locationToString(a.getLocation()));
            ma.saveFile();
            ma.reloadFile();

            // SÍNCRONO: Modificar bloques de forma asíncrona causaría un crash masivo en Paper.
            Bukkit.getServer().getScheduler().runTaskLater(Main.getInstance(), () -> {
                for (Entity ents : Main.getInstance().endWorld.getEntities()) {
                    if (ents.getType() == EntityType.ENDERMAN || ents.getType() == EntityType.CREEPER) {
                        Block b = ents.getLocation().getBlock().getRelative(BlockFace.DOWN);
                        int structure = new Random().nextInt(5);
                        ArrayList<Block> toChange = new ArrayList<>();

                        switch (structure) {
                            case 0 -> {
                                toChange.add(b.getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST));
                                toChange.add(b.getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.SOUTH_EAST));
                                toChange.add(b.getRelative(BlockFace.SOUTH_WEST));
                                toChange.add(b.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH));
                            }
                            case 1 -> {
                                toChange.add(b.getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.NORTH_EAST));
                                toChange.add(b);
                            }
                            case 2 -> {
                                toChange.add(b.getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.SOUTH_WEST));
                                toChange.add(b);
                            }
                            case 3 -> {
                                toChange.add(b.getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.NORTH_EAST));
                                toChange.add(b);
                                toChange.add(b.getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.EAST));
                            }
                            case 4 -> {
                                toChange.add(b.getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.NORTH_WEST));
                                toChange.add(b);
                                toChange.add(b.getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.WEST));
                            }
                        }

                        for (Block all : toChange) {
                            Location used = Main.getInstance().endWorld.getHighestBlockAt(new Location(Main.getInstance().endWorld, all.getX(), all.getY(), all.getZ())).getLocation();
                            Block now = Main.getInstance().endWorld.getBlockAt(used);
                            if (now.getType() == Material.END_STONE) {
                                now.setType(Material.END_STONE_BRICKS);
                            }
                        }
                    }
                }
            }, 100L);
        }
    }

    private void generateBlocks(boolean style, Location toGenerate) {
        ArrayList<Block> blocks = new ArrayList<>();
        Block centerBlock = Main.getInstance().endWorld.getBlockAt(toGenerate);

        if (style) {
            blocks.add(centerBlock);
            blocks.add(centerBlock.getRelative(BlockFace.EAST));
            blocks.add(centerBlock.getRelative(BlockFace.WEST));
            blocks.add(centerBlock.getRelative(BlockFace.NORTH));
            blocks.add(centerBlock.getRelative(BlockFace.NORTH_WEST));
            blocks.add(centerBlock.getRelative(BlockFace.NORTH_EAST));
            blocks.add(centerBlock.getRelative(BlockFace.SOUTH));
            blocks.add(centerBlock.getRelative(BlockFace.SOUTH_WEST));
            blocks.add(centerBlock.getRelative(BlockFace.SOUTH_EAST));

            for (Block all : blocks) all.setType(Material.RED_WOOL);
        } else {
            Block corner1 = centerBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).getRelative(BlockFace.EAST);
            blocks.add(corner1);
            blocks.add(corner1.getRelative(BlockFace.WEST));
            blocks.add(corner1.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));
            blocks.add(corner1.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));
            blocks.add(corner1.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));
            blocks.add(corner1.getRelative(BlockFace.SOUTH));
            blocks.add(corner1.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH));
            blocks.add(corner1.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH));

            Block southC = corner1.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH);
            blocks.add(southC);
            blocks.add(southC.getRelative(BlockFace.WEST));
            blocks.add(southC.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));
            blocks.add(southC.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));

            Block finalC = southC.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST);
            blocks.add(finalC);
            blocks.add(finalC.getRelative(BlockFace.NORTH));
            blocks.add(finalC.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH));
            blocks.add(finalC.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH));

            for (Block all : blocks) all.setType(Material.RED_GLAZED_TERRACOTTA);
        }
    }

    private String locationToString(Location loc) {
        return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getName();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void restrictCrafting(PrepareItemCraftEvent e) {
        CraftPrepareManager manager = new CraftPrepareManager(e);
        manager.runCheckForLifeOrb();
        manager.runCheckForBeginningRelic();
        manager.runCheckForInfernalPiece();
        manager.runCheckForInfernalElytra();
        manager.runCheckForGaps();

        ItemStack result = e.getInventory().getResult();
        if (result != null && result.getType().name().startsWith("LEATHER_") && !result.getItemMeta().isUnbreakable() && Main.instance.getDay() >= 25) {
            e.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent e) {
ace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.RED_CARPET);

            AreaEffectCloud a = (AreaEffectCloud) Main.getInstance().endWorld.spawnEntity(centerBlock.getRelative(BlockFace.UP).getLocation(), EntityType.AREA_EFFECT_CLOUD);
            a.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 5, 0), false);
            a.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 0), false);
            a.setDuration(999999);
            a.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData());
            a.setRadius(4.0F);

            ma.getConfig().set("CreatedRegenZone", true);
            ma.getConfig().set("RegenZoneLocation", locationToString(a.getLocation()));
            ma.saveFile();
            ma.reloadFile();

            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
                @Override
                public void run() {

                    for (Entity ents : Main.getInstance().endWorld.getEntities()) {

                        if (ents.getType() == EntityType.ENDERMAN || ents.getType() == EntityType.CREEPER) {

                            Block b = ents.getLocation().getBlock().getRelative(BlockFace.DOWN);

                            int structure = new Random().nextInt(4);

                            ArrayList<Block> toChange = new ArrayList<>();

                            if (structure == 0) {

                                toChange.add(b.getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST));
                                toChange.add(b.getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.SOUTH_EAST));
                                toChange.add(b.getRelative(BlockFace.SOUTH_WEST));
                                toChange.add(b.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH));
                            } else if (structure == 1) {

                                toChange.add(b.getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.NORTH_EAST));
                                toChange.add(b);
                            } else if (structure == 2) {

                                toChange.add(b.getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.SOUTH_WEST));
                                toChange.add(b);
                            } else if (structure == 3) {

                                toChange.add(b.getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.NORTH_EAST));
                                toChange.add(b);
                                toChange.add(b.getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.EAST));
                            } else if (structure == 4) {

                                toChange.add(b.getRelative(BlockFace.SOUTH));
                                toChange.add(b.getRelative(BlockFace.NORTH_WEST));
                                toChange.add(b);
                                toChange.add(b.getRelative(BlockFace.NORTH));
                                toChange.add(b.getRelative(BlockFace.WEST));
                            }

                            for (Block all : toChange) {

                                Location used = Main.getInstance().endWorld.getHighestBlockAt(new Location(Main.getInstance().endWorld, all.getX(), all.getY(), all.getZ())).getLocation();

                                Block now = Main.getInstance().endWorld.getBlockAt(used);

                                if (now.getType() == Material.END_STONE) {

                                    now.setType(Material.END_STONE_BRICKS);
                                }
                            }
                        }
                    }
                }
            }, 100L);
        }
    }

    private void generateBlocks(boolean b, Location toGenerate) {

        if (b) {

            ArrayList<Block> blocks = new ArrayList<>();

            Block centerBlock = Main.getInstance().endWorld.getBlockAt(toGenerate);
            blocks.add(centerBlock);

            blocks.add(Main.getInstance().endWorld.getBlockAt(toGenerate).getRelative(BlockFace.EAST));
            blocks.add(Main.getInstance().endWorld.getBlockAt(toGenerate).getRelative(BlockFace.WEST));

            blocks.add(centerBlock.getRelative(BlockFace.NORTH));
            blocks.add(centerBlock.getRelative(BlockFace.NORTH_WEST));
            blocks.add(centerBlock.getRelative(BlockFace.NORTH_EAST));

            blocks.add(centerBlock.getRelative(BlockFace.SOUTH));
            blocks.add(centerBlock.getRelative(BlockFace.SOUTH_WEST));
            blocks.add(centerBlock.getRelative(BlockFace.SOUTH_EAST));

            for (Block all : blocks) {

                all.setType(Material.RED_WOOL);
            }
        } else {

            ArrayList<Block> blocks = new ArrayList<>();
            Block centerBlockOfWool = Main.getInstance().endWorld.getBlockAt(toGenerate);

            Block corner1 = centerBlockOfWool.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).getRelative(BlockFace.EAST);

            blocks.add(corner1);
            blocks.add(corner1.getRelative(BlockFace.WEST));
            blocks.add(corner1.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));
            blocks.add(corner1.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));

            // CORNER 2
            blocks.add(corner1.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));

            blocks.add(corner1.getRelative(BlockFace.SOUTH));
            blocks.add(corner1.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH));
            blocks.add(corner1.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH));

            // CORNER 3
            Block southC = corner1.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH);
            blocks.add(southC);

            blocks.add(southC.getRelative(BlockFace.WEST));
            blocks.add(southC.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));
            blocks.add(southC.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST));

            // CORNER 4
            Block finalC = southC.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST);
            blocks.add(finalC);

            blocks.add(finalC.getRelative(BlockFace.NORTH));
            blocks.add(finalC.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH));
            blocks.add(finalC.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH));

            for (Block all : blocks) {

                all.setType(Material.RED_GLAZED_TERRACOTTA);
            }
        }
    }

    private String locationToString(Location loc) {
        return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getName();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void restrictCrafting(PrepareItemCraftEvent e) {

        CraftPrepareManager manager = new CraftPrepareManager(e);

        manager.runCheckForLifeOrb();
        manager.runCheckForBeginningRelic();
        manager.runCheckForInfernalPiece();
        manager.runCheckForInfernalElytra();
        manager.runCheckForGaps();

        if (e.getInventory().getResult() != null && e.getInventory().getResult().getType().name().toLowerCase().contains("leather_") && !e.getInventory().getResult().getItemMeta().isUnbreakable() && Main.instance.getDay() >= 25) {
            e.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent e) {
        CraftingInventory inventory = e.getInventory();

        if (inventory.getResult() != null) {

            ItemStack res = e.getRecipe().getResult();

            if (e.isCancelled() || e.getResult() != Event.Result.ALLOW) return;

            if (res.hasItemMeta()) {

                if (PermadeathItems.isEndRelic(res)) {

                    ItemMeta meta = res.getItemMeta();
                    meta.setLore(Arrays.asList(HiddenStringUtils.encodeString("{" + UUID.randomUUID().toString() + ": 0}")));
                    res.setItemMeta(meta);

                    e.setCurrentItem(res);
                    return;
                }

                if (res.isSimilar(PermadeathItems.createBeginningRelic()) || res.isSimilar(PermadeathItems.createLifeOrb())) {
                    if (e.getWhoClicked() instanceof Player) {
                        e.getInventory().setMatrix(clearMatrix());
                        Player p = (Player) e.getWhoClicked();
                        p.setItemOnCursor(res);
                    }
                }

                if (res.getItemMeta().hasDisplayName() && res.getItemMeta().getDisplayName().contains(TextUtils.format("&6Hyper Golden Apple +")) || res.getItemMeta().getDisplayName().contains(TextUtils.format("&6Super Golden Apple +"))) {
                    if (e.getWhoClicked() instanceof Player) {

                        e.getInventory().setMatrix(clearMatrix());

                        Player p = (Player) e.getWhoClicked();

                        p.setItemOnCursor(res);

                    }
                }
            }
        }
    }

    public ItemStack[] clearMatrix() {

        return new ItemStack[]{
                new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)};
    }

    private class CraftPrepareManager {

        private PrepareItemCraftEvent e;
        private ItemStack result;

        public CraftPrepareManager(PrepareItemCraftEvent e) {
            this.e = e;
            this.result = e.getInventory().getResult();
        }

        public void runCheckForBeginningRelic() {

            if (result == null) return;

            if (result.isSimilar(PermadeathItems.createBeginningRelic())) {
                int diamondBlocks = 0;
                int r = 0;
                for (ItemStack s : e.getInventory().getMatrix()) {
                    if (s != null) {
                        if (s.getType() == Material.DIAMOND_BLOCK) {
                            if (s.getAmount() >= 32) {
                                diamondBlocks++;
                            }
                        }
                        if (PermadeathItems.isEndRelic(s)) {
                            r++;
                        }
                    }
                }

                if (diamondBlocks < 4 || r < 1) {
                    e.getInventory().setResult(null);
                }

                if (diamondBlocks >= 4 && r >= 1) {
                    e.getInventory().setResult(PermadeathItems.createBeginningRelic());
                }
            }
        }

        public void runCheckForInfernalPiece() {
            if (result == null) return;
            if (NetheriteArmor.isInfernalPiece(result)) {
                if (result.getType() == Material.ELYTRA) return;
                int diamondsFound = 0;
                boolean foundPiece = false;

                for (ItemStack item : e.getInventory().getMatrix()) {
                    if (item != null) {
                        if (item.hasItemMeta()) {
                            ItemMeta meta = item.getItemMeta();
                            if (item.getType() == Material.DIAMOND) {
                                if (meta.isUnbreakable() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains("Infernal")) {
                                    diamondsFound = diamondsFound + 1;
                                }
                            }
                            if (NetheriteArmor.isNetheritePiece(item)) {
                                foundPiece = true;
                            }
                        }
                    }
                }

                if (diamondsFound < 5 || !foundPiece) {
                    e.getInventory().setResult(null);
                }

                if (diamondsFound >= 4 && foundPiece) {

                    Material mat = result.getType();

                    if (mat == Material.LEATHER_HELMET) {
                        e.getInventory().setResult(InfernalNetherite.craftNetheriteHelmet());
                    }

                    if (mat == Material.LEATHER_CHESTPLATE) {
                        e.getInventory().setResult(InfernalNetherite.craftNetheriteChest());
                    }

                    if (mat == Material.LEATHER_LEGGINGS) {
                        e.getInventory().setResult(InfernalNetherite.craftNetheriteLegs());
                    }

                    if (mat == Material.LEATHER_BOOTS) {
                        e.getInventory().setResult(InfernalNetherite.craftNetheriteBoots());
                    }
                }
            }
        }

        public void runCheckForInfernalElytra() {
            if (result == null) return;
            if (result.getType() == Material.ELYTRA) {

                int diamondsFound = 0;

                for (ItemStack item : e.getInventory().getMatrix()) {
                    if (item != null) {
                        if (item.hasItemMeta()) {
                            ItemMeta meta = item.getItemMeta();
                            if (item.getType() == Material.DIAMOND) {
                                if (meta.isUnbreakable() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains("Infernal")) {
                                    diamondsFound = diamondsFound + 1;
                                }
                            }
                        }
                    }
                }

                if (diamondsFound >= 8) {
                    e.getInventory().setResult(PermadeathItems.craftInfernalElytra());
                } else {
                    e.getInventory().setResult(null);
                }
            }
        }

        public void runCheckForGaps() {
            if (result == null) return;
            if (result.getItemMeta().getDisplayName().startsWith(TextUtils.format("&6Hyper"))) {

                if (Main.instance.getDay() < 60) {
                    int found = 0;

                    for (ItemStack item : e.getInventory().getMatrix()) {
                        if (item != null) {
                            if (item.getType() == Material.GOLD_BLOCK) {
                                if (item.getAmount() >= 8) {
                                    found = found + 1;
                                }
                            }
                        }
                    }

                    if (found >= 8) {
                        e.getInventory().setResult(new ItemBuilder(Material.GOLDEN_APPLE, 1).setDisplayName(TextUtils.format("&6Hyper Golden Apple +")).addEnchant(Enchantment.ARROW_INFINITE, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).build());
                    } else {

                        e.getInventory().setResult(null);
                    }
                } else {
                    int found = 0;
                    boolean enoughGaps = false;

                    for (ItemStack item : e.getInventory().getMatrix()) {
                        if (item != null) {
                            if (item.getType() == Material.GOLD_BLOCK) {
                                if (item.getAmount() >= 8) {
                                    found = found + 1;
                                }
                            }

                            if (item.getType() == Material.GOLDEN_APPLE && item.getAmount() == 64) {
                                enoughGaps = true;
                            }
                        }
                    }

                    if (found >= 8 && enoughGaps) {
                        e.getInventory().setResult(new ItemBuilder(Material.GOLDEN_APPLE, 1).setDisplayName(TextUtils.format("&6Hyper Golden Apple +")).addEnchant(Enchantment.ARROW_INFINITE, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).build());
                    } else {

                        e.getInventory().setResult(null);
                    }
                }
            }

            if (result.getItemMeta().getDisplayName().startsWith(TextUtils.format("&6Super"))) {

                int found = 0;
                for (ItemStack item : e.getInventory().getMatrix()) {
                    if (item != null) {
                        if (item.getType() == Material.GOLD_INGOT) {
                            if (item.getAmount() >= 8) {
                                found++;
                            }
                        }
                    }
                }
                if (found < 8) {
                    e.getInventory().setResult(null);
                    return;
                }
                if (found >= 8) {
                    e.getInventory().setResult(new ItemBuilder(Material.GOLDEN_APPLE, 1).setDisplayName(TextUtils.format("&6Super Golden Apple +")).addEnchant(Enchantment.ARROW_INFINITE, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).build());
                }
            }
        }

        public void runCheckForLifeOrb() {
            if (result == null) return;
            if (!result.isSimilar(PermadeathItems.createLifeOrb())) return;
            if (!Main.instance.getOrbEvent().isRunning()) return;
            int items = 0;

            for (ItemStack s : e.getInventory().getMatrix()) {
                if (s != null) {
                    if (s.getType() == Material.HEART_OF_THE_SEA) {
                        items++;
                    } else {
                        if (s.getAmount() >= 64) {
                            items++;
                        }
                    }
                }
            }
            if (items < 9) {
                e.getInventory().setResult(null);
            }
            if (items >= 9) {
                e.getInventory().setResult(PermadeathItems.createLifeOrb());
            }
        }
    }
}
