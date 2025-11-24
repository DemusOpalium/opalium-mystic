package de.opalium.dasloch.integration;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.item.MysticItemService;
import java.util.Map;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlaceholderHook extends PlaceholderExpansion {

    private final DasLochPlugin plugin;
    private final MysticItemService itemService;

    public PlaceholderHook(DasLochPlugin plugin) {
        this.plugin = plugin;
        this.itemService = plugin.getItemService();
    }

    @Override
    public String getIdentifier() {
        return "dasloch";
    }

    @Override
    public String getAuthor() {
        return "Opalium";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        return switch (identifier.toLowerCase()) {
            case "gold" -> String.valueOf(plugin.getVaultService().getBalance(player));
            case "lives" -> String.valueOf(itemService.getLives(hand));
            case "tokens" -> String.valueOf(itemService.getTokens(hand));
            case "enchants" -> formatEnchants(itemService.readEnchants(hand));
            default -> null;
        };
    }

    private String formatEnchants(Map<String, Integer> enchants) {
        if (enchants.isEmpty()) {
            return "none";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey()).append(" ").append(entry.getValue());
        }
        return builder.toString();
    }
}
