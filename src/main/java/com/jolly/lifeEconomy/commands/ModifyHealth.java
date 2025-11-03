package com.jolly.lifeEconomy.commands;

import com.jolly.lifeEconomy.LifeEconomy;
import com.jolly.lifeEconomy.LifeEconomyAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ModifyHealth implements CommandExecutor, TabCompleter {
    private LifeEconomyAPI api;
    private LifeEconomy plugin;

    public ModifyHealth(LifeEconomy plugin, LifeEconomyAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /life <give | take | set | get>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("get") && args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            api.getHealth(target).thenAccept(hearts ->
                    sender.sendMessage("§a" + target.getName() + " has " + hearts/2 + " hearts.")
            );
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /life " + sub + " <player> <amount>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number.");
            return true;
        }
        switch (sub) {
            case "give" -> {
                boolean heal = args.length >= 4 && Boolean.parseBoolean(args[3]);
                api.giveHealth(target, amount, heal);
                sender.sendMessage("§aGave " + amount + " health to " + target.getName());
            }
            case "take" -> {
                api.takeHealth(target, amount);
                sender.sendMessage("§aTook " + amount + " health from " + target.getName());
            }
            case "set" -> {
                api.setHealth(target, amount);
                sender.sendMessage("§aSet " + target.getName() + "'s health to " + amount);
            }
            default -> sender.sendMessage("§cUnknown subcommand. Try: give, take, set, get");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "take", "set", "get")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            return List.of("2.0", "4.0", "10.0");
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return List.of("true", "false");
        }
        return Collections.emptyList();
    }

}
