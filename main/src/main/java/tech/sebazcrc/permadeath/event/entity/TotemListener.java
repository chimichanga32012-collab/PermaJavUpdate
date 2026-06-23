package tech.sebazcrc.permadeath.event.entity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.TextUtils;

import java.util.Objects;

public class TotemListener implements Listener {

    private final Main plugin;

    public TotemListener(Main instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void totemNerf(EntityResurrectEvent event) {

        if (!(event.getEntity() instanceof Player p)) return;

        ItemStack mainHand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();

        if (mainHand.getType() == Material.TOTEM_OF_UNDYING || offHand.getType() == Material.TOTEM_OF_UNDYING) {

            if (!plugin.getConfig().getBoolean("TotemFail.Enable")) return;

            String player = p.getName();
            int failProb = 0;
            boolean containsDay = false;

            int currentDay = plugin.getDay();

            if (plugin.getConfig().contains("TotemFail.FailProbs." + currentDay)) {
                failProb = plugin.getConfig().getInt("TotemFail.FailProbs." + currentDay);
                containsDay = true;
            } else {
                System.out.println("[INFO] La probabilidad del tótem se encuentra desactivada para el día: " + currentDay);
            }

            if (plugin.getConfig().getConfigurationSection("TotemFail.FailProbs") != null) {
                for (String k : Objects.requireNonNull(plugin.getConfig().getConfigurationSection("TotemFail.FailProbs")).getKeys(false)) {
                    try {
                        int i = Integer.parseInt(k);
                        if (i == currentDay) {
                            containsDay = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Ha ocurrido un error al cargar la probabilidad de tótem del día '" + k + "'");
                    }
                }
            }

            if (!containsDay) return;

            String totemFail = Objects.requireNonNull(plugin.getConfig().getString("TotemFail.ChatMessage"));
            String totemMessage = Objects.requireNonNull(plugin.getConfig().getString("TotemFail.PlayerUsedTotemMessage"));

            if (currentDay >= 40) {
                if (currentDay < 60) {
                    totemMessage = Objects.requireNonNull(plugin.getConfig().getString("TotemFail.PlayerUsedTotemsMessage"))
                            .replace("{ammount}", "dos").replace("%player%", player);
                } else {
                    totemMessage = Objects.requireNonNull(plugin.getConfig().getString("TotemFail.PlayerUsedTotemsMessage"))
                            .replace("{ammount}", "tres").replace("%player%", player);
                }
            }

            if (failProb >= 101) failProb = 100;
            if (failProb < 0) failProb = 1;

            if (failProb == 100) {
                Bukkit.broadcastMessage(TextUtils.format(totemMessage.replace("%player%", player).replace("%porcent%", "=").replace("%totem_fail%", String.valueOf(100)).replace("%number%", String.valueOf(failProb))));
                Bukkit.broadcastMessage(TextUtils.format(totemFail.replace("%player%", player)));
                event.setCancelled(true);
            } else {

                int random = (int) (Math.random() * 100) + 1;
                int resta = 100 - failProb;
                int toShow = resta;

                if (resta == random) toShow = toShow - 1;

                int raShow = random;
                if (random == resta) raShow = raShow - 1;

                if (currentDay < 40) {
                    if (doPlayerHaveSpecialTotem(p)) {
                        ItemStack s = getTotem(p);
                        p.getInventory().removeItem(s);
                        Bukkit.broadcastMessage(TextUtils.format(Objects.requireNonNull(plugin.getConfig().getString("TotemFail.Medalla")).replace("%player%", p.getName())));
                        return;
                    }

                    if (random > resta) {
                        Bukkit.broadcastMessage(TextUtils.format(totemMessage.replace("%player%", player).replace("%porcent%", "=").replace("%totem_fail%", String.valueOf(toShow)).replace("%number%", String.valueOf(resta))));
                        Bukkit.broadcastMessage(TextUtils.format(totemFail.replace("%player%", player)));
                        event.setCancelled(true);
                    } else {
                        Bukkit.broadcastMessage(TextUtils.format(totemMessage.replace("%player%", player).replace("%porcent%", "!=").replace("%totem_fail%", String.valueOf(raShow)).replace("%number%", String.valueOf(resta))));
                    }
                } else {
                    int neededTotems = (currentDay < 60 ? 2 : 3);
                    int totems = p.getInventory().all(Material.TOTEM_OF_UNDYING).size();

                    if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
                        totems++;
                    }

                    int removedTotems = 0;
                    boolean hasTotem = doPlayerHaveSpecialTotem(p);

                    if (hasTotem) {
                        ItemStack s = getTotem(p);
                        if (getSpecialTotem(p) == EnumPlayerTotemSlot.OFF_HAND) {
                            p.getInventory().setItemInOffHand(null);
                        } else {
                            p.getInventory().removeItem(s);
                        }
                        removedTotems++;
                    } else {
                        if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
                            p.getInventory().setItemInOffHand(null);
                            removedTotems++;
                        }
                    }

                    for (ItemStack s : p.getInventory().getContents()) {
                        if (s != null && s.getType() == Material.TOTEM_OF_UNDYING) {
                            if (removedTotems < neededTotems) {
                                p.getInventory().removeItem(s);
                                removedTotems++;
                            }
                        }
                    }

                    if (totems < neededTotems) {
                        Bukkit.broadcastMessage(TextUtils.format(Objects.requireNonNull(plugin.getConfig().getString("TotemFail.NotEnoughTotems")).replace("%player%", player).replace("%porcent%", "=").replace("%totem_fail%", String.valueOf(toShow)).replace("%number%", String.valueOf(resta))));
                        event.setCancelled(true);
                        return;
                    }

                    if (hasTotem) {
                        Bukkit.broadcastMessage(TextUtils.format(Objects.requireNonNull(plugin.getConfig().getString("TotemFail.Medalla")).replace("%player%", p.getName())));
                        return;
                    }

                    if (random > resta) {
                        Bukkit.broadcastMessage(TextUtils.format(totemMessage.replace("%player%", player).replace("%porcent%", "=").replace("%totem_fail%", String.valueOf(toShow)).replace("%number%", String.valueOf(resta))));
                        Bukkit.broadcastMessage(TextUtils.format(Objects.requireNonNull(plugin.getConfig().getString("TotemFail.ChatMessageTotems")).replace("%player%", player)));
                        event.setCancelled(true);
                    } else {
                        Bukkit.broadcastMessage(TextUtils.format(totemMessage.replace("%player%", player).replace("%porcent%", "!=").replace("%totem_fail%", String.valueOf(raShow)).replace("%number%", String.valueOf(resta))));
                    }
                }
            }
        }
    }

