package de.opalium.dasloch.service;

import de.opalium.dasloch.config.EnchantsConfig;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.model.ItemCategory;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnchantParser {
    private final EnchantsConfig enchantsConfig;

    public EnchantParser(EnchantsConfig enchantsConfig) {
        this.enchantsConfig = enchantsConfig;
    }

    public Map<EnchantDefinition, Integer> parse(ItemStack stack, ItemCategory category) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || meta.getLore() == null) {
            return Map.of();
        }
        List<String> lore = meta.getLore();
        Map<EnchantDefinition, Integer> found = new HashMap<>();
        for (EnchantDefinition enchant : enchantsConfig.getEnchants().values()) {
            if (!enchant.getApplicableCategories().contains(category)) {
                continue;
            }
            String strippedName = ChatColor.stripColor(enchant.getDisplayName());
            for (String line : lore) {
                String strippedLine = ChatColor.stripColor(line).trim();
                if (!strippedLine.startsWith(strippedName)) {
                    continue;
                }
                int tier = extractTier(strippedLine.replace(strippedName, "").trim());
                if (tier > 0) {
                    found.put(enchant, tier);
                    break;
                }
            }
        }
        return found;
    }

    private int extractTier(String roman) {
        if (roman.isEmpty()) {
            return 1;
        }
        roman = roman.replace("Tier", "").trim();
        return romanNumeralToInt(roman);
    }

    private int romanNumeralToInt(String roman) {
        int result = 0;
        int prev = 0;
        for (char c : roman.toUpperCase().toCharArray()) {
            int value = switch (c) {
                case 'I' -> 1;
                case 'V' -> 5;
                case 'X' -> 10;
                case 'L' -> 50;
                case 'C' -> 100;
                default -> 0;
            };
            if (value > prev) {
                result += value - 2 * prev;
            } else {
                result += value;
            }
            prev = value;
        }
        return result;
    }
}
