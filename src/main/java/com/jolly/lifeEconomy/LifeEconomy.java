package com.jolly.lifeEconomy;

import com.jolly.lifeEconomy.commands.ModifyHealth;
import com.jolly.lifeEconomy.commands.PlayerCommands;
import com.jolly.lifeEconomy.display.ActionBar;
import com.jolly.lifeEconomy.listeners.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LifeEconomy extends JavaPlugin {
    public Scheduler scheduler;
    public static MiniMessage mm;
    private static ConfigurationSection config;
    public static DatabaseManager db;
    private LifeEconomyAPI api;
    private ModifyHealth modifyHealth;
    private ActionBar actionBar;
    private PlayerCommands playerCommands;
    public final Map<UUID, Double> heartCache = new ConcurrentHashMap<>();
    @Override
    public void onEnable() {
        saveDefaultConfig();
        mm = MiniMessage.miniMessage();
        config = getConfig();

        ConfigurationSection dbConfig = config.getConfigurationSection("database");
        String type = dbConfig.getString("type", "sqlite").toLowerCase();
        // ================================
        // 📦 DATABASE SETUP
        // ================================
        if (type.equals("mysql")) {
            String host = dbConfig.getString("host", "localhost");
            int port = dbConfig.getInt("port", 3306);
            String database = dbConfig.getString("database", "serversentials");
            String username = dbConfig.getString("username", "root");
            String password = dbConfig.getString("password", "");

            db = new DatabaseManager(this, true, host, port, database, username, password, null);
            getLogger().info("✅ Using MySQL database at " + host + ":" + port);
        } else {
            // SQLite
            String fileName = dbConfig.getString("file", "serversentials.db");
            File sqliteFile = new File(getDataFolder(), fileName);

            db = new DatabaseManager(this, false, "", 0, "", "", "", sqliteFile);
            getLogger().info("✅ Using SQLite database at " + sqliteFile.getAbsolutePath());
        }
        initTable();
        getLogger().info("LifeEconomy API loaded!");
        scheduler = new Scheduler(this);
        api = new LifeEconomyAPI(this, scheduler);
        modifyHealth = new ModifyHealth(this, api);
        playerCommands = new PlayerCommands(this, api);
        actionBar = new ActionBar(this, scheduler);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this, scheduler, actionBar), this);
        getCommand("life").setExecutor(modifyHealth);
        getCommand("life").setTabCompleter(modifyHealth);
        getCommand("health").setExecutor(playerCommands);
        getCommand("health").setTabCompleter(playerCommands);
        // ================================
        // 🪄 PLACEHOLDERAPI HOOK
        // ================================
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            scheduler.runLater(() -> {
                new Placeholder(this, api).register();
                getLogger().info("✅ Registered Serversentials placeholders with PlaceholderAPI!");
            }, 20L);

        } else {
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPluginEnable(PluginEnableEvent event) {
                    if (event.getPlugin().getName().equals("PlaceholderAPI")) {
                        scheduler.runLater(() -> {
                            new Placeholder(LifeEconomy.this, api).register();
                            getLogger().info("✅ Registered Serversentials placeholders with PlaceholderAPI (delayed)!");
                        }, 20L);
                    }
                }
            }, this);
        }
    }

    @Override
    public void onDisable() {
        if (db != null) {
            db.close();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            double health = heartCache.remove(player.getUniqueId());
            updateDb(player.getUniqueId(), health);
        }
    }

    public LifeEconomyAPI getAPI() { return api; }

    private void register(String cmd, CommandExecutor executor, TabCompleter completer) {
        getCommand(cmd).setExecutor(executor);
        getCommand(cmd).setTabCompleter(completer);
    }

    private void initTable() {
        db.updateSafe("""
            CREATE TABLE IF NOT EXISTS life_data (
                uuid TEXT PRIMARY KEY,
                health DOUBLE NOT NULL
            )
        """);
    }

    public void updateDb(UUID uuid, double hearts) {
        heartCache.put(uuid, hearts);
        db.updateSafeAsync("""
            INSERT INTO life_data (uuid, health)
            VALUES (?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                health = excluded.health
        """, uuid.toString(), hearts);
    }

    public MiniMessage mm() {
        return mm;
    }

}
