package de.opalium.dasloch.command;

import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.integration.VaultService;
import de.opalium.dasloch.model.ItemType;
import de.opalium.dasloch.service.ItemFactory;
import de.opalium.dasloch.service.LifeTokenService;
import de.opalium.dasloch.well.MysticWellService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional; // <--- WICHTIG: fehlender Import

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MysticWellCommand implements CommandExecutor, TabCompleter {

    private final ItemsConfig itemsConfig;
    private final LifeTokenService lifeTokenService;
    private final ItemFactory itemFactory;
    private final MysticWellService wellService;
    private final VaultService vaultService;

    public MysticWellCommand(
        ItemsConfig itemsConfig,
        LifeTokenService lifeTokenService,
        ItemFactory itemFactory,
        MysticWellService wellService,
        VaultService vaultService
    ) {
        this.itemsConfig = itemsConfig;
        this.lifeTokenService = lifeTokenService;
        this.itemFactory = itemFactory;
        this.wellService = wellService;
        this.vaultService = vaultService;
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
        if (wellService.tier(tierId) == null) {
            initiator.sendMessage("§cUnknown mystic well tier: " + tierId);
            return true;
        }

        ItemStack held = target.getInventory().getItemInMainHand();
        Optional<ItemType> type = lifeTokenService.getType(held);
        if (type.isEmpty() || type.get() != ItemType.MYSTIC) {
            initiator.sendMessage("§c" + target.getName() + " is not holding a mystic item.");
            return true;
        }

        int cost = wellService.baseCosts().getOrDefault(resolveCostKey(tierId), 0);
        if (!vaultService.hasEconomy()) {
            initiator.sendMessage("§cVault economy is not available.");
            return true;
        }
        if (vaultService.getBalance(target) < cost) {
            initiator.sendMessage("§c" + target.getName() + " lacks the required gold: " + cost);
            return true;
        }

        if (cost > 0) {
            vaultService.withdraw(target, cost);
        }

        MysticWellService.RollResult result = wellService.roll(tierId);
        int newTokens = lifeTokenService.getTokens(held) + result.tokensAwarded();
        lifeTokenService.setTokens(held, newTokens);
        lifeTokenService.getId(held)
            .flatMap(itemsConfig::getTemplate)
            .ifPresent(template -> itemFactory.refreshLore(held, template));

        target.getInventory().setItemInMainHand(held);
        target.sendMessage("§aMystic Well Roll: +" + result.tokensAwarded()
                + " Tokens (§e" + newTokens + "§a total), rarity: §e"
                + result.rarityRolled());
        if (!initiator.equals(target)) {
            initiator.sendMessage("§aApplied mystic well roll for " + target.getName()
                    + " (Tier " + tierId.toUpperCase(Locale.ROOT) + ")");
        }
        return true;
    }

    private String resolveCostKey(String tierId) {
        return switch (tierId.toUpperCase(Locale.ROOT)) {
            case "I", "1", "TIER1", "TIER_1" -> "tier_1";
            case "II", "2", "TIER2", "TIER_2" -> "tier_2";
            case "III", "3", "TIER3", "TIER_3" -> "tier_3";
            default -> tierId.toLowerCase(Locale.ROOT);
        };
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
