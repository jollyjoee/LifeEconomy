package com.jolly.lifeEconomy.listeners;

import com.jolly.lifeEconomy.LifeEconomy;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {
    private static LifeEconomy plugin;
    public PlayerLeaveListener(LifeEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        double hearts = plugin.heartCache.remove(player.getUniqueId());
        plugin.getLogger().info("[LifeEconomy] Saving hearts: " + hearts + " for " + player.getName());
        plugin.updateDb(player.getUniqueId(), hearts);
    }
}
