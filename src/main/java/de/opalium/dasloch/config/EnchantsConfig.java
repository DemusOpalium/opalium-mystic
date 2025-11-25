package de.opalium.dasloch.config;

import de.opalium.dasloch.enchant.EnchantDefinition;   // FIX – neues Package
import de.opalium.dasloch.item.ItemCategory;            // FIX – item package
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EnchantsConfig {
    private final JavaPlugin plugin;
    private Map<String, EnchantDefinition> enchants = Collections.emptyMap();

    public EnchantsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() throws IOException {
        File file = new File(plugin.getDataFolder(), "enchants.yml");
        if (!file.exists()) {
            plugin.saveResource("enchants.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("enchants");

        if (section == null) {
            this.enchants = Collections.emptyMap();
            return;
        }

        Map<String, EnchantDefinition> loaded = new HashMap<>();

        for (String id : section.getKeys(false)) {
            ConfigurationSection enchantSection = section.getConfigurationSection(id);
            if (enchantSection == null) continue;

            String displayName = enchantSection.getString("display-name", id);

            // neue Rarity-Methode wird hier korrekt genutzt
            EnchantDefinition.Rarity rarity =
                    EnchantDefinition.Rarity.fromString(
                            enchantSection.getString("rarity", "COMMON")
                    );

            // applicable-Kategorien laden
            Set<ItemCategory> categories = new HashSet<>();
            for (String cat : enchantSection.getStringList("applicable")) {
                categories.add(ItemCategory.fromString(cat));
            }

            // Tiers
            int maxTier = enchantSection.getInt("max-tier", 1);

            // Token-Werte laden
            Map<Integer, Integer> tokenValues = new HashMap<>();
            ConfigurationSection tokenSection = enchantSection.getConfigurationSection("token-values");

            if (tokenSection != null) {
                for (String tierKey : tokenSection.getKeys(false)) {
                    try {
                        tokenValues.put(
                                Integer.parseInt(tierKey),
                                tokenSection.getInt(tierKey)
                        );
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Effekte laden
            EnchantEffectsConfig effectsConfig = new EnchantEffectsConfig(enchantSection);
            var effects = effectsConfig.loadEffects();

            // Finales Enchant-Objekt
            EnchantDefinition definition = new EnchantDefinition(
                    id,
                    displayName,
                    null,       // description (optional)
                    null,       // lore (optional)
                    rarity,
                    categories,
                    maxTier,
                    tokenValues,
                    effects
            );

            loaded.put(id.toLowerCase(), definition);
        }

        this.enchants = Collections.unmodifiableMap(loaded);
    }

    public Optional<EnchantDefinition> getEnchant(String id) {
        return Optional.ofNullable(enchants.get(id.toLowerCase()));
    }

    public Map<String, EnchantDefinition> getEnchants() {
        return enchants;
    }
}
