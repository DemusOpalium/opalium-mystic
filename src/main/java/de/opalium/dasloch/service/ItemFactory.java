package de.opalium.dasloch.service;

import de.opalium.dasloch.model.ItemTemplate;
import de.opalium.dasloch.model.ItemType;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemFactory {
    private final LifeTokenService lifeTokenService;

    public ItemFactory(LifeTokenService lifeTokenService) {
        this.lifeTokenService = lifeTokenService;
    }

    public ItemStack createLegendItem(ItemTemplate template, OfflinePlayer owner) {
        if (template.getType() != ItemType.LEGEND) {
            throw new IllegalArgumentException("Template is not a legend item: " + template.getId());
        }
        return createItem(template, owner);
    }

    public ItemStack createMysticItem(ItemTemplate template) {
        if (template.getType() != ItemType.MYSTIC) {
            throw new IllegalArgumentException("Template is not a mystic item: " + template.getId());
        }
        return createItem(template, null);
    }

    public void refreshLore(ItemStack stack, ItemTemplate template) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        int lives = lifeTokenService.getLives(stack);
        int maxLives = lifeTokenService.getMaxLives(stack);
        int tokens = lifeTokenService.getTokens(stack);

        // Basis-Lore abhängig vom Zustand (Rohling vs Erwacht)
        List<String> baseLore = selectBaseLore(template, tokens);
        List<String> lore = buildLore(
                template,
                null,
                lives,
                maxLives,
                tokens,
                baseLore
        );
        meta.setLore(lore);

        // Anzeige-Name und Model je nach Rohling/Erwacht umschalten
        String displayName = selectDisplayName(template, tokens);
        if (displayName != null) {
            // Lore benutzt &-Farbcodes, Name kann das auch – daher übersetzen
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        }

        Integer model = selectModel(template, tokens);
        if (model != null && model > 0) {
            meta.setCustomModelData(model);
        }

        stack.setItemMeta(meta);
    }

    private ItemStack createItem(ItemTemplate template, OfflinePlayer owner) {
        ItemStack stack = new ItemStack(template.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        int lives = template.getBaseLives();
        int maxLives = template.getMaxLives();
        int tokens = 0; // frisch erstellt = Rohling

        // Name & Model nach Zustand auswählen (Rohling vs Erwacht)
        String displayName = selectDisplayName(template, tokens);
        if (displayName != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        }

        Integer model = selectModel(template, tokens);
        if (model != null && model > 0) {
            meta.setCustomModelData(model);
        }

        if (meta instanceof LeatherArmorMeta leather && template.getDyeColor().isPresent()) {
            leather.setColor(template.getDyeColor().get());
        }

        List<String> baseLore = selectBaseLore(template, tokens);
        List<String> lore = buildLore(
                template,
                owner != null ? owner.getUniqueId() : null,
                lives,
                maxLives,
                tokens,
                baseLore
        );
        meta.setLore(lore);
        stack.setItemMeta(meta);

        // PDC-Basiswerte (Lives/Tokens/ID etc.) über LifeTokenService setzen
        lifeTokenService.applyBase(stack, template);
        return stack;
    }

    /**
     * Wählt den angezeigten Namen abhängig vom Zustand.
     * Mystic + 0 Tokens -> Rohling, sonst erwacht.
     */
    private String selectDisplayName(ItemTemplate template, int tokens) {
        if (template.getType() == ItemType.MYSTIC && tokens == 0) {
            // Rohling-Name, falls gesetzt
            String dormant = template.getDisplayNameDormant();
            if (dormant != null && !dormant.isBlank()) {
                return dormant;
            }
        }
        // Fallback / Legends / erwachte Mystics
        return template.getDisplayName();
    }

    /**
     * Wählt das CustomModelData abhängig vom Zustand.
     */
    private Integer selectModel(ItemTemplate template, int tokens) {
        if (template.getType() == ItemType.MYSTIC && tokens == 0) {
            // Rohling-Model, falls vorhanden
            return template.getCustomModelDataDormant().orElse(null);
        }
        return template.getCustomModelData().orElse(null);
    }

    /**
     * Basis-Lore für Rohling vs Erwacht.
     */
    private List<String> selectBaseLore(ItemTemplate template, int tokens) {
        if (template.getType() == ItemType.MYSTIC && tokens == 0) {
            List<String> dormant = template.getLoreDormant();
            if (dormant != null && !dormant.isEmpty()) {
                return dormant;
            }
        }
        return template.getLore();
    }

    private List<String> buildLore(ItemTemplate template,
                                   UUID owner,
                                   int lives,
                                   int maxLives,
                                   int tokens,
                                   List<String> baseLore) {
        List<String> lore = new ArrayList<>();

        // Prefix bleibt wie bisher: einfacher Typ-Hinweis
        String prefix = template.getType() == ItemType.LEGEND ? "§6Legendary" : "§dMystic Item";

        List<String> source = baseLore != null ? baseLore : template.getLore();
        for (String line : source) {
            String applied = line
                .replace("%lives_current%", String.valueOf(lives))
                .replace("%lives_max%", String.valueOf(maxLives))
                .replace("%tokens%", String.valueOf(tokens))
                .replace("%prefix_line%", prefix);
            if (owner != null) {
                applied = applied.replace("%owner_uuid%", owner.toString());
            }
            // &-Farbcodes erlauben, §-Codes bleiben unverändert
            lore.add(ChatColor.translateAlternateColorCodes('&', applied));
        }
        return lore;
    }
}
