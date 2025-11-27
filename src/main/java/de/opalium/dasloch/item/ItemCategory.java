package de.opalium.dasloch.item;

import java.util.Locale;

public enum ItemCategory {
    SWORD,
    BOW,
    PANTS,
    ARMOR;

    public static ItemCategory fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return SWORD;
        }

        String v = raw.trim().toUpperCase(Locale.ROOT);

        return switch (v) {
            // Nahkampf – gleiche Enchant-Pools wie SWORD
            case "SWORD", "AXE" -> SWORD;

            // Fernkampf – BOW + CROSSBOW zusammen
            case "BOW", "CROSSBOW" -> BOW;

            // Hosen – PANTS oder LEGGINGS
            case "PANTS", "LEGGINGS" -> PANTS;

            // Alles, was klassisch Rüstung ist
            case "HELMET", "CHESTPLATE", "BOOTS", "ARMOR" -> ARMOR;

            // Fallback – lieber harmlos SWORD statt Exception
            default -> SWORD;
        };
    }
}
