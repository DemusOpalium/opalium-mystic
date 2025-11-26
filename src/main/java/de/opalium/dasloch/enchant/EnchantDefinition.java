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
 *  - tierLore: Lore-Zeilen pro Tier (1..N) für Item-Anzeige
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

    /**
     * Pro-Tier-Lore für die Item-Anzeige:
     * key = Tier (1..N), value = Lore-Zeile (inkl. Farbcodes).
     */
    private final Map<Integer, String> tierLore;

    private final Rarity rarity;
    private final Set<ItemCategory> applicable;
    private final int maxTier;
    private final Map<Integer, Integer> tokenValues;
    private final EnchantEffects effects;

    /**
     * Vereinfachter Konstruktor ohne Beschreibung/Lore.
     * Verwendet keine tierLore.
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
        this(
                id,
                displayName,
                "",
                List.of(),
                rarity,
                applicable,
                maxTier,
                tokenValues,
                effects,
                null
        );
    }

    /**
     * Voller Konstruktor mit Beschreibung + normaler Lore,
     * aber ohne tierLore (Kompatibilität).
     *
     * Parser, die noch kein tierLore kennen, können weiterhin
     * diesen Konstruktor benutzen.
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
        this(
                id,
                displayName,
                description,
                lore,
                rarity,
                applicable,
                maxTier,
                tokenValues,
                effects,
                null
        );
    }

    /**
     * Neuer Voll-Konstruktor inklusive tierLore-Map.
     *
     * Dieser Konstruktor ist für den erweiterten Parser gedacht,
     * der das "lore:"-Mapping pro Tier aus enchants.yml liest.
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
            EnchantEffects effects,
            Map<Integer, String> tierLore
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
        this.tierLore = tierLore != null ? Map.copyOf(tierLore) : Map.of();
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

    /**
     * Lore-Zeilen nach Tier.
     */
    public Map<Integer, String> tierLore() { return tierLore; }

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

    /**
     * Klassischer Getter für tierLore.
     */
    public Map<Integer, String> getTierLore() { return tierLore; }

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

    /**
     * Gibt die (optionale) Lore-Zeile für ein bestimmtes Tier zurück.
     * Wenn keine spezifische Lore definiert ist, wird null geliefert.
     */
    public String loreForTier(int tier) {
        if (tierLore.isEmpty()) {
            return null;
        }
        return tierLore.get(tier);
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
