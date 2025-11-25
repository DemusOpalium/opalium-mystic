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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DasLochPlugin extends JavaPlugin {

    // Config-Wrapper
    private ItemsConfig itemsConfig;
    private EnchantsConfig enchantsConfig;
    private WellConfig wellConfig;

    // Services / Factories
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

        // Services, die keine YAMLs brauchen
        this.lifeTokenService = new LifeTokenService(this);
        this.itemFactory = new ItemFactory(lifeTokenService);
        this.enchantParser = new EnchantParser(enchantsConfig);
        this.vaultService = new VaultService(this);

        // Alles laden und initialisieren
        reloadAll();
        registerCommands();
        registerListeners();
        registerPlaceholderApi();
    }

    /**
     * Lädt alle Configs und baut die Services neu.
     */
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

    /**
     * Registriert alle Befehle des Plugins.
     * Achtung: Signaturen exakt so, wie sie deine Klassen verlangen.
     */
    private void registerCommands() {
        // Zentrale Well-Command-Instanz für /mysticwell und /dasloch well ...
        MysticWellCommand wellCommand = new MysticWellCommand(
                itemsConfig,        // ItemsConfig
                lifeTokenService,   // LifeTokenService
                itemFactory,        // ItemFactory
                mysticWellService,  // MysticWellService
                vaultService        // VaultService
        );

        // /dasloch (Basis-Command mit /dasloch reload, /dasloch debug, /dasloch well …)
        PluginCommand dasloch = getCommand("dasloch");
        if (dasloch != null) {
            dasloch.setExecutor(new DasLochCommand(this, itemsConfig, lifeTokenService, wellCommand));
        }

        // /legendgive – erwartet MysticItemService (aus deinen letzten Fehlern)
        PluginCommand legend = getCommand("legendgive");
        if (legend != null) {
            LegendGiveCommand executor = new LegendGiveCommand(itemService);
            legend.setExecutor(executor);
            legend.setTabCompleter(executor);
        }

        // /mysticgive – erwartet ebenfalls MysticItemService
        PluginCommand mystic = getCommand("mysticgive");
        if (mystic != null) {
            MysticGiveCommand executor = new MysticGiveCommand(itemService);
            mystic.setExecutor(executor);
            mystic.setTabCompleter(executor);
        }

        // /mysticwell (direkter Zugriff auf den Brunnen)
        PluginCommand well = getCommand("mysticwell");
        if (well != null) {
            well.setExecutor(wellCommand);
            well.setTabCompleter(wellCommand);
        }
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(
                new ItemLifecycleListener(itemsConfig, lifeTokenService, itemFactory),
                this
        );
    }

    private void registerPlaceholderApi() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
        }
    }

    /**
     * Services anhand der frisch geladenen Configs neu aufbauen.
     * YAMLs werden hier direkt geladen, damit wir EnchantRegistry(YamlConfiguration)
     * und die Services korrekt füttern können.
     */
    private void reloadServices() {
        try {
            // Enchants für EnchantRegistry
            YamlConfiguration enchantsYaml = loadConfig("enchants.yml");
            this.enchantRegistry = new EnchantRegistry(enchantsYaml);

            // Brunnen-Config
            YamlConfiguration wellYaml = loadConfig("well.yml");
            this.mysticWellService = new MysticWellService(
                    this,
                    wellYaml,
                    enchantRegistry
            );

            // Items-Config für MysticItemService
            YamlConfiguration itemsYaml = loadConfig("items.yml");
            this.itemService = new MysticItemService(
                    this,
                    itemsYaml,
                    enchantRegistry,
                    mysticWellService
            );

        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to reload services", e);
        }
    }

    /**
     * Hilfs-Methode zum Laden einer YAML-Datei aus dem Plugin-Ordner.
     */
    private YamlConfiguration loadConfig(String name) throws IOException {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            // Kopiert die Ressource aus dem JAR, falls vorhanden
            saveResource(name, false);
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (InvalidConfigurationException ex) {
            throw new IOException("Invalid YAML config: " + name, ex);
        }
        return config;
    }

    // Getter für andere Klassen (falls du sie brauchst)

    public MysticItemService getItemService() {
        return itemService;
    }

    public MysticWellService getMysticWellService() {
        return mysticWellService;
    }

    public EnchantRegistry getEnchantRegistry() {
        return enchantRegistry;
    }

    public EnchantParser getEnchantParser() {
        return enchantParser;
    }

    public ItemsConfig getItemsConfig() {
        return itemsConfig;
    }

    public EnchantsConfig getEnchantsConfig() {
        return enchantsConfig;
    }

    public WellConfig getWellConfig() {
        return wellConfig;
    }

    public LifeTokenService getLifeTokenService() {
        return lifeTokenService;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    public VaultService getVaultService() {
        return vaultService;
    }
}
