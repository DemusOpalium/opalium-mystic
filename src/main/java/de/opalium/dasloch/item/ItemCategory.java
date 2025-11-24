package de.opalium.dasloch.item;

public enum ItemCategory {
    SWORD,
    BOW,
    PANTS,
    ARMOR;

    public static ItemCategory fromString(String raw) {
        for (ItemCategory category : values()) {
            if (category.name().equalsIgnoreCase(raw)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + raw);
    }
}
