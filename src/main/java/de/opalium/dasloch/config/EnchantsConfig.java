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

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = cfg.getConfigurationSection("enchants");

        if (root == null) {
            enchants = Collections.emptyMap();
            return;
        }

        Map<String, EnchantDefinition> map = new HashMap<>();

        for (String id : root.getKeys(false)) {

            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;

            String display = sec.getString("display-name", id);

            EnchantDefinition.Rarity rarity =
                    EnchantDefinition.Rarity.fromString(sec.getString("rarity", "COMMON"));

            Set<ItemCategory> applicable = new HashSet<>();
            for (String s : sec.getStringList("applicable")) {
                applicable.add(ItemCategory.fromString(s));
            }

            int maxTier = sec.getInt("max-tier", 1);

            Map<Integer, Integer> tokenValues = new HashMap<>();
            ConfigurationSection tokenSec = sec.getConfigurationSection("token-values");
            if (tokenSec != null) {
                for (String t : tokenSec.getKeys(false)) {
                    tokenValues.put(Integer.parseInt(t), tokenSec.getInt(t));
                }
            }

            // → effects bleibt vorerst NULL
            EnchantDefinition def = new EnchantDefinition(
                    id,
                    display,
                    rarity,
                    applicable,
                    maxTier,
                    tokenValues,
                    null
            );

            map.put(id.toLowerCase(), def);
        }

        enchants = Collections.unmodifiableMap(map);
    }

    public Map<String, EnchantDefinition> getEnchants() {
        return enchants;
    }
}
