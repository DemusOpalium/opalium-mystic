package de.opalium.dasloch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class DasLochPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("DasLoch plugin enabling...");
        registerCommands();
    }

    @Override
    public void onDisable() {
        getLogger().info("DasLoch plugin disabled.");
    }

    private void registerCommands() {
        PluginCommand command = getCommand("dasloch");
        if (command == null) {
            getLogger().severe("Command 'dasloch' is not defined in plugin.yml");
            return;
        }

        DasLochCommand handler = new DasLochCommand();
        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }

    private static final class DasLochCommand implements CommandExecutor, TabCompleter {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <reload|debug>");
                return true;
            }

            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "reload":
                    sender.sendMessage(ChatColor.GREEN + "Reloading configuration... (stub)");
                    return true;
                case "debug":
                    sender.sendMessage(ChatColor.AQUA + "Debug information is not implemented yet.");
                    return true;
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use reload or debug.");
                    return true;
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1) {
                List<String> suggestions = new ArrayList<>();
                if ("reload".startsWith(args[0].toLowerCase())) {
                    suggestions.add("reload");
                }
                if ("debug".startsWith(args[0].toLowerCase())) {
                    suggestions.add("debug");
                }
                return suggestions;
            }
            return Collections.emptyList();
        }
    }
}
