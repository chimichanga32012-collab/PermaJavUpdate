package tech.sebazcrc.permadeath.task;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.TextUtils;
import tech.sebazcrc.permadeath.end.demon.DemonCurrentAttack;
import tech.sebazcrc.permadeath.end.demon.DemonPhase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;

public class EndTask extends BukkitRunnable {

    private final Map<Location, Integer> regenTime = new HashMap<>();
    private final Location teleportLocation;
    private DemonCurrentAttack currentAttack = DemonCurrentAttack.NONE;
    private DemonPhase currentDemonPhase = DemonPhase.NORMAL;
    private MovesTask currentMovesTask = null;

    private final EnderDragon enderDragon;
    private final Main main;

    private int timeForTnT = 30;
    private int nextDragonAttack = 20;
    private int lightingDuration = 5;
    private int nightVisionDuration = 5;
    private int timeForEnd360 = 20;

    private boolean nightVision = false;
    private boolean isDied;
    private boolean attack360 = false;
    private boolean lightingRain = false;
    private boolean canMakeAnAttack = true;
    private boolean decided = false;

    private final Location eggLocation;
    private final SplittableRandom random = new SplittableRandom();

    public EndTask(Main plugin, EnderDragon enderDragon) {
        this.main = plugin;
        this.isDied = false;
        this.enderDragon = enderDragon;

        int y = main.endWorld.getMaxHeight() - 1;
        while (y > main.endWorld.getMinHeight() && main.endWorld.getBlockAt(0, y, 0).getType() != Material.BEDROCK) {
            y--;
        }
        this.eggLocation = main.endWorld.getHighestBlockAt(new Location(main.endWorld, 0, y, 0)).getLocation();

        var maxHealthAttr = enderDragon.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double customHealth = main.getConfig().getInt("Toggles.End.PermadeathDemon.Health", 200);
            maxHealthAttr.setBaseValue(customHealth);
            enderDragon.setHealth(customHealth);
        }

        teleportLocation = eggLocation.clone().add(0, 2, 0);
        teleportLocation.setPitch(enderDragon.getLocation().getPitch());

