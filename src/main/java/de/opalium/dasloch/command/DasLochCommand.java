package de.opalium.dasloch.command;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.model.ItemTemplate;
import de.opalium.dasloch.model.ItemType;
import de.opalium.dasloch.service.ItemFactory;
import de.opalium.dasloch.service.LifeTokenService;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            sender.sendMessage("§e/dasloch well [roll <tier> [player]] §7- use the mystic well");
            sender.sendMessage("§e/dasloch mystic <id> [player] §7- give a mystic template (rohling)");
            sender.sendMessage("§e/dasloch legend <id> <player> §7- give a legend template");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> handleReload(sender);
            case "debug"  -> handleDebug(sender);
            case "well"   -> mysticWellCommand.execute(sender, "mysticwell", dropFirst(args));
            case "mystic" -> handleMysticGive(sender, dropFirst(args));
            case "legend" -> handleLegendGive(sender, dropFirst(args));
            default       -> sender.sendMessage("§cUnknown subcommand.");
        }
        return true;
    }

    // =========================
    // /dasloch reload
    // =========================
    private void handleReload(CommandSender sender) {
        plugin.reloadAll();
        sender.sendMessage("§aDasLoch configurations reloaded.");
    }

    // =========================
    // /dasloch debug
    // =========================
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

    // =========================
    // /dasloch mystic <id> [player]
    // =========================
    private void handleMysticGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dasloch.mystic.give")) {
            sender.sendMessage("§cDafür hast du keine Berechtigung.");
            return;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage("§cVerwendung: /dasloch mystic <id> [spieler]");
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);

        ItemTemplate template = itemsConfig.getTemplate(id)
                .filter(t -> t.getType() == ItemType.MYSTIC)
                .orElse(null);

        if (template == null) {
            sender.sendMessage("§cUnbekanntes mystisches Item: §f" + id);
            return;
        }

        Player target;
        if (args.length == 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cSpieler nicht gefunden: §f" + args[1]);
                return;
            }
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("§cBitte gib einen Spieler an.");
                return;
            }
            target = p;
        }

        ItemFactory factory = plugin.getItemFactory();
        ItemStack item = factory.createMysticItem(template);

        target.getInventory().addItem(item);

        sender.sendMessage("§aMystic-Rohling §f" + id + " §aan §f" + target.getName() + " §agegeben.");
        if (sender != target) {
            target.sendMessage("§aDu hast ein mystisches Rohling-Item erhalten: §f" + id);
        }
    }

    // =========================
    // /dasloch legend <id> <player>
    // =========================
    private void handleLegendGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dasloch.legend.give")) {
            sender.sendMessage("§cDafür hast du keine Berechtigung.");
            return;
        }

        if (args.length != 2) {
            sender.sendMessage("§cVerwendung: /dasloch legend <id> <spieler>");
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);

        ItemTemplate template = itemsConfig.getTemplate(id)
                .filter(t -> t.getType() == ItemType.LEGEND)
                .orElse(null);

        if (template == null) {
            sender.sendMessage("§cUnbekanntes legendäres Item: §f" + id);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cSpieler nicht gefunden: §f" + args[1]);
            return;
        }

        ItemFactory factory = plugin.getItemFactory();
        OfflinePlayer owner = target;
        ItemStack item = factory.createLegendItem(template, owner);

        target.getInventory().addItem(item);

        sender.sendMessage("§6Legend-Item §f" + id + " §aan §f" + target.getName() + " §agegeben.");
        if (sender != target) {
            target.sendMessage("§6Du hast ein legendäres Item erhalten: §f" + id);
        }
    }

    // =========================
    // Hilfsfunktion
    // =========================
    private String[] dropFirst(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] copy = new String[args.length - 1];
        System.arraycopy(args, 1, copy, 0, copy.length);
        return copy;
    }
}
