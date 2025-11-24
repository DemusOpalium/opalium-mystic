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
    private final String displayName;
    private final int customModelData;
    private final int baseLives;
    private final int maxLives;
    private final List<String> lore;
    private final Color dyeColor;

    public ItemTemplate(String id, ItemCategory category, ItemType type, Material material, String displayName,
                        int customModelData, int baseLives, int maxLives, List<String> lore, Color dyeColor) {
        this.id = id;
        this.category = category;
        this.type = type;
        this.material = material;
        this.displayName = displayName;
        this.customModelData = customModelData;
        this.baseLives = baseLives;
        this.maxLives = maxLives;
        this.lore = lore;
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

    public Optional<Color> getDyeColor() {
        return Optional.ofNullable(dyeColor);
    }
}
