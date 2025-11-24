package de.opalium.dasloch.model;

public enum ItemCategory {
    SWORD,
    BOW,
    PANTS,
    ARMOR;

    public static ItemCategory fromString(String input) {
        for (ItemCategory category : values()) {
            if (category.name().equalsIgnoreCase(input)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown item category: " + input);
    }
}
