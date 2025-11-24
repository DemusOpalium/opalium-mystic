package de.opalium.dasloch.command;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.command.MysticWellCommand;
import de.opalium.dasloch.item.ItemKind;
import de.opalium.dasloch.item.MysticItemService;
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
    private final MysticWellCommand mysticWellCommand;

    public DasLochCommand(DasLochPlugin plugin, MysticItemService itemService, MysticWellCommand mysticWellCommand) {
        this.plugin = plugin;
        this.itemService = itemService;
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
        sender.sendMessage("§7Item: " + (item.getType() != null ? item.getType().name() : "NONE"));
        if (!itemService.isCustomItem(item)) {
            sender.sendMessage("§cThis is not a DasLoch custom item.");
            return;
        }

        Optional<String> id = itemService.getId(item);
        ItemKind kind = itemService.getKind(item);

        sender.sendMessage("§7Template: " + id.orElse("n/a"));
        sender.sendMessage("§7Type: " + (kind == null ? "n/a" : kind.name()));
        sender.sendMessage("§7Lives: §e" + itemService.getLives(item) + "§7/§e" + itemService.getMaxLives(item));
        sender.sendMessage("§7Tokens: §e" + itemService.getTokens(item));

        id.flatMap(itemService::getDefinition).ifPresent(definition ->
                sender.sendMessage("§7Lore marker: §8[#" + definition.kind().name() + "-" + definition.id() + "]")
        );
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
