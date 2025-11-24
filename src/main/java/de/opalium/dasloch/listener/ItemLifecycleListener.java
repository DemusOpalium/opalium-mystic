package de.opalium.dasloch.listener;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.item.ItemCategory;
import de.opalium.dasloch.item.MysticItemService;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public final class ItemLifecycleListener implements Listener {

    private final DasLochPlugin plugin;
    private final MysticItemService itemService;

    public ItemLifecycleListener(DasLochPlugin plugin) {
        this.plugin = plugin;
        this.itemService = plugin.getItemService();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onArmorDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        int reduction = 0;
        for (ItemStack armor : armorContents) {
            if (armor == null || !itemService.isCustomItem(armor)) {
                continue;
            }
            Map<String, Integer> enchants = itemService.readEnchants(armor);
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                EnchantDefinition def = plugin.getEnchantRegistry().get(entry.getKey());
                if (def == null || !def.applicable().contains(ItemCategory.PANTS)) {
                    continue;
                }
                int tier = entry.getValue();
                Integer threshold = def.effects().lastStandThresholdHearts();
                if (threshold > 0 && player.getHealth() <= threshold) {
                    reduction = Math.max(reduction, def.effects().lastStandReductionPercent().getOrDefault(tier, 0));
                }
            }
        }
        if (reduction > 0) {
            double newDamage = event.getDamage() * (1 - reduction / 100.0);
            event.setDamage(newDamage);
        }
    }
}
