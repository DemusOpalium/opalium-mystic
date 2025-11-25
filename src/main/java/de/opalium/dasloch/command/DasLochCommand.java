package de.opalium.dasloch.command;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.model.ItemType;
import de.opalium.dasloch.service.LifeTokenService;
import java.util.Optional;
import org.bukkit.Bukkit;
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
            case "well" -> mysticWellCommand.execute(sender, "mysticwell", dropFirst(args));
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
        sender.sendMessage("§7Item: " + (item != null ? item.getType().name() : "NONE"));

        if (item == null || item.getType().isAir()) {
            sender.sendMessage("§cNo item in hand.");
            return;
        }

        Optional<String> idOpt = lifeTokenService.getId(item);
        Optional<ItemType> typeOpt = lifeTokenService.getType(item);

        sender.sendMessage("§7Template: " + idOpt.orElse("n/a"));
        sender.sendMessage("§7Type: " + typeOpt.map(Enum::name).orElse("n/a"));
        sender.sendMessage("§7Lives: §e" + lifeTokenService.getLives(item)
                + "§7/§e" + lifeTokenService.getMaxLives(item));
        sender.sendMessage("§7Tokens: §e" + lifeTokenService.getTokens(item));

        idOpt.flatMap(itemsConfig::getTemplate).ifPresent(template -> {
            sender.sendMessage("§7Lore marker: §8[#"
                    + template.getType().name()
                    + "-"
                    + template.getId()
                    + "]");
        });
    }

    private String[] dropFirst(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] copy = new String[args.length - 1];
        System.arraycopy(args, 1, copy, 0, copy.length);
        return copy;
    }
}
