package de.opalium.dasloch.enchant;

import de.opalium.dasloch.item.ItemCategory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Vollständige Definition eines Custom-Enchants.
 *
 * Pflicht:
 *  - id: interne ID
 *  - displayName: sichtbarer Name
 *  - rarity: Seltenheit
 *  - applicable: erlaubte Item-Kategorien
 *  - maxTier: maximale Stufe
 *  - tokenValues: Token-Kosten pro Tier
 *  - effects: Effekt-Beschreibung
 *
 * Optional:
 *  - description: Kurz-Text / Menütitel
 *  - lore: mehrzeilige Beschreibung
 */
public final class EnchantDefinition {

    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY;

        /**
         * Wandelt einen String aus der Konfiguration in eine Rarity um.
         * Erlaubt z.B. "common", "Common", "COMMON", "epic", "EPIC" usw.
         */
        public static Rarity fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Rarity value cannot be null");
            }

            String normalized = value.trim().toUpperCase();

            try {
                return Rarity.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
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
     * Alter Basis-Konstruktor – wird aktuell vom EnchantRegistry benutzt.
     * (Kompatibel zu deiner bisherigen Version.)
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
        this(id, displayName, null, null, rarity, applicable, maxTier, tokenValues, effects);
    }

    /**
     * Erweiterter Konstruktor mit Beschreibung und Lore.
     * Kann später vom YAML-Loader genutzt werden.
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

    // ===== Getter im bisherigen Stil (kompatibel zu deiner Version) =====

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
        return Collections.unmodifiableSet(applicable);
    }

    public int maxTier() {
        return maxTier;
    }

    public Map<Integer, Integer> tokenValues() {
        return Collections.unmodifiableMap(tokenValues);
    }

    public EnchantEffects effects() {
        return effects;
    }

    public int tokensForTier(int tier) {
        if (tier < 1) {
            return 0;
        }
        return tokenValues.getOrDefault(tier, 0);
    }

    // ===== Zusätzliche, moderne Getter (falls du sie nutzen willst) =====

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public Rarity getRarity() {
        return rarity;
    }

    public Set<ItemCategory> getApplicable() {
        return Collections.unmodifiableSet(applicable);
    }

    public int getMaxTier() {
        return maxTier;
    }

    public Map<Integer, Integer> getTokenValues() {
        return Collections.unmodifiableMap(tokenValues);
    }

    public EnchantEffects getEffects() {
        return effects;
    }

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
