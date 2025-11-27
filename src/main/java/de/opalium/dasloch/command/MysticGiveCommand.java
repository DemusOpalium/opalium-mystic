package de.opalium.dasloch.command;

import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.model.ItemTemplate;
import de.opalium.dasloch.model.ItemType;
import de.opalium.dasloch.service.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LegendGiveCommand implements CommandExecutor, TabCompleter {

    private final ItemsConfig itemsConfig;
    private final ItemFactory itemFactory;

    public LegendGiveCommand(ItemsConfig itemsConfig, ItemFactory itemFactory) {
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
        if (!sender.hasPermission("dasloch.legend.give")) {
            sender.sendMessage("§cDafür hast du keine Berechtigung.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§cVerwendung: /legendgive <id> <spieler>");
            return true;
        }

        String id = args[0].toLowerCase(Locale.ROOT);

        ItemTemplate template = itemsConfig.getTemplate(id)
                .filter(t -> t.getType() == ItemType.LEGEND)
                .orElse(null);

        if (template == null) {
            sender.sendMessage("§cUnbekanntes legendäres Item: §f" + id);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cSpieler nicht gefunden: §f" + args[1]);
            return true;
        }

        OfflinePlayer owner = target; // Owner = Zielspieler
        ItemStack item = itemFactory.createLegendItem(template, owner);

        target.getInventory().addItem(item);

        sender.sendMessage("§6Legend-Item §f" + id + " §aan §f" + target.getName() + " §agegeben.");
        if (sender != target) {
            target.sendMessage("§6Du hast ein legendäres Item erhalten: §f" + id);
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
                if (template.getType() == ItemType.LEGEND) {
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
