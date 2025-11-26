package de.opalium.dasloch.item;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.enchant.EnchantRegistry;
import de.opalium.dasloch.integration.VaultService;
import de.opalium.dasloch.util.PluginKeys;
import de.opalium.dasloch.well.MysticWellService;
import de.opalium.dasloch.well.MysticWellTier;
import org.bukkit.ChatColor;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
        if (stack == null) {
            return Optional.empty();
        }
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
        if (meta == null) {
            return stack;
        }

        meta.setDisplayName(formatItemName(def, 0));
        if (def.customModelData() > 0) {
            meta.setCustomModelData(def.customModelData());
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        if (meta instanceof LeatherArmorMeta leather && def.dyeColor() != null) {
            leather.setColor(def.dyeColor());
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keys.itemId(), PersistentDataType.STRING, def.id());
        pdc.set(keys.itemType(), PersistentDataType.STRING, def.kind().name());
        pdc.set(keys.livesCurrent(), PersistentDataType.INTEGER, def.baseLives());
        pdc.set(keys.livesMax(), PersistentDataType.INTEGER, def.maxLives());
        pdc.set(keys.tokens(), PersistentDataType.INTEGER, 0);
        pdc.set(keys.prefix(), PersistentDataType.STRING, formatPrefix(0));
        pdc.set(keys.mysticTier(), PersistentDataType.STRING, "");
        // Initial: noch keine Mystic-Enchants
        pdc.set(keys.enchants(), PersistentDataType.STRING, "");

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
     * Zusätzliche Lore-Zeilen für gespeicherte Mystic-Enchants.
     *
     * Layout:
     *   [optional Banner]
     *   §7§oMystische Verzauberungen:
     *   <rarityColor><symbol> §7<Name> §8[Tier]
     *     <Tier-1-Lore>
     *     <Tier-2-Lore>
     *     <Tier-3-Lore>
     */
    private List<String> buildEnchantLore(Map<String, Integer> enchantTiers) {
        List<String> lines = new ArrayList<>();
        if (enchantTiers == null || enchantTiers.isEmpty()) {
            return lines;
        }

        // Vor-Scan: gibt es Legendary oder Tier >= 3?
        boolean showBanner = false;
        for (Map.Entry<String, Integer> entry : enchantTiers.entrySet()) {
            EnchantDefinition def = enchantRegistry.get(entry.getKey());
            if (def == null) {
                continue;
            }
            int tier = entry.getValue();
            if (def.rarity() == EnchantDefinition.Rarity.LEGENDARY || tier >= 3) {
                showBanner = true;
                break;
            }
        }

        if (showBanner) {
            // Dezentes Ornament – kann später verändert werden
            lines.add("§8꧁§5✠ §dVerzerrte Runen des Lochs §5✠§8꧂");
        }

        lines.add("§7§oMystische Verzauberungen:");

        enchantTiers.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> {
                    String enchantId = entry.getKey();
                    int tier = entry.getValue();

                    EnchantDefinition def = enchantRegistry.get(enchantId);
                    if (def == null) {
                        return;
                    }

                    String rarityColor = colorForRarity(def.rarity());
                    String symbol = symbolForEnchant(def);

                    String romanTier = switch (tier) {
                        case 1 -> "I";
                        case 2 -> "II";
                        case 3 -> "III";
                        case 4 -> "IV";
                        case 5 -> "V";
                        default -> String.valueOf(tier);
                    };

                    // Name immer grau, Tier dunkelgrau
                    String plainName = ChatColor.stripColor(def.displayName());
                    if (plainName == null || plainName.isEmpty()) {
                        plainName = def.displayName();
                    }

                    String headerLine = rarityColor + symbol + " §7" + plainName + " §8[" + romanTier + "]";
                    lines.add(headerLine);

                    // Tier-basierte Lore (staffelt hoch je Stufe)
                    Map<Integer, String> tierLore = def.tierLore();
                    if (tierLore != null && !tierLore.isEmpty()) {
                        for (int i = 1; i <= tier; i++) {
                            String rawLore = tierLore.get(i);
                            if (rawLore == null || rawLore.isBlank()) {
                                continue;
                            }
                            String detail = rawLore;
                            if (!detail.startsWith("§")) {
                                detail = "§8§o" + detail;
                            }
                            // leicht eingerückt unter der Titelzeile
                            lines.add("  " + detail);
                        }
                    } else {
                        // Fallback: erste normale Lore-Zeile, wenn keine tierLore definiert ist
                        List<String> extraLore = def.getLore();
                        if (extraLore != null && !extraLore.isEmpty()) {
                            String detail = extraLore.get(0);
                            if (detail != null && !detail.isBlank()) {
                                if (!detail.startsWith("§")) {
                                    detail = "§8§o" + detail;
                                }
                                lines.add("  " + detail);
                            }
                        }
                    }
                });

        return lines;
    }

    /**
     * Farbcode für die Seltenheit.
     */
    private String colorForRarity(EnchantDefinition.Rarity rarity) {
        if (rarity == null) {
            return "§7";
        }
        return switch (rarity) {
            case COMMON -> "§7";
            case UNCOMMON -> "§a";
            case RARE -> "§b";
            case EPIC -> "§5";
            case LEGENDARY -> "§6";
        };
    }

    /**
     * Symbolwahl nach Enchant-Typ (heuristisch über die ID).
     * Kann später noch feiner abgestimmt werden.
     */
    private String symbolForEnchant(EnchantDefinition def) {
        String id = def.id().toLowerCase(Locale.ROOT);

        // Economy / Loot / Gold
        if (id.contains("gold") || id.contains("coin") || id.contains("loot") || id.contains("bump") || id.contains("boost")) {
            return "☘";
        }

        // XP / Seelen / Spirit
        if (id.contains("xp") || id.contains("soul") || id.contains("spirit")) {
            return "✧";
        }

        // Heilung / Leben / Herz
        if (id.contains("heal") || id.contains("life") || id.contains("heart") || id.contains("regen")) {
            return "♥";
        }

        // Movement / Speed / Sprint
        if (id.contains("speed") || id.contains("dash") || id.contains("sprint") || id.contains("run") || id.contains("stride")) {
            return "➤";
        }

        // Defensive / Tank / Shield
        if (id.contains("tank") || id.contains("shield") || id.contains("guard") || id.contains("defense") || id.contains("resist")) {
            return "❖";
        }

        // Offensiv / Crit / Blood / Sharp
        if (id.contains("crit") || id.contains("strike") || id.contains("bleed") || id.contains("sharp") || id.contains("shark")) {
            return "✦";
        }

        // Verflucht / Dark / Void
        if (id.contains("curse") || id.contains("cursed") || id.contains("dark") || id.contains("void")) {
            return "☠";
        }

        // Hidden / Legacy / Relikt
        if (id.contains("hidden") || id.contains("legacy") || id.contains("relic")) {
            return "✠";
        }

        // Fallback: generischer Kristall
        return "◆";
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
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int max = pdc.getOrDefault(keys.livesMax(), PersistentDataType.INTEGER, lives);
        pdc.set(
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
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int tokens = pdc.getOrDefault(keys.tokens(), PersistentDataType.INTEGER, 0);
        int newValue = Math.max(0, tokens + delta);
        pdc.set(keys.tokens(), PersistentDataType.INTEGER, newValue);
        pdc.set(keys.prefix(), PersistentDataType.STRING, formatPrefix(newValue));
        refreshLore(stack, meta);
    }

    /**
     * Liest die gespeicherten Mystic-Enchants und berechnet die Token-Summe
     * über EnchantDefinition.tokenValues.
     */
    public int recalcTokens(ItemStack stack) {
        Map<String, Integer> enchantTiers = readEnchants(stack);
        int tokens = enchantTiers.entrySet().stream()
                .mapToInt(entry -> {
                    EnchantDefinition def = enchantRegistry.get(entry.getKey());
                    return def == null ? 0 : def.tokensForTier(entry.getValue());
                })
                .sum();

        ItemMeta meta = stack.getItemMeta();
        String itemId = "null";
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            itemId = pdc.get(keys.itemId(), PersistentDataType.STRING);
            pdc.set(keys.tokens(), PersistentDataType.INTEGER, tokens);
            pdc.set(keys.prefix(), PersistentDataType.STRING, formatPrefix(tokens));
            refreshLore(stack, meta);
        }

        plugin.getLogger().info("[DasLoch] [MysticWell] recalcTokens: itemId=" + itemId
                + " tokens=" + tokens + " enchantCount=" + enchantTiers.size());

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
                try {
                    values.put(pair[0], Integer.parseInt(pair[1]));
                } catch (NumberFormatException ignored) {
                }
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
        String raw = joiner.toString();

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(
                keys.enchants(),
                PersistentDataType.STRING,
                raw
        );

        String itemId = pdc.get(keys.itemId(), PersistentDataType.STRING);
        plugin.getLogger().info("[DasLoch] [MysticWell] writeEnchants: itemId=" + itemId
                + " raw=\"" + raw + "\" mapSize=" + enchantTiers.size());

        refreshLore(stack, meta);
    }

    /**
     * Erneuert Basis-Lore + Mystic-Enchant-Lore + Glint.
     */
    private void refreshLore(ItemStack stack, ItemMeta meta) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String id = pdc.get(keys.itemId(), PersistentDataType.STRING);
        LegendItemDefinition def = definitions.get(id);
        if (def == null) {
            return;
        }
        int lives = pdc.getOrDefault(keys.livesCurrent(), PersistentDataType.INTEGER, 0);
        int maxLives = pdc.getOrDefault(keys.livesMax(), PersistentDataType.INTEGER, lives);
        int tokens = pdc.getOrDefault(keys.tokens(), PersistentDataType.INTEGER, 0);

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
     * Vollständiger Mystic-Roll.
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

        // Bestimme Definition und führe den Roll aus
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String itemId = pdc.get(keys.itemId(), PersistentDataType.STRING);
        LegendItemDefinition definition = definitions.get(itemId);
        if (definition == null || definition.kind() != ItemKind.MYSTIC) {
            return null;
        }

        MysticWellService.RollResult result = wellService.roll(tier);

        // Enchants anwenden (schreibt auch Lore & enchants-PDC)
        applyEnchantRoll(definition.category(), result, wellTier, stack);

        // Jetzt METADATA NOCH EINMAL NEU LESEN, DAMIT ENCHANTS ERHALTEN BLEIBEN
        ItemMeta updatedMeta = stack.getItemMeta();
        if (updatedMeta != null) {
            PersistentDataContainer updatedPdc = updatedMeta.getPersistentDataContainer();
            updatedPdc.set(keys.mysticTier(), PersistentDataType.STRING, tier);
            stack.setItemMeta(updatedMeta);
        }

        // Tokens aus Enchants neu berechnen
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

        List<EnchantDefinition> all = new ArrayList<>(enchantRegistry.all().values());

        plugin.getLogger().info("[MysticWell] Roll: category=" + category
                + ", rarity=" + finalRarity
                + ", existingEnchants=" + existing.size()
                + ", registrySize=" + all.size());

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
                    + ". Fallback: ignoriere Rarity.");
            // Fallback: nur Kategorie prüfen
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
        if (rarity == EnchantDefinition.Rarity.COMMON) {
            return false;
        }

        String key = rarity.name().toLowerCase(); // "rare", "epic", "legendary", ...
        int limit = tier.rareLimits().getOrDefault(key, Integer.MAX_VALUE);
        if (limit == Integer.MAX_VALUE) {
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
        String normalized = rarity.trim().toUpperCase(Locale.ROOT);
        try {
            return EnchantDefinition.Rarity.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            // z.B. "UNCOMMON" -> fällt zurück auf COMMON
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
        return switch (tier.toUpperCase(Locale.ROOT)) {
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
            default -> tier.toLowerCase(Locale.ROOT);
        };
    }

    /**
     * Name-Formatierung: Mystic-Items bekommen Prefix + Name,
     * Legends behalten nur ihren Display-Namen.
     */
    private String formatItemName(LegendItemDefinition def, int tokens) {
        if (def.kind() == ItemKind.MYSTIC) {
            return formatPrefix(tokens) + " " + def.displayName();
        }
        return def.displayName();
    }
}
