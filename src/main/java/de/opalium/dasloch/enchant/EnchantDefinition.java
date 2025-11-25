package de.opalium.dasloch.enchant;

import de.opalium.dasloch.item.ItemCategory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class EnchantDefinition {

    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY;

        public static Rarity fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Rarity value cannot be null");
            }
            String normalized = value.trim().toUpperCase();
            try {
                return Rarity.valueOf(normalized);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Invalid rarity value: " + value +
                        ". Allowed: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY"
                );
            }
        }
    }

    private final String id;
    private final String displayName;
    private final String description;
    private final List<String> lore;
    private final Rarity rarity;
    private final Set<ItemCategory> applicable;
    private final int maxTier;
    private final Map<Integer, Integer> tokenValues;
    private final EnchantEffects effects;

    public EnchantDefinition(
            String id,
            String displayName,
            Rarity rarity,
            Set<ItemCategory> applicable,
            int maxTier,
            Map<Integer, Integer> tokenValues,
            EnchantEffects effects
    ) {
        this(id, displayName, "", List.of(), rarity, applicable, maxTier, tokenValues, effects);
    }

    public EnchantDefinition(
            String id,
            String displayName,
            String description,
            List<String> lore,
            Rarity rarity,
            Set<ItemCategory> applicable,
            int maxTier,
            Map<Integer, Integer> tokenValues,
            EnchantEffects effects
    ) {
        this.id = Objects.requireNonNull(id);
        this.displayName = Objects.requireNonNull(displayName);
        this.description = description != null ? description : "";
        this.lore = lore != null ? List.copyOf(lore) : List.of();
        this.rarity = Objects.requireNonNull(rarity);
        this.applicable = applicable != null ? Set.copyOf(applicable) : Set.of();
        this.maxTier = Math.max(1, maxTier);
        this.tokenValues = tokenValues != null ? Map.copyOf(tokenValues) : Map.of();
        this.effects = effects;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public Rarity rarity() { return rarity; }
    public Set<ItemCategory> applicable() { return applicable; }
    public int maxTier() { return maxTier; }
    public Map<Integer, Integer> tokenValues() { return tokenValues; }
    public EnchantEffects effects() { return effects; }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public List<String> getLore() { return lore; }
    public Rarity getRarity() { return rarity; }
    public Set<ItemCategory> getApplicable() { return applicable; }
    public int getMaxTier() { return maxTier; }
    public Map<Integer, Integer> getTokenValues() { return tokenValues; }
    public EnchantEffects getEffects() { return effects; }

    public boolean isApplicableTo(ItemCategory category) {
        return applicable.contains(category);
    }

    public boolean isValidTier(int tier) {
        return tier >= 1 && tier <= maxTier;
    }

    @Override
    public String toString() {
        return "EnchantDefinition{" +
                "id='" + id + '\'' +
                ", rarity=" + rarity +
                ", maxTier=" + maxTier +
                '}';
    }
}
