package de.opalium.dasloch.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EnchantDefinition {
    public enum Rarity {
        COMMON,
        RARE;

        public static Rarity fromString(String input) {
            for (Rarity rarity : values()) {
                if (rarity.name().equalsIgnoreCase(input)) {
                    return rarity;
                }
            }
            throw new IllegalArgumentException("Unknown rarity: " + input);
        }
    }

    private final String id;
    private final String displayName;
    private final Rarity rarity;
    private final List<ItemCategory> applicableCategories;
    private final int maxTier;
    private final Map<Integer, Integer> tokenValues;

    public EnchantDefinition(String id, String displayName, Rarity rarity, List<ItemCategory> applicableCategories,
                             int maxTier, Map<Integer, Integer> tokenValues) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.applicableCategories = Collections.unmodifiableList(applicableCategories);
        this.maxTier = maxTier;
        this.tokenValues = Collections.unmodifiableMap(tokenValues);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public List<ItemCategory> getApplicableCategories() {
        return applicableCategories;
    }

    public int getMaxTier() {
        return maxTier;
    }

    public Map<Integer, Integer> getTokenValues() {
        return tokenValues;
    }
}