    private ItemStack getTotem(Player p) {
        return getSpecialTotem(p) == EnumPlayerTotemSlot.MAIN_HAND ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInOffHand();
    }

    private EnumPlayerTotemSlot getSpecialTotem(Player p) {
        if (isSpecial(p.getInventory().getItemInMainHand())) {
            return EnumPlayerTotemSlot.MAIN_HAND;
        } else if (isSpecial(p.getInventory().getItemInOffHand())) {
            return EnumPlayerTotemSlot.OFF_HAND;
        }
        return null;
    }

    private boolean doPlayerHaveSpecialTotem(Player p) {
        return isSpecial(p.getInventory().getItemInMainHand()) || isSpecial(p.getInventory().getItemInOffHand());
    }

    private boolean isSpecial(ItemStack item) {
        return item != null && item.getType() == Material.TOTEM_OF_UNDYING && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).isUnbreakable();
    }

    public enum EnumPlayerTotemSlot {
        MAIN_HAND, OFF_HAND
    }
}
    }

        if (p.getInventory().getItemInOffHand() != null) {
            if (p.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING && p.getInventory().getItemInOffHand().getItemMeta().isUnbreakable()) {
                tieneMedalla = true;
            }
        }

        return tieneMedalla;
    }

    private boolean isSpecial(ItemStack off) {
        return off != null && off.getType() == Material.TOTEM_OF_UNDYING && off.getItemMeta().isUnbreakable();
    }

    public enum EnumPlayerTotemSlot {
        MAIN_HAND, OFF_HAND
    }
}
