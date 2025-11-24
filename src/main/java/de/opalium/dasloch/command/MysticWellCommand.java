package de.opalium.dasloch.command;

import de.opalium.dasloch.item.ItemKind;
import de.opalium.dasloch.item.MysticItemService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MysticWellCommand implements CommandExecutor, TabCompleter {

    private final MysticItemService itemService;

    public MysticWellCommand(
        MysticItemService itemService
    ) {
        this.itemService = itemService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, label, args);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                return rollForPlayer(player, "I", sender);
            }
            sender.sendMessage("§cUsage: /" + label + " roll <tier> <player>");
            return true;
        }

        if (!"roll".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§cUnknown subcommand. Use /" + label + " roll <tier> [player]");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /" + label + " roll <tier> [player]");
            return true;
        }

        String tierId = args[1];
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[2]);
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cConsole must specify a player.");
                return true;
            }
            target = player;
        }

        return rollForPlayer(target, tierId, sender);
    }

    private boolean rollForPlayer(Player target, String tierId, CommandSender initiator) {
        ItemStack held = target.getInventory().getItemInMainHand();
        if (itemService.getKind(held) != ItemKind.MYSTIC) {
            initiator.sendMessage("§c" + target.getName() + " is not holding a mystic item.");
            return true;
        }

        int previousTokens = itemService.getTokens(held);
        if (!itemService.rollMystic(target, held, tierId)) {
            initiator.sendMessage("§cCould not roll mystic well for " + target.getName()
                    + ". Check the tier or gold requirements.");
            return true;
        }

        target.getInventory().setItemInMainHand(held);
        int newTokens = itemService.getTokens(held);
        int delta = Math.max(0, newTokens - previousTokens);
        target.sendMessage("§aMystic Well Roll: +" + delta
                + " Tokens (§e" + newTokens + "§a total)");
        if (!initiator.equals(target)) {
            initiator.sendMessage("§aApplied mystic well roll for " + target.getName()
                    + " (Tier " + tierId.toUpperCase(Locale.ROOT) + ")");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("roll");
        }
        if (args.length == 2 && "roll".equalsIgnoreCase(args[0])) {
            return List.of("I", "II", "III");
        }
        if (args.length == 3 && "roll".equalsIgnoreCase(args[0])) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            Collections.sort(players);
            return players;
        }
        return Collections.emptyList();
    }
}
