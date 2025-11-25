package de.opalium.dasloch.enchant;

import de.opalium.dasloch.item.ItemCategory;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class EnchantRegistry {

    private final Map<String, EnchantDefinition> enchants = new HashMap<>();
    private final Logger logger = Logger.getLogger("DasLoch-Enchants");

    public EnchantRegistry(YamlConfiguration config) {
        load(config);
    }

    private void load(YamlConfiguration config) {
        enchants.clear();
        ConfigurationSection root = config.getConfigurationSection("enchants");
        if (root == null) {
            logger.warning("No enchants defined in enchants.yml");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }

            String displayName = section.getString("display-name", id);

            EnchantDefinition.Rarity rarity = EnchantDefinition.Rarity.fromString(
                    section.getString("rarity", "COMMON")
            );

            Set<ItemCategory> applicable = new HashSet<>();
            for (String cat : section.getStringList("applicable")) {
                try {
                    applicable.add(ItemCategory.fromString(cat));
                } catch (IllegalArgumentException ex) {
                    logger.warning("Unknown item category '" + cat + "' for enchant '" + id + "'");
                }
            }

            int maxTier = section.getInt("max-tier", 1);

            Map<Integer, Integer> tokenValues = new HashMap<>();
            ConfigurationSection tokenSection = section.getConfigurationSection("token-values");
            if (tokenSection != null) {
                for (String tierKey : tokenSection.getKeys(false)) {
                    try {
                        int tier = Integer.parseInt(tierKey);
                        tokenValues.put(tier, tokenSection.getInt(tierKey));
                    } catch (NumberFormatException ex) {
                        logger.warning("Invalid tier key '" + tierKey + "' in token-values of enchant '" + id + "'");
                    }
                }
            }

            EnchantEffects effects = parseEffects(section.getConfigurationSection("effects"));
            EnchantDefinition def = new EnchantDefinition(
                    id,
                    displayName,
                    rarity,
                    applicable,
                    maxTier,
                    tokenValues,
                    effects
            );
            enchants.put(id, def);
        }
        logger.info(() -> "Loaded " + enchants.size() + " enchants");
    }

    private EnchantEffects parseEffects(ConfigurationSection section) {
        if (section == null) {
            return new EnchantEffects(
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    0,
                    Collections.emptyMap()
            );
        }

        Map<Integer, Integer> healPercent = readTierMap(section, "on-melee-hit.heal-percent");
        Map<Integer, Integer> goldOnKill = readTierMap(section, "on-kill.extra-gold");
        Map<Integer, Integer> xpOnKill = readTierMap(section, "on-kill.extra-xp-percent");
        Map<Integer, Integer> streakBonus = readTierMap(section, "passive.streak-bonus-percent");
        Map<Integer, Integer> bowDamage = readTierMap(section, "on-bow-hit.extra-damage-percent");
        Map<Integer, Integer> lastStandReduction = readTierMap(section, "on-damage-taken.damage-reduction-percent");
        int lastStandThreshold = section.getInt("on-damage-taken.threshold-hearts", 0);

        return new EnchantEffects(
                healPercent,
                goldOnKill,
                xpOnKill,
                streakBonus,
                bowDamage,
                lastStandThreshold,
                lastStandReduction
        );
    }

    private Map<Integer, Integer> readTierMap(ConfigurationSection section, String path) {
        ConfigurationSection node = section.getConfigurationSection(path);
        if (node == null) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> values = new HashMap<>();
        for (String key : node.getKeys(false)) {
            try {
                int tier = Integer.parseInt(key);
                values.put(tier, node.getInt(key));
            } catch (NumberFormatException ex) {
                logger.warning("Invalid tier key '" + key + "' at path '" + path + "'");
            }
        }
        return values;
    }

    public EnchantDefinition get(String id) {
        return enchants.get(id);
    }

    public Map<String, EnchantDefinition> all() {
        return Collections.unmodifiableMap(enchants);
    }
}
