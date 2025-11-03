package com.jolly.lifeEconomy.listeners;

import com.jolly.lifeEconomy.LifeEconomy;
import com.jolly.lifeEconomy.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final LifeEconomy plugin;

    public PlayerJoinListener(LifeEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Double cached = plugin.heartCache.get(uuid);
        if (cached != null) {
            plugin.scheduler.runGlobal(() -> player.setMaxHealth(cached));
            return;
        }
        plugin.db.querySafeAsync(
                "SELECT health FROM life_data WHERE uuid = ?",
                rs -> {
                    double hearts = 20.0;
                    if (rs.next()) {
                        hearts = rs.getDouble("health");
                    } else { //if player doesn't exist in DB
                        plugin.updateDb(uuid, hearts);
                    }
                    double finalHearts = hearts;
                    plugin.heartCache.put(uuid, finalHearts);
                    plugin.scheduler.runGlobal(() -> player.setMaxHealth(finalHearts));
                    return null;
                },
                uuid.toString()
        );
    }
}
