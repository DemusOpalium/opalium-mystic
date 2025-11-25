package de.opalium.dasloch;

import de.opalium.dasloch.command.DasLochCommand;
import de.opalium.dasloch.command.LegendGiveCommand;
import de.opalium.dasloch.command.MysticGiveCommand;
import de.opalium.dasloch.command.MysticWellCommand;
import de.opalium.dasloch.config.EnchantsConfig;
import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.config.WellConfig;
import de.opalium.dasloch.enchant.EnchantRegistry;
import de.opalium.dasloch.integration.PlaceholderHook;
import de.opalium.dasloch.integration.VaultService;
import de.opalium.dasloch.item.MysticItemService;
import de.opalium.dasloch.listener.CombatListener;
import de.opalium.dasloch.listener.ItemLifecycleListener;
import de.opalium.dasloch.service.EnchantParser;
import de.opalium.dasloch.service.ItemFactory;
import de.opalium.dasloch.service.LifeTokenService;
import de.opalium.dasloch.well.MysticWellService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DasLochPlugin extends JavaPlugin {

    private ItemsConfig itemsConfig;
    private EnchantsConfig enchantsConfig;
    private WellConfig wellConfig;

    private LifeTokenService lifeTokenService;
    private ItemFactory itemFactory;
    private EnchantParser enchantParser;

    private MysticWellService mysticWellService;
    private EnchantRegistry enchantRegistry;
    private MysticItemService itemService;

    private VaultService vaultService;

    @Override
    public void onEnable() {
        // Standard-Config sichern
        saveDefaultConfig();

        // Konfig-Wrapper
        this.itemsConfig = new ItemsConfig(this);
        this.enchantsConfig = new EnchantsConfig(this);
        this.wellConfig = new WellConfig(this);

        // Services
        this.lifeTokenService = new LifeTokenService(this);
        this.itemFactory = new ItemFactory(lifeTokenService);
        this.enchantParser = new EnchantParser(enchantsConfig);
        this.vaultService = new VaultService(this);

        // Alles laden und registrieren
        reloadAll();
        registerCommands();
        registerListeners();
        registerPlaceholderApi();
    }

    public void reloadAll() {
        try {
            itemsConfig.load();
            enchantsConfig.load();
            wellConfig.load();
            reloadConfig();
            reloadServices();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to load configuration", ex);
        }
    }

    private void registerCommands() {
        // Zentrale MysticWellCommand-Instanz, die wir auch im /dasloch-Command benutzen
        MysticWellCommand wellCommand = new MysticWellCommand(
                itemsConfig,
                lifeTokenService,
                itemFactory,
                mysticWellService,
                vaultService
        );

        // /legendgive
        PluginCommand legend = getCommand("legendgive");
        if (legend != null) {
            LegendGiveCommand executor = new LegendGiveCommand(itemFactory);
            legend.setExecutor(executor);
            legend.setTabCompleter(executor);
        }

        // /mysticgive
        PluginCommand mystic = getCommand("mysticgive");
        if (mystic != null) {
            MysticGiveCommand executor = new MysticGiveCommand(itemFactory);
            mystic.setExecutor(executor);
            mystic.setTabCompleter(executor);
        }

        // /mysticwell
        PluginCommand mysticWell = getCommand("mysticwell");
        if (mysticWell != null) {
            mysticWell.setExecutor(wellCommand);
            mysticWell.setTabCompleter(wellCommand);
        }

        // /dasloch (Basis-Command mit /dasloch reload, /dasloch debug, /dasloch well …)
        PluginCommand dasloch = getCommand("dasloch");
        if (dasloch != null) {
            dasloch.setExecutor(new DasLochCommand(this, itemsConfig, lifeTokenService, wellCommand));
        }
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        // Item-Lifecycle (Leben / Tokens etc.)
        pm.registerEvents(new ItemLifecycleListener(itemsConfig, lifeTokenService, itemFactory), this);

        // Combat-Effekte (Mystic-Enchants usw.)
        pm.registerEvents(new CombatListener(this), this);
    }

    private void registerPlaceholderApi() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
        }
    }

    /**
     * Lädt die YAML-Konfigs in die eigentlichen Services
     * (Mystic-Item-System, Well-System, Enchant-Registry).
     */
    private void reloadServices() {
        File itemsFile = new File(getDataFolder(), "items.yml");
        YamlConfiguration itemsYaml = YamlConfiguration.loadConfiguration(itemsFile);

        File enchantsFile = new File(getDataFolder(), "enchants.yml");
        YamlConfiguration enchantsYaml = YamlConfiguration.loadConfiguration(enchantsFile);

        File wellFile = new File(getDataFolder(), "well.yml");
        YamlConfiguration wellYaml = YamlConfiguration.loadConfiguration(wellFile);

        // Enchant-Registry
        this.enchantRegistry = new EnchantRegistry(enchantsYaml);

        // Mystic-Well-Service (neue Implementation im well-Package)
        this.mysticWellService = new MysticWellService(wellYaml);

        // Mystic-Item-Service, nutzt Registry + Well
        this.itemService = new MysticItemService(this, itemsYaml, enchantRegistry, mysticWellService);
    }

    public MysticWellService getMysticWellService() {
        return mysticWellService;
    }

    public VaultService getVaultService() {
        return vaultService;
    }

    public EnchantRegistry getEnchantRegistry() {
        return enchantRegistry;
    }

    public MysticItemService getItemService() {
        return itemService;
    }
}
