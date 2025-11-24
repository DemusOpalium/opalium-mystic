package de.opalium.dasloch;

import de.opalium.dasloch.enchant.EnchantRegistry;
import de.opalium.dasloch.integration.PlaceholderHook;
import de.opalium.dasloch.integration.VaultService;
import de.opalium.dasloch.item.MysticItemService;
import de.opalium.dasloch.listener.CombatListener;
import de.opalium.dasloch.listener.ItemLifecycleListener;
import de.opalium.dasloch.well.MysticWellService;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DasLochPlugin extends JavaPlugin {

    private MysticItemService itemService;
    private EnchantRegistry enchantRegistry;
    private MysticWellService wellService;
    private VaultService vaultService;

    @Override
    public void onEnable() {
        getLogger().info("DasLoch plugin enabling...");
        saveDefaultResources();
        reloadSystems();
        registerCommands();
        registerListeners();
        registerPlaceholderHook();
    }

    @Override
    public void onDisable() {
        getLogger().info("DasLoch plugin disabled.");
    }

    public MysticItemService getItemService() {
        return itemService;
    }

    public EnchantRegistry getEnchantRegistry() {
        return enchantRegistry;
    }

    public MysticWellService getWellService() {
        return wellService;
    }

    public VaultService getVaultService() {
        return vaultService;
    }

    public void reloadSystems() {
        File itemsFile = new File(getDataFolder(), "items.yml");
        File enchantsFile = new File(getDataFolder(), "enchants.yml");
        File wellFile = new File(getDataFolder(), "well.yml");

        YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        YamlConfiguration enchantConfig = YamlConfiguration.loadConfiguration(enchantsFile);
        YamlConfiguration wellConfig = YamlConfiguration.loadConfiguration(wellFile);

        enchantRegistry = new EnchantRegistry(enchantConfig);
        wellService = new MysticWellService(wellConfig);
        itemService = new MysticItemService(this, itemsConfig, enchantRegistry, wellService);
        vaultService = new VaultService(this);
    }

    private void saveDefaultResources() {
        saveResource("items.yml", false);
        saveResource("enchants.yml", false);
        saveResource("well.yml", false);
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

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new ItemLifecycleListener(this), this);
    }

    private void registerPlaceholderHook() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            getLogger().info("Hooked into PlaceholderAPI");
        }
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
                    if (sender.getServer().getPluginManager().getPlugin("DasLoch") instanceof DasLochPlugin plugin) {
                        plugin.reloadSystems();
                    }
                    sender.sendMessage(ChatColor.GREEN + "Reloaded DasLoch configs.");
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
