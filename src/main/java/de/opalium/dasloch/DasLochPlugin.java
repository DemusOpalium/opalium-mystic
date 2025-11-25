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

        // Alles laden und initialisieren
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

    /**
     * Registriert alle Befehle des Plugins.
     * Nutzt durchgängig MysticItemService, damit die Give-Commands
     * und der Brunnen dieselbe Logik verwenden.
     */
    private void registerCommands() {
        // Zentrale Well-Command-Instanz für /mysticwell und /dasloch well ...
        MysticWellCommand wellCommand = new MysticWellCommand(
                itemService,          // MysticItemService
                mysticWellService,    // MysticWellService
                vaultService          // VaultService
        );

        // /dasloch (Basis-Command mit /dasloch reload, /dasloch debug, /dasloch well …)
        PluginCommand dasloch = getCommand("dasloch");
        if (dasloch != null) {
            dasloch.setExecutor(new DasLochCommand(this, itemService, wellCommand));
        }

        // /legendgive
        PluginCommand legend = getCommand("legendgive");
        if (legend != null) {
            LegendGiveCommand executor = new LegendGiveCommand(itemService);
            legend.setExecutor(executor);
            legend.setTabCompleter(executor); // falls Tab-Completion implementiert
        }

        // /mysticgive
        PluginCommand mystic = getCommand("mysticgive");
        if (mystic != null) {
            MysticGiveCommand executor = new MysticGiveCommand(itemService);
            mystic.setExecutor(executor);
            mystic.setTabCompleter(executor); // falls Tab-Completion implementiert
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
        pm.registerEvents(new ItemLifecycleListener(itemsConfig, lifeTokenService, itemFactory), this);
    }

    private void registerPlaceholderApi() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
        }
    }

    /**
     * Services anhand der frisch geladenen Configs neu aufbauen.
     * Wird von reloadAll() aufgerufen.
     */
    private void reloadServices() {
        // Enchant-Registry neu erstellen
        this.enchantRegistry = new EnchantRegistry();

        // Brunnen-Service
        this.mysticWellService = new MysticWellService(
                this,
                wellConfig.getConfig(),
                enchantRegistry
        );

        // Item-Service (Legends + Mystics)
        this.itemService = new MysticItemService(
            this,
            itemsConfig.getConfig(),
            enchantRegistry,
            mysticWellService
        );
    }

    /**
     * Hilfs-Methode zum Laden einer YAML-Datei aus dem Plugin-Ordner.
     * (Falls du sie nicht mehr brauchst, kannst du sie auch löschen.)
     */
    private YamlConfiguration loadConfig(String name) throws IOException {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (org.bukkit.configuration.InvalidConfigurationException ex) {
            throw new IOException("Invalid YAML config: " + name, ex);
        }
        return config;
    }

    // Getter für andere Klassen

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
