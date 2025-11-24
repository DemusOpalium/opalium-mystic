package de.opalium.dasloch.item;

import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;

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

    public LegendItemDefinition(String id, ItemCategory category, ItemKind kind, Material material, String displayName,
                                int customModelData, int baseLives, int maxLives, List<String> lore, Color dyeColor) {
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
}
