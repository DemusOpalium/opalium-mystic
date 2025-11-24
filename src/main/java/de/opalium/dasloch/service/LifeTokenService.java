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
    private final NamespacedKey idKey;
    private final NamespacedKey typeKey;
    private final NamespacedKey livesKey;
    private final NamespacedKey maxLivesKey;
    private final NamespacedKey tokensKey;

    public LifeTokenService(JavaPlugin plugin) {
        this.idKey = new NamespacedKey(plugin, "id");
        this.typeKey = new NamespacedKey(plugin, "type");
        this.livesKey = new NamespacedKey(plugin, "lives");
        this.maxLivesKey = new NamespacedKey(plugin, "max_lives");
        this.tokensKey = new NamespacedKey(plugin, "tokens");
    }

    public void applyBase(ItemStack stack, ItemTemplate template) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(idKey, PersistentDataType.STRING, template.getId());
        container.set(typeKey, PersistentDataType.STRING, markerFor(template.getType()));
        container.set(livesKey, PersistentDataType.INTEGER, template.getBaseLives());
        container.set(maxLivesKey, PersistentDataType.INTEGER, template.getMaxLives());
        container.set(tokensKey, PersistentDataType.INTEGER, 0);
        stack.setItemMeta(meta);
    }

    public Optional<String> getId(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING));
    }

    public Optional<ItemType> getType(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        String typeValue = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (typeValue == null) {
            return Optional.empty();
        }
        try {
            if (typeValue.equalsIgnoreCase("myst")) {
                return Optional.of(ItemType.MYSTIC);
            }
            return Optional.of(ItemType.valueOf(typeValue));
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
        return meta.getPersistentDataContainer().getOrDefault(livesKey, PersistentDataType.INTEGER, 0);
    }

    public int getMaxLives(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        return meta.getPersistentDataContainer().getOrDefault(maxLivesKey, PersistentDataType.INTEGER, 0);
    }

    public void setLives(ItemStack stack, int value) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(livesKey, PersistentDataType.INTEGER, Math.max(0, value));
        stack.setItemMeta(meta);
    }

    public int getTokens(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        return meta.getPersistentDataContainer().getOrDefault(tokensKey, PersistentDataType.INTEGER, 0);
    }

    public void setTokens(ItemStack stack, int value) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(tokensKey, PersistentDataType.INTEGER, Math.max(0, value));
        stack.setItemMeta(meta);
    }
}
