package com.jolly.lifeEconomy.listeners;

import com.jolly.lifeEconomy.LifeEconomy;
import com.jolly.lifeEconomy.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public class PlayerRespawnListener implements Listener {

    private final LifeEconomy plugin;
    public PlayerRespawnListener(LifeEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Double cached = plugin.heartCache.get(uuid);
        if (cached != null) {
            plugin.scheduler.runLater(() -> player.setMaxHealth(cached), 5L);
            return;
        }
        plugin.db.querySafeAsync(
                "SELECT hearts FROM life_data WHERE uuid = ?",
                rs -> {
                    double hearts = 20.0;
                    if (rs.next()) {
                        hearts = rs.getDouble("hearts");
                    } else plugin.updateDb(uuid, hearts);
                    final double finalHearts = hearts;
                    plugin.scheduler.runGlobal(() -> player.setMaxHealth(finalHearts));
                    return null;
                },
                uuid.toString()
        );
    }
}
