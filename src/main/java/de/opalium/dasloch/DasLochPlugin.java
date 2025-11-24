package de.opalium.dasloch;

import de.opalium.dasloch.command.DasLochCommand;
import de.opalium.dasloch.command.LegendGiveCommand;
import de.opalium.dasloch.command.MysticGiveCommand;
import de.opalium.dasloch.config.EnchantsConfig;
import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.config.WellConfig;
import de.opalium.dasloch.listener.ItemLifecycleListener;
import de.opalium.dasloch.service.EnchantParser;
import de.opalium.dasloch.service.ItemFactory;
import de.opalium.dasloch.service.LifeTokenService;
import de.opalium.dasloch.service.MysticWellService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public class DasLochPlugin extends JavaPlugin {
    private ItemsConfig itemsConfig;
    private EnchantsConfig enchantsConfig;
    private WellConfig wellConfig;
    private LifeTokenService lifeTokenService;
    private ItemFactory itemFactory;
    private EnchantParser enchantParser;
    private MysticWellService mysticWellService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.itemsConfig = new ItemsConfig(this);
        this.enchantsConfig = new EnchantsConfig(this);
        this.wellConfig = new WellConfig(this);
        this.lifeTokenService = new LifeTokenService(this);
        this.itemFactory = new ItemFactory(lifeTokenService);
        this.enchantParser = new EnchantParser(enchantsConfig);
        this.mysticWellService = new MysticWellService(wellConfig, enchantsConfig);

        reloadAll();
        registerCommands();
        getServer().getPluginManager().registerEvents(
            new ItemLifecycleListener(itemsConfig, lifeTokenService, itemFactory), this
        );
    }

    public void reloadAll() {
        try {
            itemsConfig.load();
            enchantsConfig.load();
            wellConfig.load();
            reloadConfig();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to load configuration", ex);
        }
    }

    private void registerCommands() {
        PluginCommand legend = getCommand("legendgive");
        if (legend != null) {
            LegendGiveCommand executor = new LegendGiveCommand(itemsConfig, itemFactory);
            legend.setExecutor(executor);
            legend.setTabCompleter(executor);
        }
        PluginCommand mystic = getCommand("mysticgive");
        if (mystic != null) {
            MysticGiveCommand executor = new MysticGiveCommand(itemsConfig, itemFactory);
            mystic.setExecutor(executor);
            mystic.setTabCompleter(executor);
        }
        PluginCommand dasloch = getCommand("dasloch");
        if (dasloch != null) {
            dasloch.setExecutor(new DasLochCommand(this, itemsConfig, lifeTokenService));
        }
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

    public EnchantParser getEnchantParser() {
        return enchantParser;
    }

    public MysticWellService getMysticWellService() {
        return mysticWellService;
    }
}
