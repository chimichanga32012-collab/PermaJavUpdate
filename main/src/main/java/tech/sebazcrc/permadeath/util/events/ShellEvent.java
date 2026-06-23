package tech.sebazcrc.permadeath.util.events;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.TextUtils;

public class ShellEvent {

    private final Main instance;
    private boolean running;

    private int timeLeft;
    private final BossBar bossBar;
    private String title;

    public ShellEvent(Main instance) {
        this.instance = instance;
        this.timeLeft = 60 * 60 * 4;
        this.title = TextUtils.format("&c&lX2 Shulker Shells: &b&n");
        this.bossBar = Bukkit.createBossBar(title, BarColor.RED, BarStyle.SOLID);
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void addPlayer(Player p) {
        if (p != null) {
            bossBar.addPlayer(p);
        }
    }

    public void clearPlayers() {
        bossBar.removeAll();
    }

    public void setTitle(String title) {
        this.title = title;
        this.bossBar.setTitle(title);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public void reduceTime() {
        this.timeLeft--;
    }

    public void removePlayer(Player player) {
        if (player != null && bossBar.getPlayers().contains(player)) {
            bossBar.removePlayer(player);
        }
    }
}

}
