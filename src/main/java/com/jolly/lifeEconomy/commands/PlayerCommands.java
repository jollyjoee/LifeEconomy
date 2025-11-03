package com.jolly.lifeEconomy.commands;

import com.jolly.lifeEconomy.LifeEconomy;
import com.jolly.lifeEconomy.LifeEconomyAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PlayerCommands implements CommandExecutor, TabCompleter {
    private LifeEconomyAPI api;
    private LifeEconomy plugin;
    public PlayerCommands(LifeEconomy plugin, LifeEconomyAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) {
            player.sendActionBar("§cUsage: /health <pay> <amount> <player>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("pay") && args.length == 3) {
            double amount;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendActionBar(plugin.mm().deserialize("<red>Please enter a valid number!"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                player.sendActionBar(plugin.mm().deserialize(plugin.getConfig().getString("messages.player-not-found", "<red>That player does not exist or is not online!")));
                return true;
            }
            if (amount <= 0) {
                player.sendActionBar(plugin.mm().deserialize("<red>Please enter a valid number!"));
                return true;
            }
            if (target.getName().equalsIgnoreCase(player.getName())) {
                player.sendActionBar(plugin.mm().deserialize(plugin.getConfig().getString("messages.pay-self", "<red>You cannot send health to yourself!")));
                return true;
            }
            if (api.getHealth(player) <= amount) {
                player.sendActionBar(plugin.mm().deserialize(plugin.getConfig().getString(
                                "messages.pay-more-than-health"
                                , "<red>You're trying to pay more than what you have!")
                ));
                return true;
            }
            if (api.getHealth(player) - amount < plugin.getConfig().getDouble("cost.minimum-health", 2.0)) {
                player.sendActionBar(plugin.mm().deserialize(plugin.getConfig().getString(
                        "messages.pay-more-than-minimum"
                        , "<red>Cannot send if it results in you having less than {minimum} health!")
                        .replace("{minimum}", String.valueOf(plugin.getConfig().getDouble("cost.minimum-health", 2.0)))
                ));
                return true;
            }
            api.giveHealth(target, amount, false);
            api.takeHealth(player, amount);
            player.sendActionBar(plugin.mm().deserialize(plugin.getConfig().getString(
                    "messages.pay", "<gray>You gave <gold>{target}</gold><green> {amount} <red> health")
                    .replace("{target}", target.getName())
                    .replace("{amount}", String.valueOf(amount))));
            return true;
        } else {
            player.sendActionBar("§cUsage: /health <pay> <amount> <player>");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();;
        if (args.length == 1) {
            return List.of("pay");
        }
        if (args.length == 2) {
            return List.of("<amount>");
        }
        if (args.length == 3) {
            return Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .filter(name -> !name.equalsIgnoreCase(player.getName()))
                    .toList();
        }
        return Collections.emptyList();
    }

}
