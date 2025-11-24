package de.opalium.dasloch.command;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.enchant.EnchantRegistry;
import de.opalium.dasloch.item.ItemKind;
import de.opalium.dasloch.item.MysticItemService;
import de.opalium.dasloch.util.PluginKeys;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

public class DasLochCommand implements CommandExecutor {
    private final DasLochPlugin plugin;
    private final MysticItemService itemService;
    private final PluginKeys keys;
    private final EnchantRegistry enchantRegistry;
    private final MysticWellCommand mysticWellCommand;

    public DasLochCommand(
            DasLochPlugin plugin,
            MysticItemService itemService,
            EnchantRegistry enchantRegistry,
            MysticWellCommand mysticWellCommand
    ) {
        this.plugin = plugin;
        this.itemService = itemService;
        this.keys = itemService.keys();
        this.enchantRegistry = enchantRegistry;
        this.mysticWellCommand = mysticWellCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/dasloch reload §7- reload configs");
            sender.sendMessage("§e/dasloch debug §7- inspect item in hand");
            sender.sendMessage("§e/dasloch well roll <tier> <player> §7- roll the mystic well for a player");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "debug" -> handleDebug(sender);
            case "well" -> mysticWellCommand.execute(sender, "well", dropFirst(args));
            default -> sender.sendMessage("§cUnknown subcommand.");
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadAll();
        sender.sendMessage("§aDasLoch configurations reloaded.");
    }

    private void handleDebug(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDebug can only be run in-game.");
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            sender.sendMessage("§cNo item metadata found.");
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Optional<String> id = Optional.ofNullable(container.get(keys.itemId(), PersistentDataType.STRING));
        String kindRaw = container.get(keys.itemType(), PersistentDataType.STRING);
        ItemKind kind = null;
        if (kindRaw != null) {
            try {
                kind = ItemKind.fromString(kindRaw);
            } catch (IllegalArgumentException ignored) {
                // fall back to null
            }
        }
        int livesCurrent = container.getOrDefault(keys.livesCurrent(), PersistentDataType.INTEGER, 0);
        int livesMax = container.getOrDefault(keys.livesMax(), PersistentDataType.INTEGER, 0);
        int tokens = container.getOrDefault(keys.tokens(), PersistentDataType.INTEGER, 0);
        String mysticTier = container.getOrDefault(keys.mysticTier(), PersistentDataType.STRING, "");
        String prefix = container.getOrDefault(keys.prefix(), PersistentDataType.STRING, "");
        Map<String, Integer> enchants = itemService.readEnchants(item);

        sender.sendMessage("§7Item: " + (item.getType() != null ? item.getType().name() : "NONE"));
        sender.sendMessage("§7Item ID: §e" + id.orElse("n/a"));
        sender.sendMessage("§7Kind: §e" + (kind != null ? kind.name() : "n/a"));
        sender.sendMessage("§7Lives: §e" + livesCurrent + "§7/§e" + livesMax);
        sender.sendMessage("§7Tokens: §e" + tokens);
        sender.sendMessage("§7Mystic Tier: §e" + (!mysticTier.isEmpty() ? mysticTier : "n/a"));
        sender.sendMessage("§7Prefix: §r" + (!prefix.isEmpty() ? prefix : "§7n/a"));
        sender.sendMessage(formatEnchants(enchants));
    }

    private String[] dropFirst(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] copy = new String[args.length - 1];
        System.arraycopy(args, 1, copy, 0, copy.length);
        return copy;
    }

    private String formatEnchants(Map<String, Integer> enchants) {
        if (enchants.isEmpty()) {
            return "§7Enchants: §8none";
        }

        StringJoiner joiner = new StringJoiner("§7, ", "§7Enchants: §e", "");
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            EnchantDefinition def = enchantRegistry.get(entry.getKey());
            String label = entry.getKey();
            if (def != null) {
                label += " (§f" + def.displayName() + "§e)";
            }
            joiner.add(label + " §7T" + entry.getValue());
        }
        return joiner.toString();
    }
}