        for (Ghast ghast : enderDragon.getWorld().getEntitiesByClass(Ghast.class)) {
            ghast.remove();
        }
    }

    @Override
    public void run() {
        if (isDied || enderDragon.isDead()) {
            main.setTask(null);
            cancel();
            return;
        }
        tickTnTAttack();
        tickLightingRain();
        tickNightVision();
        tick360Attack();
        tickDemonPhase();
        tickRandomLighting();
        tickEnderCrystals();
        tickDragonAttacks();
    }

    private void tickEnderCrystals() {
        if (!regenTime.isEmpty()) {
            regenTime.entrySet().removeIf(entry -> {
                Location loc = entry.getKey();
                int time = entry.getValue();
                if (time >= 1) {
                    entry.setValue(time - 1);
                    return false;
                } else {
                    loc.getWorld().spawnEntity(loc, EntityType.ENDER_CRYSTAL);
                    var block = loc.getBlock();
                    if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                    return true;
                }
            });
        }
    }

    private void tickRandomLighting() {
        int x = (random.nextBoolean() ? 1 : -1) * random.nextInt(21);
        int z = (random.nextBoolean() ? 1 : -1) * random.nextInt(21);
        int y = main.endWorld.getHighestBlockYAt(x, z);

        if (y < main.endWorld.getMinHeight()) return;

        main.endWorld.strikeLightning(new Location(main.endWorld, x, y, z));
    }

    private void tickDemonPhase() {
        if (currentDemonPhase == DemonPhase.ENRAGED) {
            enderDragon.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 7, false, false));
            enderDragon.setCustomName(TextUtils.format(main.getConfig().getString("Toggles.End.PermadeathDemon.DisplayNameEnraged")));
        } else {
            enderDragon.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 5, false, false));
        }
    }

    private void tick360Attack() {
        double distance = enderDragon.getLocation().distance(eggLocation);
        if (distance >= 10.0D && decided) {
            decided = false;
        }
        if (distance <= 3.0D && !decided) {
            decided = true;
            enderDragon.setRotation(enderDragon.getLocation().getPitch(), 0);

            if (random.nextInt(10) <= 7) {
                start360attack();
            }
        }

        if (attack360) {
            canMakeAnAttack = false;
            if (timeForEnd360 >= 1) {
                timeForEnd360--;
            }
            if (timeForEnd360 >= 16) {
                if (enderDragon.getPhase() != EnderDragon.Phase.LAND_ON_PORTAL) {
                    enderDragon.setPhase(EnderDragon.Phase.LAND_ON_PORTAL);
                }
                enderDragon.teleport(teleportLocation);
            }

            if (timeForEnd360 == 15) {
                this.currentMovesTask = new MovesTask(main, enderDragon, teleportLocation);
                currentMovesTask.runTaskTimer(main, 5L, 5L);
            }

            if (timeForEnd360 == 0) {
                if (currentMovesTask != null) {
                    currentMovesTask.cancel();
                    currentMovesTask = null;
                }

                canMakeAnAttack = true;
                timeForEnd360 = 20;
                attack360 = false;
                enderDragon.setPhase(EnderDragon.Phase.LEAVE_PORTAL);
            }
        }
    }

    private void tickDragonAttacks() {
        if (nextDragonAttack >= 1) {
            nextDragonAttack--;
        } else if (nextDragonAttack == 0) {
            nextDragonAttack = (getCurrentDemonPhase() == DemonPhase.NORMAL) ? 60 : 40;

            if (canMakeAnAttack) {
                chooseAnAttack();
            } else {
                currentAttack = DemonCurrentAttack.NONE;
            }
            
            if (currentAttack == DemonCurrentAttack.NONE) {
                return;
            }
            
            if (currentAttack == DemonCurrentAttack.ENDERMAN_BUFF) {
                int endermenChosen = 0;
                ArrayList<Enderman> endermen = new ArrayList<>();

                for (Enderman man : main.endWorld.getEntitiesByClass(Enderman.class)) {
                    Location backUp = man.getLocation();
                    backUp.setY(0);

                    if (eggLocation.distance(backUp) <= 35) {
                        if (endermenChosen < 4) {
                            endermenChosen++;
                            endermen.add(man);
                        }
                    }
                }
                
                for (Enderman mans : endermen) {
                    AreaEffectCloud a = (AreaEffectCloud) main.endWorld.spawnEntity(main.endWorld.getHighestBlockAt(mans.getLocation()).getLocation().add(0, 1, 0), EntityType.AREA_EFFECT_CLOUD);
                    a.setRadius(10.0F);
                    a.setParticle(Particle.HAPPY_VILLAGER);
                    a.setColor(Color.GREEN);
                    a.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 999999, 0), false);

                    mans.setInvulnerable(true);
                }
            } else if (currentAttack == DemonCurrentAttack.LIGHTING_RAIN) {
                lightingRain = true;
                lightingDuration = 5;
            } else if (currentAttack == DemonCurrentAttack.NIGHT_VISION) {
                nightVision = true;
                nightVisionDuration = 5;
                for (Player all : main.endWorld.getPlayers()) {
                    all.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 7, 0));
                }
            }
        }
    }

    private void tickTnTAttack() {
        timeForTnT--;

        if (timeForTnT == 0) {
            if (enderDragon.getPhase() != EnderDragon.Phase.DYING && !attack360 && enderDragon.getLocation().distance(eggLocation) >= 15) {
                int[][] offsets = {{3, -3}, {3, 3}, {3, 0}, {-3, 3}, {-3, -3}, {-3, 0}};
                
                for (int[] offset : offsets) {
                    TNTPrimed tnt = (TNTPrimed) enderDragon.getWorld().spawnEntity(enderDragon.getLocation().add(offset[0], 0, offset[1]), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(60);
                    tnt.setYield(tnt.getYield() * 2);
                    tnt.setCustomName("dragontnt");
                    tnt.setCustomNameVisible(false);
                }
            }
            timeForTnT = 30 + (random.nextInt(61));
        }
    }

    private void tickLightingRain() {
        if (lightingRain) {
            if (lightingDuration >= 1) {
                canMakeAnAttack = false;
                lightingDuration--;

                for (Player all : main.endWorld.getPlayers()) {
                    main.endWorld.strikeLightning(all.getLocation());

                    if (currentDemonPhase == DemonPhase.ENRAGED) {
                        all.damage(1.0D);
                    }
                }
            } else {
                lightingRain = false;
                lightingDuration = 5;
                canMakeAnAttack = true;
            }
        }
    }

    private void tickNightVision() {
        if (nightVision) {
            if (nightVisionDuration >= 1) {
                nightVisionDuration--;
            } else {
                for (Player all : main.endWorld.getPlayers()) {
                    Location highest = main.endWorld.getHighestBlockAt(all.getLocation()).getLocation();
                    if (currentDemonPhase == DemonPhase.NORMAL) {
                        highest.add(0, 1, 0);
                    }

                    AreaEffectCloud eff = (AreaEffectCloud) main.endWorld.spawnEntity(highest, EntityType.AREA_EFFECT_CLOUD);
                    eff.setParticle(Particle.DAMAGE_INDICATOR);
                    eff.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 20 * 5, 1), false);
                    eff.setRadius(3.0F);
                }

                nightVision = false;
                canMakeAnAttack = true;
            }
        }
    }

    public void chooseAnAttack() {
        int ran = random.nextInt(25);
        if (ran <= 3) {
            currentAttack = DemonCurrentAttack.LIGHTING_RAIN;
        } else if (ran <= 15) {
            currentAttack = DemonCurrentAttack.ENDERMAN_BUFF;
        } else {
            currentAttack = DemonCurrentAttack.NIGHT_VISION;
        }
    }

    public Map<Location, Integer> getRegenTime() {
        return regenTime;
    }

    public void setDied(boolean died) {
        isDied = died;
    }

    public Entity getEnderDragon() {
        return enderDragon;
    }

    public boolean isDied() {
        return isDied;
    }

    public Main getMain() {
        return main;
    }

    public void start360attack() {
        this.attack360 = true;
    }

    public DemonPhase getCurrentDemonPhase() {
        return currentDemonPhase;
    }

    public void setCurrentDemonPhase(DemonPhase currentDemonPhase) {
        this.currentDemonPhase = currentDemonPhase;
    }
}
on());

                    if (currentDemonPhase == DemonPhase.ENRAGED) {

                        all.damage(1.0D);
                    }
                }
            } else {
                lightingRain = false;
                lightingDuration = 5;
                canMakeAnAttack = true;
            }
        }
    }

    private void tickNightVision() {
        if (nightVision) {
            if (nightVisionDuration >= 1) {
                nightVisionDuration--;
            } else {
                for (Player all : main.endWorld.getPlayers()) {
                    if (currentDemonPhase == DemonPhase.NORMAL) {

                        Location highest = main.endWorld.getHighestBlockAt(all.getLocation()).getLocation().add(0, 1, 0);

                        AreaEffectCloud eff = (AreaEffectCloud) main.endWorld.spawnEntity(highest, EntityType.AREA_EFFECT_CLOUD);

                        eff.setParticle(Particle.DAMAGE_INDICATOR);
                        eff.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 20 * 5, 1), false);
                        eff.setRadius(3.0F);
                    } else {

                        Location highest = main.endWorld.getHighestBlockAt(all.getLocation()).getLocation();

                        AreaEffectCloud eff = (AreaEffectCloud) main.endWorld.spawnEntity(highest, EntityType.AREA_EFFECT_CLOUD);

                        eff.setParticle(Particle.DAMAGE_INDICATOR);
                        eff.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 20 * 5, 1), false);
                        eff.setRadius(3.0F);
                    }
                }

                nightVision = false;
                canMakeAnAttack = true;
            }
        }
    }

    public void chooseAnAttack() {
        int ran = random.nextInt(25);
        if (ran <= 3) {
            currentAttack = DemonCurrentAttack.LIGHTING_RAIN;
        } else if (ran >= 4 && ran <= 15) {
            currentAttack = DemonCurrentAttack.ENDERMAN_BUFF;
        } else if (ran >= 15 && ran <= 25) {
            currentAttack = DemonCurrentAttack.NIGHT_VISION;
        }
    }


    public Map<Location, Integer> getRegenTime() {
        return regenTime;
    }

    public void setDied(boolean died) {
        isDied = died;
    }

    public Entity getEnderDragon() {
        return enderDragon;
    }

    public boolean isDied() {
        return isDied;
    }

    public Main getMain() {
        return main;
    }

    public void start360attack() {

        this.attack360 = true;
    }

    public DemonPhase getCurrentDemonPhase() {
        return currentDemonPhase;
    }

    public void setCurrentDemonPhase(DemonPhase currentDemonPhase) {
        this.currentDemonPhase = currentDemonPhase;
    }
}
