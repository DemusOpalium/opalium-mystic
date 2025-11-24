package de.opalium.dasloch.item;

public enum ItemKind {
    LEGEND,
    MYSTIC;

    public static ItemKind fromString(String raw) {
        for (ItemKind type : values()) {
            if (type.name().equalsIgnoreCase(raw)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown item type: " + raw);
    }
}
