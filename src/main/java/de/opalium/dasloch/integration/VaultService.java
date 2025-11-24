package de.opalium.dasloch.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultService {

    private final Economy economy;

    public VaultService(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
            this.economy = provider == null ? null : provider.getProvider();
        } else {
            this.economy = null;
        }
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public void deposit(Player player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    public double getBalance(Player player) {
        return economy == null ? 0 : economy.getBalance(player);
    }
}
