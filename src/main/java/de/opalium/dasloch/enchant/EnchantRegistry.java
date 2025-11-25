package de.opalium.dasloch.enchant;

import de.opalium.dasloch.item.ItemCategory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EnchantDefinition {

    public enum Rarity {
        COMMON,
        RARE;

        public static Rarity fromString(String input) {
            if (input == null) {
                return COMMON;
            }
            for (Rarity rarity : values()) {
                if (rarity.name().equalsIgnoreCase(input)) {
                    return rarity;
                }
            }
            return COMMON;
        }
    }

    private final String id;
    private final String displayName;
    private final Rarity rarity;
    private final List<ItemCategory> applicableCategories;
    private final int maxTier;
    private final Map<Integer, Integer> tokenValues;
    private final EnchantEffects effects;

    public EnchantDefinition(String id,
                             String displayName,
                             Rarity rarity,
                             Set<ItemCategory> applicable,
                             int maxTier,
                             Map<Integer, Integer> tokenValues,
                             EnchantEffects effects) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        // als Liste speichern, weil andere Klassen getApplicableCategories() als List nutzen
        this.applicableCategories = List.copyOf(applicable);
        this.maxTier = maxTier;
        this.tokenValues = Collections.unmodifiableMap(tokenValues != null ? tokenValues : Collections.emptyMap());
        this.effects = effects;
    }

    // Kompatible Getter zur bestehenden Codebasis

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

    public EnchantEffects getEffects() {
        return effects;
    }

    /** Tokens, die für eine bestimmte Tier-Stufe vorgesehen sind (z.B. für Anzeige/Balancing). */
    public int getTokensForTier(int tier) {
        return tokenValues.getOrDefault(tier, 0);
    }

    /** Hilfsmethode: passt dieses Enchant überhaupt auf die Item-Kategorie? */
    public boolean isApplicableTo(ItemCategory category) {
        return applicableCategories.contains(category);
    }
}
