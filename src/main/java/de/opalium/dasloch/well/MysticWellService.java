package de.opalium.dasloch.well;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MysticWellService {

    public record RollResult(int tokensAwarded, String rarityRolled) {}

    private final Map<String, Integer> baseCosts = new HashMap<>();
    private final Map<String, MysticWellTier> tiers = new HashMap<>();
    private final Map<String, Double> modifiers = new HashMap<>();
    private final Logger logger = Logger.getLogger("DasLoch-Well");

    public MysticWellService(YamlConfiguration config) {
        load(config);
    }

    private void load(YamlConfiguration config) {
        baseCosts.clear();
        tiers.clear();
        modifiers.clear();

        ConfigurationSection base = config.getConfigurationSection("base_costs");
        if (base != null) {
            for (String key : base.getKeys(false)) {
                baseCosts.put(key, base.getInt(key));
            }
        }

        ConfigurationSection tierSection = config.getConfigurationSection("tiers");
        if (tierSection != null) {
            for (String tierId : tierSection.getKeys(false)) {
                ConfigurationSection tier = tierSection.getConfigurationSection(tierId);
                if (tier == null) {
                    continue;
                }
                List<Integer> tokenRange = tier.getIntegerList("token_range");
                int min = tokenRange.isEmpty() ? 0 : tokenRange.get(0);
                int max = tokenRange.size() > 1 ? tokenRange.get(1) : min;
                Map<String, Integer> rareLimits = new HashMap<>();
                ConfigurationSection rareSec = tier.getConfigurationSection("rare_limits");
                if (rareSec != null) {
                    for (String key : rareSec.getKeys(false)) {
                        rareLimits.put(key, rareSec.getInt(key));
                    }
                }
                Map<String, Double> probabilities = new HashMap<>();
                ConfigurationSection probSec = tier.getConfigurationSection("probability");
                if (probSec != null) {
                    for (String key : probSec.getKeys(false)) {
                        probabilities.put(key, probSec.getDouble(key));
                    }
                }
                tiers.put(tierId, new MysticWellTier(tierId, min, max, rareLimits, probabilities));
            }
        }

        ConfigurationSection modifierSec = config.getConfigurationSection("modifiers");
        if (modifierSec != null) {
            for (String key : modifierSec.getKeys(false)) {
                modifiers.put(key, modifierSec.getDouble(key));
            }
        }

        logger.info(() -> "Loaded " + tiers.size() + " Mystic Well tiers");
    }

    public Map<String, Integer> baseCosts() {
        return Collections.unmodifiableMap(baseCosts);
    }

    public MysticWellTier tier(String id) {
        return tiers.get(id);
    }

    public Map<String, Double> modifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    public RollResult roll(String tierId) {
        MysticWellTier tier = tiers.get(tierId);
        if (tier == null) {
            return new RollResult(0, "common");
        }

        int tokens = ThreadLocalRandom.current().nextInt(tier.tokenMin(), tier.tokenMax() + 1);
        String rarity = pickRarity(tier.probabilities());
        return new RollResult(tokens, rarity);
    }

    private String pickRarity(Map<String, Double> probabilities) {
        if (probabilities.isEmpty()) {
            return "common";
        }
        double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = ThreadLocalRandom.current().nextDouble(total);
        double cumulative = 0.0;
        for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                return entry.getKey();
            }
        }
        List<String> keys = new ArrayList<>(probabilities.keySet());
        return keys.isEmpty() ? "common" : keys.get(0);
    }
}
