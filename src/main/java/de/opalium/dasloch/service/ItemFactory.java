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
        List<String> lore = buildLore(template, null,
            lifeTokenService.getLives(stack),
            lifeTokenService.getMaxLives(stack),
            lifeTokenService.getTokens(stack));
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    private ItemStack createItem(ItemTemplate template, OfflinePlayer owner) {
        ItemStack stack = new ItemStack(template.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        meta.setDisplayName(template.getDisplayName());
        template.getCustomModelData().ifPresent(meta::setCustomModelData);
        if (meta instanceof LeatherArmorMeta leather && template.getDyeColor().isPresent()) {
            leather.setColor(template.getDyeColor().get());
        }

        List<String> lore = buildLore(template, owner != null ? owner.getUniqueId() : null,
            template.getBaseLives(), template.getMaxLives(), 0);
        meta.setLore(lore);
        stack.setItemMeta(meta);

        lifeTokenService.applyBase(stack, template);
        return stack;
    }

    private List<String> buildLore(ItemTemplate template, UUID owner, int lives, int maxLives, int tokens) {
        List<String> lore = new ArrayList<>();
        String prefix = template.getType() == ItemType.LEGEND ? "§6Legendary" : "§dMystic Item";
        for (String line : template.getLore()) {
            String applied = line
                .replace("%lives_current%", String.valueOf(lives))
                .replace("%lives_max%", String.valueOf(maxLives))
                .replace("%tokens%", String.valueOf(tokens))
                .replace("%prefix_line%", prefix);
            if (owner != null) {
                applied = applied.replace("%owner_uuid%", owner.toString());
            }
            lore.add(ChatColor.translateAlternateColorCodes('&', applied));
        }
        return lore;
    }
}
