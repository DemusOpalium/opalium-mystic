package de.opalium.dasloch.command;

import de.opalium.dasloch.item.ItemKind;
import de.opalium.dasloch.item.MysticItemService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MysticGiveCommand implements CommandExecutor, TabCompleter {
    private final MysticItemService itemService;

    public MysticGiveCommand(MysticItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /" + label + " <id> <player>");
            return true;
        }

        String itemId = args[0];
        if (itemService.getDefinition(itemId).map(def -> def.kind() != ItemKind.MYSTIC).orElse(true)) {
            sender.sendMessage("§cUnknown mystic item: " + itemId);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not online: " + args[1]);
            return true;
        }

        ItemStack item = itemService.createMysticItem(itemId);
        target.getInventory().addItem(item);
        sender.sendMessage("§aGave mystic item " + itemId + " to " + target.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return itemService.definitionIds(ItemKind.MYSTIC).stream()
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return Collections.emptyList();
    }
}
