package de.opalium.dasloch.config;

import de.opalium.dasloch.model.EnchantDefinition;
import de.opalium.dasloch.model.ItemCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            if (enchantSection == null) {
                continue;
            }
            String displayName = enchantSection.getString("display-name", id);
            EnchantDefinition.Rarity rarity = EnchantDefinition.Rarity.fromString(enchantSection.getString("rarity", "COMMON"));

            List<ItemCategory> categories = new ArrayList<>();
            for (String cat : enchantSection.getStringList("applicable")) {
                categories.add(ItemCategory.fromString(cat));
            }

            int maxTier = enchantSection.getInt("max-tier", 1);
            Map<Integer, Integer> tokenValues = new HashMap<>();
            ConfigurationSection tokenSection = enchantSection.getConfigurationSection("token-values");
            if (tokenSection != null) {
                for (String tierKey : tokenSection.getKeys(false)) {
                    tokenValues.put(Integer.parseInt(tierKey), tokenSection.getInt(tierKey));
                }
            }

            EnchantDefinition definition = new EnchantDefinition(id, displayName, rarity, categories, maxTier, tokenValues);
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
