package de.opalium.dasloch.command;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.enchant.EnchantRegistry;
import de.opalium.dasloch.item.MysticItemService;
import de.opalium.dasloch.model.ItemTemplate;
import de.opalium.dasloch.model.ItemType;
import de.opalium.dasloch.service.LifeTokenService;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DasLochCommand implements CommandExecutor {

    private final DasLochPlugin plugin;
    private final ItemsConfig itemsConfig;
    private final LifeTokenService lifeTokenService;
    private final MysticWellCommand mysticWellCommand;
    private final EnchantRegistry enchantRegistry;
    private final MysticItemService itemService;

    public DasLochCommand(
            DasLochPlugin plugin,
            ItemsConfig itemsConfig,
            LifeTokenService lifeTokenService,
            MysticWellCommand mysticWellCommand
    ) {
        this.plugin = plugin;
        this.itemsConfig = itemsConfig;
        this.lifeTokenService = lifeTokenService;
        this.mysticWellCommand = mysticWellCommand;
        // Zugriff auf Registry und Mystic-Item-Service über das Plugin
        this.enchantRegistry = plugin.getEnchantRegistry();
        this.itemService = plugin.getItemService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/dasloch reload §7- reload configs");
            sender.sendMessage("§e/dasloch debug §7- inspect item in hand");
            sender.sendMessage("§e/dasloch well roll <tier> [player] §7- roll the mystic well for a player");
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

        // Prüfen, ob das ein LifeToken/Mystic/Legend-Item ist
        Optional<ItemType> type = lifeTokenService.getType(item);
        if (type.isEmpty()) {
            sender.sendMessage("§cThis is not a DasLoch custom item.");
            return;
        }

        Optional<String> id = lifeTokenService.getId(item);

        sender.sendMessage("§7Template: " + id.orElse("n/a"));
        sender.sendMessage("§7Type: " + type.get().name());
        sender.sendMessage("§7Lives: §e" + lifeTokenService.getLives(item)
                + "§7/§e" + lifeTokenService.getMaxLives(item));
        sender.sendMessage("§7Tokens: §e" + lifeTokenService.getTokens(item));

        // Template / Lore-Marker anzeigen, falls vorhanden
        id.flatMap(itemsConfig::getTemplate)
                .ifPresent(template -> sender.sendMessage(formatTemplateInfo(template)));

        // Enchants über MysticItemService auslesen und hübsch formatieren
        Map<String, Integer> enchants = itemService.readEnchants(item);
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

    private String formatTemplateInfo(ItemTemplate template) {
        return "§7Lore marker: §8[#"
                + template.type().name()
                + "-"
                + template.id()
                + "]";
    }

    private String formatEnchants(Map<String, Integer> enchants) {
        if (enchants.isEmpty()) {
            return "§7Enchants: §8none";
        }

        StringJoiner joiner = new StringJoiner("§7, ", "§7Enchants: §e", "");
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            String key = entry.getKey();
            int tier = entry.getValue();

            EnchantDefinition def = enchantRegistry.get(key);
            String label = key;
            if (def != null) {
                label += " (§f" + def.displayName() + "§e)";
            }
            joiner.add(label + " §7T" + tier);
        }
        return joiner.toString();
    }
}
