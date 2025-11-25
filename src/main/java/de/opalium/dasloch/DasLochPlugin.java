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

private void registerCommands(
        ItemsConfig itemsConfig,
        LifeTokenService lifeTokenService,
        de.opalium.dasloch.service.ItemFactory itemFactory,
        de.opalium.dasloch.service.MysticWellService mysticWellService,
        de.opalium.dasloch.integration.VaultService vaultService
) {
    // Zentrale Well-Command-Instanz (für /mysticwell und /dasloch well …)
    MysticWellCommand wellCommand = new MysticWellCommand(
            itemsConfig,
            lifeTokenService,
            itemFactory,
            mysticWellService,
            vaultService
    );

    // /legendgive
    var legend = getCommand("legendgive");
    if (legend != null) {
        LegendGiveCommand executor = new LegendGiveCommand(itemsConfig, itemFactory);
        legend.setExecutor(executor);
    }

    // /mysticgive
    var mystic = getCommand("mysticgive");
    if (mystic != null) {
        MysticGiveCommand executor = new MysticGiveCommand(itemsConfig, itemFactory);
        mystic.setExecutor(executor);
    }

    // /dasloch
    var dasloch = getCommand("dasloch");
    if (dasloch != null) {
        dasloch.setExecutor(new DasLochCommand(this, itemsConfig, lifeTokenService, wellCommand));
    }

    // /mysticwell (Direkter Zugriff auf den Brunnen)
    var well = getCommand("mysticwell");
    if (well != null) {
        well.setExecutor(wellCommand);
        well.setTabCompleter(wellCommand);
    }
}
