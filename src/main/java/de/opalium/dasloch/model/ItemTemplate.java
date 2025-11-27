package de.opalium.dasloch.model;

import org.bukkit.Color;
import org.bukkit.Material;

import java.util.List;
import java.util.Optional;

public class ItemTemplate {
    private final String id;
    private final ItemCategory category;
    private final ItemType type;
    private final Material material;

    // Erwachter Zustand
    private final String displayName;
    private final int customModelData;
    private final int baseLives;
    private final int maxLives;
    private final List<String> lore;

    // Rohling-Zustand (dormant)
    private final String displayNameDormant;
    private final Integer customModelDataDormant;
    private final List<String> loreDormant;

    private final Color dyeColor;

    /**
     * Alter Konstruktor – bleibt für bestehende Aufrufer kompatibel.
     * Nutzt intern den erweiterten Konstruktor und setzt Dormant-Werte auf "leer".
     */
    public ItemTemplate(String id,
                        ItemCategory category,
                        ItemType type,
                        Material material,
                        String displayName,
                        int customModelData,
                        int baseLives,
                        int maxLives,
                        List<String> lore,
                        Color dyeColor) {
        this(
                id,
                category,
                type,
                material,
                displayName,
                null,                 // displayNameDormant
                customModelData,
                null,                 // customModelDataDormant
                baseLives,
                maxLives,
                lore,
                null,                 // loreDormant
                dyeColor
        );
    }

    /**
     * Neuer, erweiterter Konstruktor mit Dormant-Feldern.
     */
    public ItemTemplate(String id,
                        ItemCategory category,
                        ItemType type,
                        Material material,
                        String displayName,
                        String displayNameDormant,
                        int customModelData,
                        Integer customModelDataDormant,
                        int baseLives,
                        int maxLives,
                        List<String> lore,
                        List<String> loreDormant,
                        Color dyeColor) {
        this.id = id;
        this.category = category;
        this.type = type;
        this.material = material;

        this.displayName = displayName;
        this.displayNameDormant = displayNameDormant;

        this.customModelData = customModelData;
        this.customModelDataDormant = customModelDataDormant;

        this.baseLives = baseLives;
        this.maxLives = maxLives;

        this.lore = lore != null ? List.copyOf(lore) : List.of();
        this.loreDormant = loreDormant != null ? List.copyOf(loreDormant) : List.of();

        this.dyeColor = dyeColor;
    }

    public String getId() {
        return id;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public ItemType getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    // Erwachter Zustand
    public String getDisplayName() {
        return displayName;
    }

    public Optional<Integer> getCustomModelData() {
        return customModelData > 0 ? Optional.of(customModelData) : Optional.empty();
    }

    public int getBaseLives() {
        return baseLives;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public List<String> getLore() {
        return lore;
    }

    // Dormant-Zustand

    public Optional<String> getDisplayNameDormant() {
        return Optional.ofNullable(displayNameDormant);
    }

    public Optional<Integer> getCustomModelDataDormant() {
        return customModelDataDormant != null && customModelDataDormant > 0
                ? Optional.of(customModelDataDormant)
                : Optional.empty();
    }

    public List<String> getLoreDormant() {
        return loreDormant;
    }

    public Optional<Color> getDyeColor() {
        return Optional.ofNullable(dyeColor);
    }
}
