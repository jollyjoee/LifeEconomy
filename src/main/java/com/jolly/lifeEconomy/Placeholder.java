package com.jolly.lifeEconomy;

import com.jolly.lifeEconomy.LifeEconomyAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {

    private final LifeEconomy plugin;
    private final LifeEconomyAPI api;
    public Placeholder(LifeEconomy plugin,  LifeEconomyAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "life";
    }

    @Override
    public @NotNull String getAuthor() {
        return "jolly";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null || !player.isOnline()) return "";

        double health = LifeEconomyAPI.get().getHealth(player.getPlayer());
        double hearts = health / 2.0;
        double minHealth = plugin.getConfig().getDouble("cost.minimum-health", 2.0);
        double remaining = (health - minHealth) / 2.0;

        // ✅ %life_health%
        if (identifier.equalsIgnoreCase("health")) return String.valueOf(health);

        // ✅ %life_hearts%
        if (identifier.equalsIgnoreCase("hearts")) return String.valueOf((int) hearts);

        // ✅ %life_hearts_formatted%
        if (identifier.equalsIgnoreCase("hearts_formatted")) return "❤ x" + ((int) hearts);

        // ✅ %life_percentage%
        if (identifier.equalsIgnoreCase("percentage")) return String.format("%.0f%%", (health / 20.0) * 100);

        // ✅ %life_min_health%
        if (identifier.equalsIgnoreCase("min_health")) return String.valueOf(minHealth);

        // ✅ %life_can_lose%
        if (identifier.equalsIgnoreCase("can_lose")) return String.valueOf((int) Math.max(0, remaining));

        // ✅ %life_is_min_health%
        if (identifier.equalsIgnoreCase("is_min_health")) return String.valueOf(health <= minHealth);

        // ✅ %life_bar% (ASCII hearts)
        if (identifier.equalsIgnoreCase("bar")) {
            String bar = generateHeartsBar((int) hearts, "<red>❤</red>", "<gray>❤</gray>", 10);
            return miniToLegacy(bar);
        }

        // ✅ Planned leaderboard placeholders
        if (identifier.equalsIgnoreCase("rank")) return "Coming soon";
        if (identifier.equalsIgnoreCase("top_1")) return "Coming soon";
        if (identifier.equalsIgnoreCase("top_1_hearts")) return "Coming soon";
        return null;
    }


    private String generateHeartsBar(int valueHearts, String full, String empty, int heartsPerLine) {
        StringBuilder bar = new StringBuilder();

        for (int i = 1; i <= valueHearts; i++) {
            bar.append(full);

            if (i % heartsPerLine == 0 && i != valueHearts) {
                bar.append("\n");
            }
        }

        int remainder = valueHearts % heartsPerLine;
        if (remainder != 0) {
            int emptyNeeded = heartsPerLine - remainder;
            for (int i = 0; i < emptyNeeded; i++) {
                bar.append(empty);
            }
        }

        return bar.toString();
    }

    private String miniToLegacy(String miniMsg) {
        return LegacyComponentSerializer.legacySection().serialize(
                MiniMessage.miniMessage().deserialize(miniMsg)
        );
    }

}

