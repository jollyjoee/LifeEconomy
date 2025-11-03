package com.jolly.lifeEconomy.display;

import com.jolly.lifeEconomy.LifeEconomy;
import com.jolly.lifeEconomy.Scheduler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionBar {

    private final LifeEconomy plugin;
    private final Scheduler scheduler;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ActionBar(LifeEconomy plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    public void start() {
        scheduler.runTimer(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                double hearts = plugin.heartCache.get(p.getUniqueId()) / 2;
                String msg = "<red>❤ " + hearts + " hearts";
                p.sendActionBar(mm.deserialize(msg));
            }
        }, 1L, 20L);
    }
}
