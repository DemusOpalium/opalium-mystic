package de.opalium.dasloch.config;

import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.item.ItemCategory;
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
            enchants = Collections.emptyMap();
            return;
        }

        Map<String, EnchantDefinition> loaded = new HashMap<>();

        for (String id : section.getKeys(false)) {
            ConfigurationSection es = section.getConfigurationSection(id);
            if (es == null) continue;

            String displayName = es.getString("display-name", id);
            EnchantDefinition.Rarity rarity =
                    EnchantDefinition.Rarity.fromString(es.getString("rarity", "COMMON"));

            Set<ItemCategory> applicable = new HashSet<>();
            for (String s : es.getStringList("applicable")) {
                applicable.add(ItemCategory.fromString(s));
            }

            int maxTier = es.getInt("max-tier", 1);

            Map<Integer, Integer> tokenValues = new HashMap<>();
            ConfigurationSection tSec = es.getConfigurationSection("token-values");
            if (tSec != null) {
                for (String t : tSec.getKeys(false)) {
                    tokenValues.put(Integer.parseInt(t), tSec.getInt(t));
                }
            }

            // WICHTIG: effects bleiben erstmal null
            EnchantDefinition def = new EnchantDefinition(
                    id,
                    displayName,
                    rarity,
                    applicable,
                    maxTier,
                    tokenValues,
                    null
            );

            loaded.put(id.toLowerCase(), def);
        }

        enchants = Collections.unmodifiableMap(loaded);
    }

    public Map<String, EnchantDefinition> getEnchants() {
        return enchants;
    }
}
