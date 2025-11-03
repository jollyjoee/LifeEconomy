package com.jolly.lifeEconomy.display;

import com.jolly.lifeEconomy.LifeEconomy;
import com.jolly.lifeEconomy.Scheduler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import com.jolly.lifeEconomy.CancelTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActionBar {
    private CancelTask scheduledTask;
    private final LifeEconomy plugin;
    private final Scheduler scheduler;
    private final MiniMessage mm = MiniMessage.miniMessage();
    public final List<UUID> tasks = new ArrayList<>();
    public ActionBar(LifeEconomy plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    public void start(Player p) {
        //if (tasks.contains(p.getUniqueId())) return;
        scheduledTask = scheduler.runTimer(() -> {
            double hearts = plugin.heartCache.get(p.getUniqueId()) / 2;
            String msg = "<red>❤ " + hearts + " hearts";
            p.sendActionBar(mm.deserialize(msg));
        }, 1L, 20L);
        tasks.add(p.getUniqueId());
    }

    public void stop(Player p) {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
            tasks.remove(p.getUniqueId());
        }
    }
}

