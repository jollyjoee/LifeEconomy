package com.jolly.lifeEconomy;

import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Public API for interacting with the LifeEconomy system.
 * <p>
 * Health values follow vanilla Minecraft rules:
 * <ul>
 *     <li>1 heart = 2 health</li>
 *     <li>Default player health = 20 (10 hearts)</li>
 *     <li>Minimum allowed health = 2 (1 heart)</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>{@code
 * LifeEconomyAPI api = LifeEconomyAPI.get();
 * api.giveHealth(player, 2.0, true); // +1 heart & heal instantly
 * }</pre>
 */
public class LifeEconomyAPI {

    private final LifeEconomy plugin;
    private final Scheduler scheduler;
    private static LifeEconomyAPI instance;

    /**
     * Creates a new API instance and stores a global reference.
     *
     * @param plugin    Reference to the LifeEconomy plugin instance.
     * @param scheduler Folia-compatible scheduler.
     */
    public LifeEconomyAPI(LifeEconomy plugin, Scheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        instance = this;
    }

    /**
     * Gets the active LifeEconomyAPI instance.
     *
     * @return The API instance.
     */
    public static LifeEconomyAPI get() {
        return instance;
    }

    /**
     * Gives health to a player and optionally heals them.
     *
     * @param player The player to modify.
     * @param amount Health points to add (2.0 = +1 heart).
     * @param heal   If true, restores player to full health after change.
     */
    public void giveHealth(Player player, double amount, boolean heal) {
        UUID uuid = player.getUniqueId();

        plugin.db.querySafeAsync(
                "SELECT health FROM life_data WHERE uuid = ?",
                rs -> {
                    double stored = 20.0; // Default 10 hearts
                    if (rs.next()) {
                        stored = rs.getDouble("health");
                    } else {
                        plugin.db.updateSafe(
                                "INSERT INTO life_data (uuid, health) VALUES (?, ?)",
                                uuid.toString(), stored
                        );
                    }

                    double newHealth = stored + amount;
                    plugin.updateDb(uuid, newHealth);

                    if (player != null) {
                        scheduler.runGlobal(() -> {
                            player.setMaxHealth(newHealth);
                            if (heal) player.setHealth(player.getMaxHealth());
                        });
                    }

                    return null;
                },
                uuid.toString()
        );
    }

    /**
     * Removes health from a player, respecting the minimum of 2 health (1 heart).
     *
     * @param player The player to modify.
     * @param amount Health points to remove (2.0 = -1 heart).
     */
    public void takeHealth(Player player, double amount) {
        UUID uuid = player.getUniqueId();

        plugin.db.querySafeAsync(
                "SELECT health FROM life_data WHERE uuid = ?",
                rs -> {
                    double stored = 20.0;
                    if (rs.next()) {
                        stored = rs.getDouble("health");
                    } else {
                        plugin.db.updateSafe(
                                "INSERT INTO life_data (uuid, health) VALUES (?, ?)",
                                uuid.toString(), stored
                        );
                    }
                    double newHealth = Math.max(2.0, stored - amount);

                    if (stored <= 2.0) {
                        plugin.getLogger().info(player.getName() + " is already at minimum health.");
                        return null;
                    }

                    if (amount > stored - 2.0) {
                        plugin.getLogger().info(player.getName() + " cannot lose that much health.");
                        return null;
                    }

                    plugin.updateDb(uuid, newHealth);

                    if (player != null) {
                        scheduler.runGlobal(() -> player.setMaxHealth(newHealth));
                    }

                    return null;
                },
                uuid.toString()
        );
    }

    /**
     * Gets a player's stored health value from the cache.
     * @param player The player whose health to fetch.
     * @return A Double containing the player's health.
     */

    public Double getHealth(Player player) {
        UUID uuid = player.getUniqueId();
        return plugin.heartCache.get(uuid);
    }

    /**
     * Sets a player's health and updates both cache and database.
     *
     * @param player The player to update.
     * @param amount Health value to set (2.0 = 1 heart, 20.0 = default).
     */
    public void setHealth(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        plugin.updateDb(uuid, amount);

        if (player != null) {
            player.setMaxHealth(amount);
        }
    }
}
