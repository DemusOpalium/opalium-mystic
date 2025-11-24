package de.opalium.dasloch.enchant;

import de.opalium.dasloch.item.ItemCategory;
import java.util.Map;
import java.util.Set;

public final class EnchantDefinition {

    public enum Rarity {
        COMMON,
        RARE
    }

    private final String id;
    private final String displayName;
    private final Rarity rarity;
    private final Set<ItemCategory> applicable;
    private final int maxTier;
    private final Map<Integer, Integer> tokenValues;
    private final EnchantEffects effects;

    public EnchantDefinition(String id, String displayName, Rarity rarity, Set<ItemCategory> applicable, int maxTier,
                             Map<Integer, Integer> tokenValues, EnchantEffects effects) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.applicable = applicable;
        this.maxTier = maxTier;
        this.tokenValues = tokenValues;
        this.effects = effects;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Rarity rarity() {
        return rarity;
    }

    public Set<ItemCategory> applicable() {
        return applicable;
    }

    public int maxTier() {
        return maxTier;
    }

    public Map<Integer, Integer> tokenValues() {
        return tokenValues;
    }

    public EnchantEffects effects() {
        return effects;
    }

    public int tokensForTier(int tier) {
        return tokenValues.getOrDefault(tier, 0);
    }
}
