package de.opalium.dasloch.item;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.enchant.EnchantRegistry;
import de.opalium.dasloch.util.PluginKeys;
import de.opalium.dasloch.well.MysticWellService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

public final class MysticItemService {

    private final DasLochPlugin plugin;
    private final Map<String, LegendItemDefinition> definitions = new HashMap<>();
    private final PluginKeys keys;
    private final EnchantRegistry enchantRegistry;
    private final MysticWellService wellService;

    public MysticItemService(DasLochPlugin plugin, YamlConfiguration config, EnchantRegistry enchantRegistry, MysticWellService wellService) {
        this.plugin = plugin;
        this.keys = new PluginKeys(plugin);
        this.enchantRegistry = enchantRegistry;
        this.wellService = wellService;
        load(config);
    }

    private void load(YamlConfiguration config) {
        definitions.clear();
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items == null) {
            return;
        }
        for (String id : items.getKeys(false)) {
            ConfigurationSection section = items.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ItemCategory category = ItemCategory.fromString(section.getString("category", "SWORD"));
            ItemKind type = ItemKind.fromString(section.getString("type", "MYSTIC"));
            Material material = Material.matchMaterial(section.getString("material", "STONE_SWORD"));
            if (material == null) {
                material = Material.STONE_SWORD;
            }
            String displayName = section.getString("display-name", id);
            int model = section.getInt("custom-model-data", 0);
            int baseLives = section.getInt("base-lives", 10);
            int maxLives = section.getInt("max-lives", baseLives);
            List<String> lore = section.getStringList("lore");
            Color color = null;
            ConfigurationSection dye = section.getConfigurationSection("dye-color");
            if (dye != null) {
                color = Color.fromRGB(dye.getInt("red"), dye.getInt("green"), dye.getInt("blue"));
            }
            LegendItemDefinition def = new LegendItemDefinition(id, category, type, material, displayName, model, baseLives, maxLives, lore, color);
            definitions.put(id, def);
        }
    }

    public Optional<LegendItemDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public ItemStack createLegendItem(String id, String owner) {
        return createItem(id, ItemKind.LEGEND, owner);
    }

    public ItemStack createMysticItem(String id) {
        return createItem(id, ItemKind.MYSTIC, null);
    }

    private ItemStack createItem(String id, ItemKind expectedType, String owner) {
        LegendItemDefinition def = definitions.get(id);
        if (def == null || def.kind() != expectedType) {
            return new ItemStack(Material.BARRIER);
        }
        ItemStack stack = new ItemStack(def.material());
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(def.displayName());
        if (def.customModelData() > 0) {
            meta.setCustomModelData(def.customModelData());
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        if (meta instanceof LeatherArmorMeta leather && def.dyeColor() != null) {
            leather.setColor(def.dyeColor());
        }
        meta.getPersistentDataContainer().set(keys.itemId(), PersistentDataType.STRING, def.id());
        meta.getPersistentDataContainer().set(keys.itemType(), PersistentDataType.STRING, def.kind().name());
        meta.getPersistentDataContainer().set(keys.livesCurrent(), PersistentDataType.INTEGER, def.baseLives());
        meta.getPersistentDataContainer().set(keys.livesMax(), PersistentDataType.INTEGER, def.maxLives());
        meta.getPersistentDataContainer().set(keys.tokens(), PersistentDataType.INTEGER, 0);
        meta.getPersistentDataContainer().set(keys.prefix(), PersistentDataType.STRING, formatPrefix(0));
        meta.setLore(applyLore(def, def.baseLives(), def.maxLives(), 0, owner));
        stack.setItemMeta(meta);
        return stack;
    }

    private List<String> applyLore(LegendItemDefinition def, int lives, int maxLives, int tokens, String owner) {
        List<String> result = new ArrayList<>();
        for (String line : def.lore()) {
            String replaced = line
                    .replace("%lives_current%", String.valueOf(lives))
                    .replace("%lives_max%", String.valueOf(maxLives))
                    .replace("%tokens%", String.valueOf(tokens))
                    .replace("%prefix_line%", formatPrefix(tokens));
            if (owner != null) {
                replaced = replaced.replace("%owner%", owner);
            }
            result.add(replaced);
        }
        return result;
    }

    public boolean isCustomItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(keys.itemId(), PersistentDataType.STRING);
    }

    public int getLives(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        return meta.getPersistentDataContainer().getOrDefault(keys.livesCurrent(), PersistentDataType.INTEGER, 0);
    }

    public int getTokens(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        return meta.getPersistentDataContainer().getOrDefault(keys.tokens(), PersistentDataType.INTEGER, 0);
    }

    public void setLives(ItemStack stack, int lives) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        int max = meta.getPersistentDataContainer().getOrDefault(keys.livesMax(), PersistentDataType.INTEGER, lives);
        meta.getPersistentDataContainer().set(keys.livesCurrent(), PersistentDataType.INTEGER, Math.max(0, Math.min(lives, max)));
        refreshLore(stack, meta);
    }

    public void decrementLife(ItemStack stack) {
        int current = getLives(stack);
        setLives(stack, current - 1);
    }

    public int recalcTokens(ItemStack stack) {
        Map<String, Integer> enchantTiers = readEnchants(stack);
        int tokens = enchantTiers.entrySet().stream()
                .mapToInt(entry -> {
                    EnchantDefinition def = enchantRegistry.get(entry.getKey());
                    return def == null ? 0 : def.tokensForTier(entry.getValue());
                })
                .sum();
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(keys.tokens(), PersistentDataType.INTEGER, tokens);
            meta.getPersistentDataContainer().set(keys.prefix(), PersistentDataType.STRING, formatPrefix(tokens));
            refreshLore(stack, meta);
        }
        return tokens;
    }

    public Map<String, Integer> readEnchants(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(keys.enchants(), PersistentDataType.STRING)) {
            return Collections.emptyMap();
        }
        String raw = meta.getPersistentDataContainer().get(keys.enchants(), PersistentDataType.STRING);
        Map<String, Integer> values = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return values;
        }
        String[] parts = raw.split(";");
        for (String part : parts) {
            String[] pair = part.split(":");
            if (pair.length == 2) {
                values.put(pair[0], Integer.parseInt(pair[1]));
            }
        }
        return values;
    }

    public void writeEnchants(ItemStack stack, Map<String, Integer> enchantTiers) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        StringJoiner joiner = new StringJoiner(";");
        for (Map.Entry<String, Integer> entry : enchantTiers.entrySet()) {
            joiner.add(entry.getKey() + ":" + entry.getValue());
        }
        meta.getPersistentDataContainer().set(keys.enchants(), PersistentDataType.STRING, joiner.toString());
        refreshLore(stack, meta);
    }

    private void refreshLore(ItemStack stack, ItemMeta meta) {
        String id = meta.getPersistentDataContainer().get(keys.itemId(), PersistentDataType.STRING);
        LegendItemDefinition def = definitions.get(id);
        if (def == null) {
            return;
        }
        int lives = meta.getPersistentDataContainer().getOrDefault(keys.livesCurrent(), PersistentDataType.INTEGER, 0);
        int maxLives = meta.getPersistentDataContainer().getOrDefault(keys.livesMax(), PersistentDataType.INTEGER, lives);
        int tokens = meta.getPersistentDataContainer().getOrDefault(keys.tokens(), PersistentDataType.INTEGER, 0);
        meta.setLore(applyLore(def, lives, maxLives, tokens, null));
        stack.setItemMeta(meta);
    }

    public String formatPrefix(int tokens) {
        if (tokens >= 9) {
            return "§6Legendary";
        }
        if (tokens >= 5) {
            return "§eGrand";
        }
        if (tokens >= 3) {
            return "§bRare";
        }
        return "§7Unenchanted";
    }

    public PluginKeys keys() {
        return keys;
    }

    public ItemCategory getCategory(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        String id = meta.getPersistentDataContainer().get(keys.itemId(), PersistentDataType.STRING);
        LegendItemDefinition def = definitions.get(id);
        return def == null ? null : def.category();
    }

    public ItemKind getKind(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        return ItemKind.fromString(meta.getPersistentDataContainer().getOrDefault(keys.itemType(), PersistentDataType.STRING, "MYSTIC"));
    }

    public void damageIfMystic(Player player, ItemStack stack) {
        if (!isCustomItem(stack)) {
            return;
        }
        decrementLife(stack);
        if (getLives(stack) <= 0) {
            player.getInventory().remove(stack);
            player.sendMessage("§cDein mystisches Item hat alle Leben verloren und zerfällt.");
        }
    }

    public void refreshDamageable(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(0);
            stack.setItemMeta(meta);
        }
    }
}
