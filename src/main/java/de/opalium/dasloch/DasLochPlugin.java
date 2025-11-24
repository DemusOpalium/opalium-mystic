package de.opalium.dasloch;

import de.opalium.dasloch.command.DasLochCommand;
import de.opalium.dasloch.command.LegendGiveCommand;
import de.opalium.dasloch.command.MysticGiveCommand;
import de.opalium.dasloch.command.MysticWellCommand;
import de.opalium.dasloch.config.EnchantsConfig;
import de.opalium.dasloch.config.WellConfig;
import de.opalium.dasloch.enchant.EnchantRegistry;
import de.opalium.dasloch.integration.PlaceholderHook;
import de.opalium.dasloch.integration.VaultService;
import de.opalium.dasloch.item.MysticItemService;
import de.opalium.dasloch.listener.CombatListener;
import de.opalium.dasloch.service.EnchantParser;
import de.opalium.dasloch.well.MysticWellService;
import java.io.File;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public class DasLochPlugin extends JavaPlugin {
    private EnchantsConfig enchantsConfig;
    private WellConfig wellConfig;
    private EnchantParser enchantParser;
    private MysticWellService mysticWellService;
    private EnchantRegistry enchantRegistry;
    private MysticItemService itemService;
    private VaultService vaultService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.enchantsConfig = new EnchantsConfig(this);
        this.wellConfig = new WellConfig(this);
        this.vaultService = new VaultService(this);
        this.enchantParser = new EnchantParser(enchantsConfig);

        reloadAll();
        registerCommands();
        registerListeners();
        registerPlaceholderApi();
    }

    public void reloadAll() {
        try {
            enchantsConfig.load();
            wellConfig.load();
            reloadConfig();
            reloadServices();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to load configuration", ex);
        }
    }

    private void registerCommands() {
        MysticWellCommand wellCommand = new MysticWellCommand(itemService, mysticWellService, vaultService);

        PluginCommand legend = getCommand("legendgive");
        if (legend != null) {
            LegendGiveCommand executor = new LegendGiveCommand(itemService);
            legend.setExecutor(executor);
            legend.setTabCompleter(executor);
        }
        PluginCommand mystic = getCommand("mysticgive");
        if (mystic != null) {
            MysticGiveCommand executor = new MysticGiveCommand(itemService);
            mystic.setExecutor(executor);
            mystic.setTabCompleter(executor);
        }
        PluginCommand mysticWell = getCommand("mysticwell");
        if (mysticWell != null) {
            mysticWell.setExecutor(wellCommand);
            mysticWell.setTabCompleter(wellCommand);
        }
        PluginCommand dasloch = getCommand("dasloch");
        if (dasloch != null) {
            dasloch.setExecutor(new DasLochCommand(this, itemService, wellCommand));
        }
    }

    private void registerListeners() {
        PluginManager plugins = getServer().getPluginManager();
        plugins.registerEvents(new CombatListener(this), this);
    }

    private void registerPlaceholderApi() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
        }
    }

    private void reloadServices() throws IOException {
        YamlConfiguration itemConfig = loadConfig("items.yml");
        YamlConfiguration enchantConfig = loadConfig("enchants.yml");
        YamlConfiguration wellConfig = loadConfig("well.yml");

        this.enchantRegistry = new EnchantRegistry(enchantConfig);
        this.mysticWellService = new MysticWellService(wellConfig);
        this.itemService = new MysticItemService(this, itemConfig, enchantRegistry, mysticWellService);
    }

    private YamlConfiguration loadConfig(String name) throws IOException {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public EnchantsConfig getEnchantsConfig() {
        return enchantsConfig;
    }

    public WellConfig getWellConfig() {
        return wellConfig;
    }

    public EnchantParser getEnchantParser() {
        return enchantParser;
    }

    public MysticWellService getMysticWellService() {
        return mysticWellService;
    }

    public EnchantRegistry getEnchantRegistry() {
        return enchantRegistry;
    }

    public MysticItemService getItemService() {
        return itemService;
    }

    public VaultService getVaultService() {
        return vaultService;
    }
}
