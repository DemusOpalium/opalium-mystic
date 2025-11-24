package de.opalium.dasloch.model;

import java.util.Map;

public class MysticWellTier {
    private final String name;
    private final int tokenMin;
    private final int tokenMax;
    private final Map<EnchantDefinition.Rarity, Integer> rareLimits;
    private final Map<EnchantDefinition.Rarity, Double> probabilities;

    public MysticWellTier(String name, int tokenMin, int tokenMax,
                          Map<EnchantDefinition.Rarity, Integer> rareLimits,
                          Map<EnchantDefinition.Rarity, Double> probabilities) {
        this.name = name;
        this.tokenMin = tokenMin;
        this.tokenMax = tokenMax;
        this.rareLimits = rareLimits;
        this.probabilities = probabilities;
    }

    public String getName() {
        return name;
    }

    public int getTokenMin() {
        return tokenMin;
    }

    public int getTokenMax() {
        return tokenMax;
    }

    public Map<EnchantDefinition.Rarity, Integer> getRareLimits() {
        return rareLimits;
    }

    public Map<EnchantDefinition.Rarity, Double> getProbabilities() {
        return probabilities;
    }
}
