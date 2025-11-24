package de.opalium.dasloch.model;

import java.util.Map;

public class MysticWellRollResult {
    private final String tierName;
    private final int goldCost;
    private final int tokens;
    private final Map<String, Integer> enchants; // enchant id -> tier

    public MysticWellRollResult(String tierName, int goldCost, int tokens, Map<String, Integer> enchants) {
        this.tierName = tierName;
        this.goldCost = goldCost;
        this.tokens = tokens;
        this.enchants = enchants;
    }

    public String getTierName() {
        return tierName;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public int getTokens() {
        return tokens;
    }

    public Map<String, Integer> getEnchants() {
        return enchants;
    }
}
