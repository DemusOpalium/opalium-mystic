package de.opalium.dasloch.listener;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.item.ItemCategory;
import de.opalium.dasloch.item.MysticItemService;
import org.bukkit.entity.Entity;
import java.util.Map;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class CombatListener implements Listener {

    private final DasLochPlugin plugin;
    private final MysticItemService itemService;

    public CombatListener(DasLochPlugin plugin) {
        this.plugin = plugin;
        this.itemService = plugin.getItemService();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMelee(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!itemService.isCustomItem(weapon)) {
            return;
        }
        ItemCategory category = itemService.getCategory(weapon);
        if (category != ItemCategory.SWORD) {
            return;
        }
        applyHitEnchantEffects(player, event.getEntity(), weapon, event.getDamage());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBow(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof Player player)) {
            return;
        }
        ItemStack bow = player.getInventory().getItemInMainHand();
        if (!itemService.isCustomItem(bow) || itemService.getCategory(bow) != ItemCategory.BOW) {
            return;
        }
        applyBowEnchantEffects(event, bow, event.getDamage());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        ItemStack hand = victim.getInventory().getItemInMainHand();
        itemService.damageIfMystic(victim, hand);
        for (ItemStack armor : victim.getInventory().getArmorContents()) {
            if (armor != null) {
                itemService.damageIfMystic(victim, armor);
            }
        }

        Player killer = victim.getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            applyKillEffects(killer, weapon);
        }
    }

    private void applyHitEnchantEffects(Player player, Entity target, ItemStack weapon, double damage) {
        Map<String, Integer> enchants = itemService.readEnchants(weapon);
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            EnchantDefinition def = plugin.getEnchantRegistry().get(entry.getKey());
            if (def == null || !def.applicable().contains(ItemCategory.SWORD)) {
                continue;
            }
            int tier = entry.getValue();
            Integer healPercent = def.effects().healPercentOnHit().get(tier);
            if (healPercent != null && healPercent > 0) {
                double heal = damage * healPercent / 100.0;
                double newHealth = Math.min(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + heal);
                player.setHealth(newHealth);
            }
        }
    }

    private void applyBowEnchantEffects(EntityDamageByEntityEvent event, ItemStack bow, double damage) {
        Map<String, Integer> enchants = itemService.readEnchants(bow);
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            EnchantDefinition def = plugin.getEnchantRegistry().get(entry.getKey());
            if (def == null || !def.applicable().contains(ItemCategory.BOW)) {
                continue;
            }
            int tier = entry.getValue();
            Integer extraPercent = def.effects().bowExtraDamagePercent().get(tier);
            if (extraPercent != null && extraPercent > 0) {
                double newDamage = damage + (damage * extraPercent / 100.0);
                event.setDamage(newDamage);
            }
        }
    }

    private void applyKillEffects(Player killer, ItemStack weapon) {
        if (!itemService.isCustomItem(weapon)) {
            return;
        }
        Map<String, Integer> enchants = itemService.readEnchants(weapon);
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            EnchantDefinition def = plugin.getEnchantRegistry().get(entry.getKey());
            if (def == null) {
                continue;
            }
            int tier = entry.getValue();
            Integer gold = def.effects().goldOnKill().get(tier);
            if (gold != null && gold > 0 && plugin.getVaultService().hasEconomy()) {
                plugin.getVaultService().deposit(killer, gold);
            }
            Integer xpPercent = def.effects().xpOnKillPercent().get(tier);
            if (xpPercent != null && xpPercent > 0) {
                int base = 10; // simple baseline
                killer.giveExp((int) Math.round(base * xpPercent / 100.0));
            }
        }
    }

}
