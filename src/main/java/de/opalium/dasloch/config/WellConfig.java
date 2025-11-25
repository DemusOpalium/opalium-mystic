package de.opalium.dasloch.config;

import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.model.MysticWellTier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WellConfig {
    private final JavaPlugin plugin;
    private Map<String, Integer> baseCosts = Collections.emptyMap();
    private Map<String, MysticWellTier> tiers = Collections.emptyMap();
    private Map<String, Double> modifiers = Collections.emptyMap();

    public WellConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() throws IOException {
        File file = new File(plugin.getDataFolder(), "well.yml");
        if (!file.exists()) {
            plugin.saveResource("well.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.baseCosts = parseIntMap(config.getConfigurationSection("base_costs"));
        this.modifiers = parseDoubleMap(config.getConfigurationSection("modifiers"));

        Map<String, MysticWellTier> loadedTiers = new HashMap<>();
        ConfigurationSection tierSection = config.getConfigurationSection("tiers");
        if (tierSection != null) {
            for (String tierName : tierSection.getKeys(false)) {
                ConfigurationSection section = tierSection.getConfigurationSection(tierName);
                if (section == null) {
                    continue;
                }

                int tokenMin = section.getIntegerList("token_range").stream().findFirst().orElse(1);
                int tokenMax = section.getIntegerList("token_range").stream().skip(1).findFirst().orElse(tokenMin);

                Map<EnchantDefinition.Rarity, Integer> rareLimits = new HashMap<>();
                ConfigurationSection rareSection = section.getConfigurationSection("rare_limits");
                if (rareSection != null) {
                    for (String key : rareSection.getKeys(false)) {
                        EnchantDefinition.Rarity rarity = parseRarity(key);
                        rareLimits.put(rarity, rareSection.getInt(key));
                    }
                }

                Map<EnchantDefinition.Rarity, Double> probabilities = new HashMap<>();
                ConfigurationSection probSection = section.getConfigurationSection("probability");
                if (probSection != null) {
                    for (String key : probSection.getKeys(false)) {
                        EnchantDefinition.Rarity rarity = parseRarity(key);
                        probabilities.put(rarity, probSection.getDouble(key));
                    }
                }

                MysticWellTier tier = new MysticWellTier(
                        tierName,
                        tokenMin,
                        tokenMax,
                        rareLimits,
                        probabilities
                );
                loadedTiers.put(tierName.toUpperCase(), tier);
            }
        }

        this.tiers = Collections.unmodifiableMap(loadedTiers);
    }

    private Map<String, Integer> parseIntMap(ConfigurationSection section) {
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, Integer> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            map.put(key, section.getInt(key));
        }
        return map;
    }

    private Map<String, Double> parseDoubleMap(ConfigurationSection section) {
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, Double> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            map.put(key, section.getDouble(key));
        }
        return map;
    }

    private EnchantDefinition.Rarity parseRarity(String id) {
        if ("regular".equalsIgnoreCase(id) || "common".equalsIgnoreCase(id)) {
            return EnchantDefinition.Rarity.COMMON;
        }
        return EnchantDefinition.Rarity.fromString(id);
    }

    public Map<String, Integer> getBaseCosts() {
        return baseCosts;
    }

    public Map<String, MysticWellTier> getTiers() {
        return tiers;
    }

    public Optional<MysticWellTier> getTier(String name) {
        return Optional.ofNullable(tiers.get(name.toUpperCase()));
    }

    public Map<String, Double> getModifiers() {
        return modifiers;
    }
}
