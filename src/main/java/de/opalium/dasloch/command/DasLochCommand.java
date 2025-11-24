package de.opalium.dasloch.command;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.model.ItemTemplate;
import de.opalium.dasloch.service.LifeTokenService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class DasLochCommand implements CommandExecutor {
    private final DasLochPlugin plugin;
    private final ItemsConfig itemsConfig;
    private final LifeTokenService lifeTokenService;

    public DasLochCommand(DasLochPlugin plugin, ItemsConfig itemsConfig, LifeTokenService lifeTokenService) {
        this.plugin = plugin;
        this.itemsConfig = itemsConfig;
        this.lifeTokenService = lifeTokenService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/dasloch reload §7- reload configs");
            sender.sendMessage("§e/dasloch debug §7- inspect item in hand");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "debug" -> handleDebug(sender);
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
        Optional<String> id = lifeTokenService.getId(item);
        Optional<ItemTemplate> template = id.flatMap(itemsConfig::getTemplate);
        sender.sendMessage("§7Item: " + (item.getType() != null ? item.getType().name() : "NONE"));
        sender.sendMessage("§7Template: " + id.orElse("n/a"));
        sender.sendMessage("§7Type: " + lifeTokenService.getType(item).map(Enum::name).orElse("n/a"));
        sender.sendMessage("§7Lives: §e" + lifeTokenService.getLives(item) + "§7/§e" + lifeTokenService.getMaxLives(item));
        sender.sendMessage("§7Tokens: §e" + lifeTokenService.getTokens(item));
        template.ifPresent(value -> {
            String markerType = value.getType() == de.opalium.dasloch.model.ItemType.MYSTIC ? "MYST" : "LEGEND";
            sender.sendMessage("§7Lore marker: §8[#" + markerType + "-" + value.getId() + "]");
        });
    }
}
