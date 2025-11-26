package de.opalium.dasloch.item;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.enchant.EnchantRegistry;
import de.opalium.dasloch.integration.VaultService;
import de.opalium.dasloch.util.PluginKeys;
import de.opalium.dasloch.well.MysticWellService;
import de.opalium.dasloch.well.MysticWellTier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
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

    public MysticItemService(
            DasLochPlugin plugin,
            YamlConfiguration config,
            EnchantRegistry enchantRegistry,
            MysticWellService wellService
    ) {
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
                color = Color.fromRGB(
                        dye.getInt("red"),
                        dye.getInt("green"),
                        dye.getInt("blue")
                );
            }
            LegendItemDefinition def = new LegendItemDefinition(
                    id,
                    category,
                    type,
                    material,
                    displayName,
                    model,
                    baseLives,
                    maxLives,
                    lore,
                    color
            );
            definitions.put(id, def);
        }
    }

    public Optional<LegendItemDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public Optional<String> getId(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(meta.getPersistentDataContainer()
                .get(keys.itemId(), PersistentDataType.STRING));
    }

    public List<String> definitionIds(ItemKind kind) {
        List<String> ids = new ArrayList<>();
        for (LegendItemDefinition def : new HashSet<>(definitions.values())) {
            if (def.kind() == kind) {
                ids.add(def.id());
            }
        }
        Collections.sort(ids);
        return ids;
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
        meta.setDisplayName(formatItemName(def, 0));
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
        meta.getPersistentDataContainer().set(keys.mysticTier(), PersistentDataType.STRING, "");
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

    /**
     * Baut zusätzliche Lore-Zeilen für die gespeicherten Mystic-Enchants.
     * Zeigt für jeden Enchant: Rarity-Farbe, Name und Tier (römisch).
     */
    private List<String> buildEnchantLore(Map<String, Integer> enchantTiers) {
        List<String> lines = new ArrayList<>();
        if (enchantTiers == null || enchantTiers.isEmpty()) {
            return lines;
        }

        // Überschrift für den Enchant-Block
        lines.add("§8Mystische Verzauberungen:");

        enchantTiers.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> {
                    String enchantId = entry.getKey();
                    int tier = entry.getValue();

                    EnchantDefinition def = enchantRegistry.get(enchantId);
                    if (def == null) {
                        return;
                    }

                    // Rarity-Farbe bestimmen
                    String rarityColor;
                    switch (def.rarity()) {
                        case COMMON -> rarityColor = "§7";
                        case RARE -> rarityColor = "§9";
                        case EPIC -> rarityColor = "§5";
                        case LEGENDARY -> rarityColor = "§6";
                        default -> rarityColor = "§7";
                    }

                    // Tier in römische Zahl umwandeln
                    String romanTier = switch (tier) {
                        case 1 -> "I";
                        case 2 -> "II";
                        case 3 -> "III";
                        case 4 -> "IV";
                        case 5 -> "V";
                        default -> String.valueOf(tier);
                    };

                    // ✦ Name [Tier]
                    lines.add(rarityColor + "✦ " + def.displayName() + " §7[" + romanTier + "]");
                });

        return lines;
    }

    public boolean isCustomItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer()
                .has(keys.itemId(), PersistentDataType.STRING);
    }

    public int getLives(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        return meta.getPersistentDataContainer()
                .getOrDefault(keys.livesCurrent(), PersistentDataType.INTEGER, 0);
    }

    public int getMaxLives(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        return meta.getPersistentDataContainer()
                .getOrDefault(keys.livesMax(), PersistentDataType.INTEGER, 0);
    }

    public int getTokens(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return 0;
        }
        return meta.getPersistentDataContainer()
                .getOrDefault(keys.tokens(), PersistentDataType.INTEGER, 0);
    }

    public void setLives(ItemStack stack, int lives) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        int max = meta.getPersistentDataContainer()
                .getOrDefault(keys.livesMax(), PersistentDataType.INTEGER, lives);
        meta.getPersistentDataContainer().set(
                keys.livesCurrent(),
                PersistentDataType.INTEGER,
                Math.max(0, Math.min(lives, max))
        );
        refreshLore(stack, meta);
    }

    public void decrementLife(ItemStack stack) {
        int current = getLives(stack);
        setLives(stack, current - 1);
    }

    public void addTokens(ItemStack stack, int delta) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        int tokens = meta.getPersistentDataContainer()
                .getOrDefault(keys.tokens(), PersistentDataType.INTEGER, 0);
        int newValue = Math.max(0, tokens + delta);
        meta.getPersistentDataContainer().set(keys.tokens(), PersistentDataType.INTEGER, newValue);
        meta.getPersistentDataContainer().set(keys.prefix(), PersistentDataType.STRING, formatPrefix(newValue));
        refreshLore(stack, meta);
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
            meta.getPersistentDataContainer().set(
                    keys.tokens(),
                    PersistentDataType.INTEGER,
                    tokens
            );
            meta.getPersistentDataContainer().set(
                    keys.prefix(),
                    PersistentDataType.STRING,
                    formatPrefix(tokens)
            );
            refreshLore(stack, meta);
        }
        return tokens;
    }

    public Map<String, Integer> readEnchants(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer()
                .has(keys.enchants(), PersistentDataType.STRING)) {
            return Collections.emptyMap();
        }
        String raw = meta.getPersistentDataContainer()
                .get(keys.enchants(), PersistentDataType.STRING);
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
        meta.getPersistentDataContainer().set(
                keys.enchants(),
                PersistentDataType.STRING,
                joiner.toString()
        );
        refreshLore(stack, meta);
    }

    private void refreshLore(ItemStack stack, ItemMeta meta) {
        String id = meta.getPersistentDataContainer()
                .get(keys.itemId(), PersistentDataType.STRING);
        LegendItemDefinition def = definitions.get(id);
        if (def == null) {
            return;
        }
        int lives = meta.getPersistentDataContainer()
                .getOrDefault(keys.livesCurrent(), PersistentDataType.INTEGER, 0);
        int maxLives = meta.getPersistentDataContainer()
                .getOrDefault(keys.livesMax(), PersistentDataType.INTEGER, lives);
        int tokens = meta.getPersistentDataContainer()
                .getOrDefault(keys.tokens(), PersistentDataType.INTEGER, 0);

        // Basis-Lore aus items.yml
        List<String> lore = applyLore(def, lives, maxLives, tokens, null);

        // Enchants lesen und zusätzliche Lore-Zeilen anhängen
        Map<String, Integer> enchantTiers = readEnchants(stack);
        List<String> enchantLore = buildEnchantLore(enchantTiers);
        if (!enchantLore.isEmpty()) {
            lore.add("");
            lore.addAll(enchantLore);

            // Glint hinzufügen, wenn Mystic-Enchants vorhanden sind
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            // Glint entfernen, wenn keine Mystic-Enchants vorhanden sind
            meta.removeEnchant(Enchantment.UNBREAKING);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setLore(lore);
        meta.setDisplayName(formatItemName(def, tokens));
        stack.setItemMeta(meta);
    }

    public String formatPrefix(int tokens) {
        if (tokens >= 8) {
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
        String id = meta.getPersistentDataContainer()
                .get(keys.itemId(), PersistentDataType.STRING);
        LegendItemDefinition def = definitions.get(id);
        return def == null ? null : def.category();
    }

    public ItemKind getKind(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }
        return ItemKind.fromString(
                meta.getPersistentDataContainer()
                        .getOrDefault(keys.itemType(), PersistentDataType.STRING, "MYSTIC")
        );
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

    public boolean tryDeductGold(Player player, String tier) {
        int cost = wellService.baseCosts().getOrDefault(costKey(tier), 0);
        VaultService vaultService = plugin.getVaultService();
        if (cost <= 0 || !vaultService.hasEconomy()) {
            return true;
        }
        return vaultService.withdraw(player, cost);
    }

    /**
     * Vollständiger Mystic-Roll:
     * - prüft, ob Mystic-Item
     * - prüft Tier-Reihenfolge
     * - zieht Gold ab
     * - würfelt RollResult
     * - wendet Enchant an (applyEnchantRoll)
     * - speichert Tier und Tokens / Prefix
     * - aktualisiert Name und Lore
     *
     * @return RollResult oder null, wenn der Roll nicht ausgeführt wurde
     */
    public MysticWellService.RollResult rollMystic(Player player, ItemStack stack, String requestedTier) {
        if (!isCustomItem(stack) || getKind(stack) != ItemKind.MYSTIC) {
            return null;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return null;
        }

        String tier = normalizeTier(requestedTier);
        if (tier == null || !canApplyTier(meta, tier)) {
            return null;
        }

        MysticWellTier wellTier = wellService.tier(tier);
        if (wellTier == null) {
            return null;
        }

        if (!tryDeductGold(player, tier)) {
            return null;
        }

        MysticWellService.RollResult result = wellService.roll(tier);
        LegendItemDefinition definition = definitions.get(
                meta.getPersistentDataContainer().get(keys.itemId(), PersistentDataType.STRING)
        );
        if (definition == null || definition.kind() != ItemKind.MYSTIC) {
            return null;
        }

        applyEnchantRoll(definition.category(), result, wellTier, stack);
        meta.getPersistentDataContainer().set(keys.mysticTier(), PersistentDataType.STRING, tier);
        stack.setItemMeta(meta);
        recalcTokens(stack);
        return result;
    }

    private void applyEnchantRoll(
            ItemCategory category,
            MysticWellService.RollResult rollResult,
            MysticWellTier tier,
            ItemStack stack
    ) {
        EnchantDefinition.Rarity rarity = parseRarity(rollResult.rarityRolled());
        Map<String, Integer> existing = new HashMap<>(readEnchants(stack));

        // Limit für höhere Rarities anhand der well.yml-Konfiguration
        if (exceedsRarityLimit(tier, existing, rarity)) {
            rarity = EnchantDefinition.Rarity.COMMON;
        }

        final EnchantDefinition.Rarity finalRarity = rarity;

        // Debug: Wie viele Enchants kennt das Registry überhaupt?
        int totalEnchants = enchantRegistry.all().size();
        plugin.getLogger().info("[MysticWell] Roll: category=" + category
                + ", rarity=" + finalRarity
                + ", existing=" + existing.size()
                + ", registrySize=" + totalEnchants);

        // Basis: alle bekannten Enchants
        List<EnchantDefinition> all = new ArrayList<>(enchantRegistry.all().values());

        // Kandidaten: passende Kategorie + passende Rarity
        List<EnchantDefinition> candidates = all.stream()
                .filter(def -> def.applicable() == null
                        || def.applicable().isEmpty()
                        || def.applicable().contains(category))
                .filter(def -> def.rarity() == finalRarity)
                .toList();

        if (candidates.isEmpty()) {
            plugin.getLogger().warning("[MysticWell] Keine passenden Enchants für category="
                    + category + ", rarity=" + finalRarity
                    + ". Verwende Fallback (beliebige Rarity).");

            // Fallback: nur Kategorie prüfen, Rarity ignorieren
            candidates = all.stream()
                    .filter(def -> def.applicable() == null
                            || def.applicable().isEmpty()
                            || def.applicable().contains(category))
                    .toList();
        }

        if (candidates.isEmpty()) {
            plugin.getLogger().severe("[MysticWell] Immer noch keine Enchant-Kandidaten! "
                    + "Prüfe enchants.yml und EnchantRegistry-Initialisierung.");
            return;
        }

        EnchantDefinition selected = candidates.get(
                ThreadLocalRandom.current().nextInt(candidates.size())
        );

        int rolledTier = Math.max(1, Math.min(rollResult.tokensAwarded(), selected.maxTier()));

        if (existing.containsKey(selected.id())) {
            int newTier = Math.min(
                    selected.maxTier(),
                    existing.get(selected.id()) + rolledTier
            );
            existing.put(selected.id(), newTier);
        } else if (existing.size() >= 3) {
            String target = existing.entrySet().stream()
                    .min(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            if (target != null) {
                EnchantDefinition targetDef = enchantRegistry.get(target);
                if (targetDef != null) {
                    int newTier = Math.min(
                            targetDef.maxTier(),
                            existing.get(target) + rolledTier
                    );
                    existing.put(target, newTier);
                }
            }
        } else {
            existing.put(selected.id(), rolledTier);
        }

        writeEnchants(stack, existing);
    }

    private boolean exceedsRarityLimit(
            MysticWellTier tier,
            Map<String, Integer> existing,
            EnchantDefinition.Rarity rarity
    ) {
        // Nur COMMON ist immer erlaubt, alle höheren Rarities können limitiert werden
        if (rarity == EnchantDefinition.Rarity.COMMON) {
            return false;
        }

        String key = rarity.name().toLowerCase(); // "rare", "epic", "legendary", ...
        int limit = tier.rareLimits().getOrDefault(key, Integer.MAX_VALUE);
        if (limit == Integer.MAX_VALUE) {
            // kein Limit gesetzt
            return false;
        }

        long count = existing.keySet().stream()
                .map(enchantRegistry::get)
                .filter(def -> def != null && def.rarity() == rarity)
                .count();

        return count >= limit;
    }

    private EnchantDefinition.Rarity parseRarity(String rarity) {
        if (rarity == null || rarity.isBlank()) {
            return EnchantDefinition.Rarity.COMMON;
        }
        String normalized = rarity.trim().toUpperCase();
        try {
            // Erwartet Enum-Namen wie COMMON, RARE, EPIC, LEGENDARY ...
            return EnchantDefinition.Rarity.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            // Fallback, wenn well.yml etwas Unerwartetes liefert
            return EnchantDefinition.Rarity.COMMON;
        }
    }

    private boolean canApplyTier(ItemMeta meta, String requestedTier) {
        String current = meta.getPersistentDataContainer()
                .getOrDefault(keys.mysticTier(), PersistentDataType.STRING, "");
        if ("III".equalsIgnoreCase(current)) {
            return false;
        }
        return switch (requestedTier) {
            case "I" -> current.isEmpty();
            case "II" -> "I".equalsIgnoreCase(current);
            case "III" -> "II".equalsIgnoreCase(current);
            default -> false;
        };
    }

    private String normalizeTier(String tier) {
        if (tier == null) {
            return null;
        }
        return switch (tier.toUpperCase()) {
            case "T1", "TIER_1", "1", "I" -> "I";
            case "T2", "TIER_2", "2", "II" -> "II";
            case "T3", "TIER_3", "3", "III" -> "III";
            default -> null;
        };
    }

    private String costKey(String tier) {
        return switch (tier) {
            case "I" -> "tier_1";
            case "II" -> "tier_2";
            case "III" -> "tier_3";
            default -> tier.toLowerCase();
        };
    }

    private String formatItemName(LegendItemDefinition def, int tokens) {
        if (def.kind() == ItemKind.MYSTIC) {
            return formatPrefix(tokens) + " " + def.displayName();
        }
        return def.displayName();
    }
}
