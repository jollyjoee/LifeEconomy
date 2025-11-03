package com.jolly.lifeEconomy.listeners;

import com.jolly.lifeEconomy.LifeEconomy;
import com.jolly.lifeEconomy.display.ActionBar;
import com.jolly.lifeEconomy.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {
    private final Scheduler scheduler;
    private final ActionBar bar;
    private LifeEconomy plugin;
    public PlayerDamageListener(LifeEconomy plugin, Scheduler scheduler, ActionBar bar) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.bar = bar;
    }
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        String displayType = plugin.getConfig().getString("settings.hearts-display", "ACTION_BAR");

        if (displayType.equalsIgnoreCase("none")) return;

        if (displayType.equalsIgnoreCase("ACTION_BAR")) {
            if (bar.tasks.contains(p.getUniqueId())) return;
            bar.start(p);
            scheduler.runLater(() -> bar.stop(p), plugin.getConfig().getInt("settings.display-timeout", 4) * 20L);
        }
    }
}
