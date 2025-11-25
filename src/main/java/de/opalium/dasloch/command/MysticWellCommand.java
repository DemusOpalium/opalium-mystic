package de.opalium.dasloch.command;

import de.opalium.dasloch.item.ItemKind;
import de.opalium.dasloch.item.MysticItemService;
import de.opalium.dasloch.well.MysticWellService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MysticWellCommand implements CommandExecutor, TabCompleter {

    private final MysticItemService itemService;

    public MysticWellCommand(MysticItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, label, args);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        // /mysticwell → Standard: Tier I auf sich selbst
        if (args.length == 0) {
            if (sender instanceof Player player) {
                return rollForPlayer(player, "I", sender);
            }
            sender.sendMessage("§cUsage: /" + label + " roll <tier> [player]");
            return true;
        }

        // Einziger Subcommand: roll
        if (!"roll".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§cUnknown subcommand. Use /" + label + " roll <tier> [player]");
            return true;
        }

        String tierId = args.length >= 2 ? args[1] : "I";

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[2]);
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("§cYou must specify a player when using this command from console.");
            return true;
        }

        return rollForPlayer(target, tierId, sender);
    }

    private boolean rollForPlayer(Player target, String tierId, CommandSender initiator) {
        ItemStack held = target.getInventory().getItemInMainHand();

        // Muss ein Mystic-Custom-Item sein
        if (!itemService.isCustomItem(held) || itemService.getKind(held) != ItemKind.MYSTIC) {
            initiator.sendMessage("§c" + target.getName() + " hält kein mystisches Item.");
            return true;
        }

        int oldTokens = itemService.getTokens(held);

        // Hier passiert die ganze Magie: Gold, Roll, Enchants, Tokens, Prefix, Lore
        MysticWellService.RollResult result = itemService.rollMystic(target, held, tierId);
        if (result == null) {
            initiator.sendMessage("§cMystic-Roll konnte nicht ausgeführt werden. Tier oder Balance prüfen.");
            return true;
        }

        int newTokens = itemService.getTokens(held);
        int gained = Math.max(0, newTokens - oldTokens);

        target.getInventory().setItemInMainHand(held);

        String rarity = result.rarityRolled();
        target.sendMessage("§aMystic Well Roll: +" + gained
                + " Tokens (§e" + newTokens + "§a total), rarity: §e" + rarity);
        if (!initiator.equals(target)) {
            initiator.sendMessage("§aMystic-Roll für "
                    + target.getName() + " angewendet (+" + gained + " Tokens, jetzt " + newTokens + ")");
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
