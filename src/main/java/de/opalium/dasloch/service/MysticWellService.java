package de.opalium.dasloch.service;

import de.opalium.dasloch.config.EnchantsConfig;
import de.opalium.dasloch.config.WellConfig;
import de.opalium.dasloch.model.EnchantDefinition;
import de.opalium.dasloch.model.ItemCategory;
import de.opalium.dasloch.model.MysticWellRollResult;
import de.opalium.dasloch.model.MysticWellTier;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class MysticWellService {
    private final WellConfig wellConfig;
    private final EnchantsConfig enchantsConfig;
    private final Random random = new SecureRandom();

    public MysticWellService(WellConfig wellConfig, EnchantsConfig enchantsConfig) {
        this.wellConfig = wellConfig;
        this.enchantsConfig = enchantsConfig;
    }

    public Optional<MysticWellRollResult> roll(ItemCategory category, String tierName) {
        Optional<MysticWellTier> tierOptional = wellConfig.getTier(tierName);
        if (tierOptional.isEmpty()) {
            return Optional.empty();
        }
        MysticWellTier tier = tierOptional.get();
        int tokens = random.nextInt(tier.getTokenMax() - tier.getTokenMin() + 1) + tier.getTokenMin();
        Map<String, Integer> enchantResults = new HashMap<>();

        EnchantDefinition.Rarity pickedRarity = pickRarity(tier);
        List<EnchantDefinition> candidates = enchantsConfig.getEnchants().values().stream()
            .filter(def -> def.getApplicableCategories().contains(category))
            .filter(def -> def.getRarity() == pickedRarity)
            .toList();

        if (candidates.isEmpty()) {
            candidates = enchantsConfig.getEnchants().values().stream()
                .filter(def -> def.getApplicableCategories().contains(category))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        if (!candidates.isEmpty()) {
            EnchantDefinition selected = candidates.get(random.nextInt(candidates.size()));
            int tierValue = Math.min(selected.getMaxTier(), Math.max(1, tokens));
            enchantResults.put(selected.getId(), tierValue);
        }

        int goldCost = resolveCost(tier.getName());
        return Optional.of(new MysticWellRollResult(tier.getName(), goldCost, tokens, enchantResults));
    }

    private int resolveCost(String tierName) {
        String key = switch (tierName.toUpperCase()) {
            case "I" -> "tier_1";
            case "II" -> "tier_2";
            case "III" -> "tier_3";
            default -> tierName.toLowerCase();
        };
        return wellConfig.getBaseCosts().getOrDefault(key, 0);
    }

    private EnchantDefinition.Rarity pickRarity(MysticWellTier tier) {
        double roll = random.nextDouble();
        double cumulative = 0;
        for (EnchantDefinition.Rarity rarity : List.of(EnchantDefinition.Rarity.COMMON, EnchantDefinition.Rarity.RARE)) {
            double weight = tier.getProbabilities().getOrDefault(rarity, rarity == EnchantDefinition.Rarity.COMMON ? 1.0 : 0.0);
            cumulative += weight;
            if (roll <= cumulative) {
                return rarity;
            }
        }
        return EnchantDefinition.Rarity.COMMON;
    }
}
