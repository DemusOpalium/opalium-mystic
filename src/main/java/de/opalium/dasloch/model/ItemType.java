package de.opalium.dasloch.model;

public enum ItemType {
    LEGEND,
    MYSTIC;

    public static ItemType fromString(String input) {
        for (ItemType type : values()) {
            if (type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown item type: " + input);
    }
}
