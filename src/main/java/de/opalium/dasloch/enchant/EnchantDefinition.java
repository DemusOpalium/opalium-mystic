package de.opalium.dasloch.enchant;

import de.opalium.dasloch.item.ItemCategory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Zentrale Definition eines Custom-Enchants.
 *
 * Unterstützt:
 *  - id / displayName / description / lore
 *  - rarity (COMMON, UNCOMMON, RARE, EPIC, LEGENDARY)
 *  - applicable ItemCategories
 *  - maxTier
 *  - tokenValues pro Tier
 *  - EnchantEffects (Heilung, Gold, XP, usw.)
 *
 * Enthält sowohl „record-artige“ Getter (id(), rarity(), …)
 * als auch klassische getX()-Methoden und die Legacy-Methoden
 * getApplicableCategories() und tokensForTier(), damit alle
 * älteren Services weiter kompilieren.
 */
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

    /**
     * Vereinfachter Konstruktor ohne Beschreibung/Lore.
     */
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

    /**
     * Voller Konstruktor mit allen Feldern.
     */
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
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.description = description != null ? description : "";
        this.lore = lore != null ? List.copyOf(lore) : List.of();
        this.rarity = Objects.requireNonNull(rarity, "rarity");
        this.applicable = applicable != null ? Set.copyOf(applicable) : Set.of();
        this.maxTier = Math.max(1, maxTier);
        this.tokenValues = tokenValues != null ? Map.copyOf(tokenValues) : Map.of();
        this.effects = effects;
    }

    // ---------------------------------------------------------------------
    // "record"-artige Getter (werden bereits im Projekt verwendet)
    // ---------------------------------------------------------------------

    public String id() { return id; }
    public String displayName() { return displayName; }
    public Rarity rarity() { return rarity; }
    public Set<ItemCategory> applicable() { return applicable; }
    public int maxTier() { return maxTier; }
    public Map<Integer, Integer> tokenValues() { return tokenValues; }
    public EnchantEffects effects() { return effects; }

    // ---------------------------------------------------------------------
    // Klassische Getter
    // ---------------------------------------------------------------------

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public List<String> getLore() { return lore; }
    public Rarity getRarity() { return rarity; }
    public Set<ItemCategory> getApplicable() { return applicable; }
    public int getMaxTier() { return maxTier; }
    public Map<Integer, Integer> getTokenValues() { return tokenValues; }

    // ---------------------------------------------------------------------
    // Kompatibilitäts-Methoden für ältere Services
    // ---------------------------------------------------------------------

    /**
     * Wird von EnchantParser/MysticWellService erwartet.
     * Alias für {@link #getApplicable()}.
     */
    public Set<ItemCategory> getApplicableCategories() {
        return applicable;
    }

    /**
     * Wird von MysticItemService erwartet.
     * Gibt die Token-Kosten für das angefragte Tier zurück.
     *
     * - Tier < 1 -> 0
     * - Tier > maxTier -> Wert von maxTier
     * - Kein Eintrag im tokenValues-Map -> 0
     */
    public int tokensForTier(int tier) {
        if (tier < 1) {
            return 0;
        }
        if (tier > maxTier) {
            tier = maxTier;
        }
        Integer value = tokenValues.get(tier);
        return value != null ? value : 0;
    }

    /**
     * Komfort-Overload für Aufrufe mit Integer.
     */
    public int tokensForTier(Integer tier) {
        return (tier == null) ? 0 : tokensForTier(tier.intValue());
    }

    // ---------------------------------------------------------------------
    // Utility
    // ---------------------------------------------------------------------

    public boolean isApplicableTo(ItemCategory category) {
        return applicable.contains(category);
    }

    public boolean isValidTier(int tier) {
        return tier >= 1 && tier <= maxTier;
    }
}
