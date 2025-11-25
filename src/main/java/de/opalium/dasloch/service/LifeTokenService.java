package de.opalium.dasloch.service;

import de.opalium.dasloch.model.ItemTemplate;
import de.opalium.dasloch.model.ItemType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class LifeTokenService {

    // Neue Keys (aktuelles Schema)
    private final NamespacedKey idKey;
    private final NamespacedKey typeKey;
    private final NamespacedKey livesKey;
    private final NamespacedKey maxLivesKey;
    private final NamespacedKey tokensKey;

    // Legacy-Keys (altes Schema aus PluginKeys / alten Items)
    private final NamespacedKey legacyIdKey;
    private final NamespacedKey legacyTypeKey;
    private final NamespacedKey legacyLivesCurrentKey;
    private final NamespacedKey legacyLivesMaxKey;
    private final NamespacedKey legacyTokensKey;

    public LifeTokenService(JavaPlugin plugin) {
        // neues Schema
        this.idKey = new NamespacedKey(plugin, "id");
        this.typeKey = new NamespacedKey(plugin, "type");
        this.livesKey = new NamespacedKey(plugin, "lives");
        this.maxLivesKey = new NamespacedKey(plugin, "max_lives");
        this.tokensKey = new NamespacedKey(plugin, "tokens");

        // altes Schema (Kompatibilität)
        this.legacyIdKey = new NamespacedKey(plugin, "item_id");
        this.legacyTypeKey = new NamespacedKey(plugin, "item_type");
        this.legacyLivesCurrentKey = new NamespacedKey(plugin, "lives_current");
        this.legacyLivesMaxKey = new NamespacedKey(plugin, "lives_max");
        this.legacyTokensKey = new NamespacedKey(plugin, "tokens"); // gleicher Name wie neu
    }

    /**
     * Basisdaten des Items in die PDC schreiben.
     * Schreibt sowohl neue als auch Legacy-Keys, damit alte Tools weiter funktionieren.
     */
    public void applyBase(ItemStack stack, ItemTemplate template) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String id = template.getId();
        String typeMarker = markerFor(template.getType());
        int baseLives = template.getBaseLives();
        int maxLives = template.getMaxLives();

        // neues Schema
        container.set(idKey, PersistentDataType.STRING, id);
        container.set(typeKey, PersistentDataType.STRING, typeMarker);
        container.set(livesKey, PersistentDataType.INTEGER, baseLives);
        container.set(maxLivesKey, PersistentDataType.INTEGER, maxLives);
        container.set(tokensKey, PersistentDataType.INTEGER, 0);

        // Legacy-Schema
        container.set(legacyIdKey, PersistentDataType.STRING, id);
        container.set(legacyTypeKey, PersistentDataType.STRING, template.getType().name()); // LEGEND / MYSTIC
        container.set(legacyLivesCurrentKey, PersistentDataType.INTEGER, baseLives);
        container.set(legacyLivesMaxKey, PersistentDataType.INTEGER, maxLives);
        container.set(legacyTokensKey, PersistentDataType.INTEGER, 0);

        stack.setItemMeta(meta);
    }

    public Optional<String> getId(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String id = container.get(idKey, PersistentDataType.STRING);
        if (id == null) {
            id = container.get(legacyIdKey, PersistentDataType.STRING);
        }
        return Optional.ofNullable(id);
    }

    /**
     * Ermittelt den ItemType.
     * Versteht sowohl neues "type" (MYST / LEGEND) als auch altes "item_type" (MYSTIC / LEGEND).
     */
    public Optional<ItemType> getType(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String typeValue = container.get(typeKey, PersistentDataType.STRING);
        if (typeValue == null) {
            typeValue = container.get(legacyTypeKey, PersistentDataType.STRING);
        }
        if (typeValue == null) {
            return Optional.empty();
        }

        try {
            // Tolerant gegenüber Schreibweisen
            if (typeValue.equalsIgnoreCase("myst")) {
                return Optional.of(ItemType.MYSTIC);
            }
            if (typeValue.equalsIgnoreCase("mystic")) {
                return Optional.of(ItemType.MYSTIC);
            }
            if (typeValue.equalsIgnoreCase("legend")) {
                return Optional.of(ItemType.LEGEND);
            }
            // Fallback: Enum-Namen direkt
            return Optional.of(ItemType.valueOf(typeValue.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String markerFor(ItemType type) {
        return type == ItemType.MYSTIC ? "MYST" : "LEGEND";
    }

    public int getLives(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        Integer lives = container.get(livesKey, PersistentDataType.INTEGER);
        if (lives == null) {
            lives = container.get(legacyLivesCurrentKey, PersistentDataType.INTEGER);
        }
        return lives != null ? lives : 0;
    }

    public int getMaxLives(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        Integer maxLives = container.get(maxLivesKey, PersistentDataType.INTEGER);
        if (maxLives == null) {
            maxLives = container.get(legacyLivesMaxKey, PersistentDataType.INTEGER);
        }
        return maxLives != null ? maxLives : 0;
    }

    public void setLives(ItemStack stack, int value) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int clamped = Math.max(0, value);

        // neues Schema
        container.set(livesKey, PersistentDataType.INTEGER, clamped);
        // Legacy
        container.set(legacyLivesCurrentKey, PersistentDataType.INTEGER, clamped);

        stack.setItemMeta(meta);
    }

    public int getTokens(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        Integer tokens = container.get(tokensKey, PersistentDataType.INTEGER);
        if (tokens == null) {
            tokens = container.get(legacyTokensKey, PersistentDataType.INTEGER);
        }
        return tokens != null ? tokens : 0;
    }

    public void setTokens(ItemStack stack, int value) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int clamped = Math.max(0, value);

        // neues Schema
        container.set(tokensKey, PersistentDataType.INTEGER, clamped);
        // Legacy
        container.set(legacyTokensKey, PersistentDataType.INTEGER, clamped);

        stack.setItemMeta(meta);
    }
}
