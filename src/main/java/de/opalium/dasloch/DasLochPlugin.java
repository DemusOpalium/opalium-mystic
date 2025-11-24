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

    private boolean debugEnabled;

    @Override
    public void onEnable() {
        getLogger().info("DasLoch plugin enabling...");
        saveDefaultConfig();
        reloadPluginConfig();
        registerListeners();
        registerCommands();
        getLogger().info("DasLoch plugin enabled.");
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

        DasLochCommand handler = new DasLochCommand(this);
        command.setExecutor(handler);
        command.setTabCompleter(handler);
        getLogger().info("Registered /dasloch command handler.");
    }

    private void registerListeners() {
        getLogger().info("No listeners to register yet.");
    }

    void reloadPluginConfig() {
        reloadConfig();
        debugEnabled = getConfig().getBoolean("debug", false);
    }

    boolean isDebugEnabled() {
        return debugEnabled;
    }

    void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        getConfig().set("debug", debugEnabled);
        saveConfig();
    }

    private static final class DasLochCommand implements CommandExecutor, TabCompleter {

        private final DasLochPlugin plugin;

        private DasLochCommand(DasLochPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <reload|debug> [on|off]");
                return true;
            }

            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "reload":
                    plugin.reloadPluginConfig();
                    sender.sendMessage(ChatColor.GREEN + "DasLoch configuration reloaded.");
                    return true;
                case "debug":
                    boolean newValue = !plugin.isDebugEnabled();
                    if (args.length > 1) {
                        String value = args[1].toLowerCase();
                        if (value.equals("on")) {
                            newValue = true;
                        } else if (value.equals("off")) {
                            newValue = false;
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid value. Use 'on' or 'off'.");
                            return true;
                        }
                    }

                    plugin.setDebugEnabled(newValue);
                    sender.sendMessage(ChatColor.AQUA + "Debug mode is now " + (newValue ? "enabled" : "disabled") + ".");
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

            if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
                List<String> suggestions = new ArrayList<>();
                if ("on".startsWith(args[1].toLowerCase())) {
                    suggestions.add("on");
                }
                if ("off".startsWith(args[1].toLowerCase())) {
                    suggestions.add("off");
                }
                return suggestions;
            }

            return Collections.emptyList();
        }
    }
}
