package de.opalium.dasloch.listener;

import de.opalium.dasloch.DasLochPlugin;
import de.opalium.dasloch.enchant.EnchantDefinition;
import de.opalium.dasloch.item.ItemCategory;
import de.opalium.dasloch.item.MysticItemService;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class CombatListener implements Listener {

    private final DasLochPlugin plugin;
    private final MysticItemService itemService;

    public CombatListener(DasLochPlugin plugin) {
        this.plugin = plugin;
        this.itemService = plugin.getItemService();
    }

    // ------------------------------------------------------------------------
    // MELEE DAMAGE
    // ------------------------------------------------------------------------
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

    // ------------------------------------------------------------------------
    // BOW DAMAGE
    // ------------------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGH)
    public void onBow(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }

        if (!(arrow.getShooter() instanceof Player player)) {
            return;
        }

        ItemStack bow = player.getInventory().getItemInMainHand();
        if (!itemService.isCustomItem(bow)) {
            return;
        }

        if (itemService.getCategory(bow) != ItemCategory.BOW) {
            return;
        }

        applyBowEnchantEffects(event, bow, event.getDamage());
    }

    // ------------------------------------------------------------------------
    // PLAYER DEATH – HANDLE DURABILITY + KILL REWARDS
    // ------------------------------------------------------------------------
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // Damage mainhand
        ItemStack hand = victim.getInventory().getItemInMainHand();
        itemService.damageIfMystic(victim, hand);

        // Damage armor
        for (ItemStack armor : victim.getInventory().getArmorContents()) {
            if (armor != null) {
                itemService.damageIfMystic(victim, armor);
            }
        }

        // Killer rewards
        Player killer = victim.getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            applyKillEffects(killer, weapon);
        }
    }

    // ------------------------------------------------------------------------
    // SWORD ENCHANTS (ON HIT)
    // ------------------------------------------------------------------------
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
                double healAmount = damage * healPercent / 100.0;

                AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealthAttr == null) {
                    return;
                }

                double maxHP = maxHealthAttr.getValue();
                double newHP = Math.min(maxHP, player.getHealth() + healAmount);

                player.setHealth(newHP);
            }
        }
    }

    // ------------------------------------------------------------------------
    // BOW ENCHANTS (EXTRA DAMAGE)
    // ------------------------------------------------------------------------
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

    // ------------------------------------------------------------------------
    // ON KILL REWARD ENCHANTS
    // ------------------------------------------------------------------------
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

            // GOLD ON KILL
            Integer gold = def.effects().goldOnKill().get(tier);
            if (gold != null && gold > 0 && plugin.getVaultService().hasEconomy()) {
                plugin.getVaultService().deposit(killer, gold);
            }

            // XP ON KILL
            Integer xpPercent = def.effects().xpOnKillPercent().get(tier);
            if (xpPercent != null && xpPercent > 0) {
                int baseXP = 10; // baseline
                killer.giveExp((int) Math.round(baseXP * xpPercent / 100.0));
            }
        }
    }
}
