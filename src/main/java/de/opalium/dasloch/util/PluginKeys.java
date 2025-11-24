package de.opalium.dasloch.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class PluginKeys {

    private final NamespacedKey itemId;
    private final NamespacedKey itemType;
    private final NamespacedKey livesCurrent;
    private final NamespacedKey livesMax;
    private final NamespacedKey tokens;
    private final NamespacedKey prefix;
    private final NamespacedKey enchants;

    public PluginKeys(Plugin plugin) {
        this.itemId = new NamespacedKey(plugin, "item_id");
        this.itemType = new NamespacedKey(plugin, "item_type");
        this.livesCurrent = new NamespacedKey(plugin, "lives_current");
        this.livesMax = new NamespacedKey(plugin, "lives_max");
        this.tokens = new NamespacedKey(plugin, "tokens");
        this.prefix = new NamespacedKey(plugin, "prefix");
        this.enchants = new NamespacedKey(plugin, "enchants");
    }

    public NamespacedKey itemId() {
        return itemId;
    }

    public NamespacedKey itemType() {
        return itemType;
    }

    public NamespacedKey livesCurrent() {
        return livesCurrent;
    }

    public NamespacedKey livesMax() {
        return livesMax;
    }

    public NamespacedKey tokens() {
        return tokens;
    }

    public NamespacedKey prefix() {
        return prefix;
    }

    public NamespacedKey enchants() {
        return enchants;
    }
}
