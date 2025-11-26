package de.opalium.dasloch.item;

import org.bukkit.Color;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class LegendItemDefinition {

    private final String id;
    private final ItemCategory category;
    private final ItemKind kind;
    private final Material material;
    private final String displayName;
    private final int customModelData;
    private final int baseLives;
    private final int maxLives;
    private final List<String> lore;
    private final Color dyeColor;
    private final Map<String, Integer> baseEnchants;

    /**
     * Neuer Hauptkonstruktor mit baseEnchants.
     */
    public LegendItemDefinition(
            String id,
            ItemCategory category,
            ItemKind kind,
            Material material,
            String displayName,
            int customModelData,
            int baseLives,
            int maxLives,
            List<String> lore,
            Color dyeColor,
            Map<String, Integer> baseEnchants
    ) {
        this.id = id;
        this.category = category;
        this.kind = kind;
        this.material = material;
        this.displayName = displayName;
        this.customModelData = customModelData;
        this.baseLives = baseLives;
        this.maxLives = maxLives;
        this.lore = lore;
        this.dyeColor = dyeColor;
        // niemals null – alte Aufrufer können ohne Map arbeiten
        this.baseEnchants = (baseEnchants == null)
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(baseEnchants);
    }

    /**
     * Alte Signatur (ohne baseEnchants) für vorhandene Aufrufer.
     * Delegiert auf den neuen Konstruktor mit leerer Map.
     */
    public LegendItemDefinition(
            String id,
            ItemCategory category,
            ItemKind kind,
            Material material,
            String displayName,
            int customModelData,
            int baseLives,
            int maxLives,
            List<String> lore,
            Color dyeColor
    ) {
        this(
                id,
                category,
                kind,
                material,
                displayName,
                customModelData,
                baseLives,
                maxLives,
                lore,
                dyeColor,
                Collections.emptyMap()
        );
    }

    public String id() {
        return id;
    }

    public ItemCategory category() {
        return category;
    }

    public ItemKind kind() {
        return kind;
    }

    public Material material() {
        return material;
    }

    public String displayName() {
        return displayName;
    }

    public int customModelData() {
        return customModelData;
    }

    public int baseLives() {
        return baseLives;
    }

    public int maxLives() {
        return maxLives;
    }

    public List<String> lore() {
        return lore;
    }

    public Color dyeColor() {
        return dyeColor;
    }

    /**
     * Feste Mystic-Enchants, die ein Legend-Item beim Erzeugen bereits trägt.
     * Key = Enchant-ID (z.B. "lifesteal"), Value = Tier.
     * Kann leer, aber nie null sein.
     */
    public Map<String, Integer> baseEnchants() {
        return baseEnchants;
    }
}
