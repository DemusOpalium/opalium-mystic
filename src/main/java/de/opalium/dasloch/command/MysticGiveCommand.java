package de.opalium.dasloch.command;

import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.model.ItemTemplate;
import de.opalium.dasloch.model.ItemType;
import de.opalium.dasloch.service.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MysticGiveCommand implements CommandExecutor, TabCompleter {

    private final ItemsConfig itemsConfig;
    private final ItemFactory itemFactory;

    public MysticGiveCommand(ItemsConfig itemsConfig, ItemFactory itemFactory) {
        this.itemsConfig = itemsConfig;
        this.itemFactory = itemFactory;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("dasloch.mystic.give")) {
            sender.sendMessage("§cDafür hast du keine Berechtigung.");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage("§cVerwendung: /mysticgive <id> [spieler]");
            return true;
        }

        String id = args[0].toLowerCase(Locale.ROOT);

        // Nur Mystic-Templates sind erlaubt
        ItemTemplate template = itemsConfig.getTemplate(id)
                .filter(t -> t.getType() == ItemType.MYSTIC)
                .orElse(null);

        if (template == null) {
            sender.sendMessage("§cUnbekanntes mystisches Item: §f" + id);
            return true;
        }

        Player target;
        if (args.length == 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cSpieler nicht gefunden: §f" + args[1]);
                return true;
            }
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("§cBitte gib einen Spieler an.");
                return true;
            }
            target = p;
        }

        // Wichtig: Mystic-ROHLING über ItemFactory erzeugen
        ItemStack item = itemFactory.createMysticItem(template);
        target.getInventory().addItem(item);

        sender.sendMessage("§aMystic-Rohling §f" + id + " §aan §f" + target.getName() + " §agegeben.");
        if (sender != target) {
            target.sendMessage("§aDu hast ein mystisches Rohling-Item erhalten: §f" + id);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (ItemTemplate template : itemsConfig.getTemplates().values()) {
                if (template.getType() == ItemType.MYSTIC) {
                    String tid = template.getId().toLowerCase(Locale.ROOT);
                    if (tid.startsWith(prefix)) {
                        completions.add(tid);
                    }
                }
            }
        } else if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    completions.add(online.getName());
                }
            }
        }

        return completions;
    }
}
