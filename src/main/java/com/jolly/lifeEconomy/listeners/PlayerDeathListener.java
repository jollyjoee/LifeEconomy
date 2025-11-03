package com.jolly.lifeEconomy.listeners;

import com.jolly.lifeEconomy.LifeEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private final LifeEconomy plugin;

    public PlayerDeathListener(LifeEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = victim.getKiller();
        UUID vUUID = victim.getUniqueId();
        double min = plugin.getConfig().getDouble("settings.minimum-health", 2.0);
        double randomLoss = plugin.getConfig().getDouble("cost.random-death", 2.0);
        double pvpLoss = plugin.getConfig().getDouble("cost.death-to-player", 2.0);
        boolean healOnKill = plugin.getConfig().getBoolean("settings.heal-on-kill");
        double victimHearts = plugin.heartCache.getOrDefault(vUUID, 20.0);
        if (killer == null) {
            double newVictimHearts = Math.max(min, victimHearts - randomLoss);
            plugin.heartCache.put(vUUID, newVictimHearts);
            applyHealth(victim, newVictimHearts);
            plugin.updateDb(vUUID, newVictimHearts);
            return;
        }
        UUID kUUID = killer.getUniqueId();
        double killerHearts = plugin.heartCache.getOrDefault(kUUID, 20.0);
        if (victimHearts <= min) {
            sendActionBar(victim, "messages.no-hearts-lost", "<red>You didn't lose any hearts.");
            sendActionBar(killer, "messages.no-hearts-gained", "<red>You didn't gain any hearts.");
            return;
        }
        double newVictim = Math.max(min, victimHearts - pvpLoss);
        double newKiller = killerHearts + pvpLoss;
        plugin.heartCache.put(vUUID, newVictim);
        plugin.heartCache.put(kUUID, newKiller);
        applyHealth(victim, newVictim);
        applyHealth(killer, newKiller);
        if (healOnKill) {
            double finalNewKiller = newKiller;
            plugin.scheduler.runGlobal(() ->
                    killer.setHealth(Math.min(finalNewKiller, killer.getMaxHealth()))
            );
        }
        plugin.updateDb(vUUID, newVictim);
        plugin.updateDb(kUUID, newKiller);
    }

    private void applyHealth(Player p, double value) {
        plugin.scheduler.runGlobal(() -> p.setMaxHealth(value));
    }

    private void sendActionBar(Player p, String path, String def) {
        p.sendActionBar(
                plugin.mm.deserialize(plugin.getConfig().getString(path, def))
        );
    }
}
