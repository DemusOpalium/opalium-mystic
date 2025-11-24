package de.opalium.dasloch.listener;

import de.opalium.dasloch.config.ItemsConfig;
import de.opalium.dasloch.service.ItemFactory;
import de.opalium.dasloch.service.LifeTokenService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ItemLifecycleListener implements Listener {
    private final ItemsConfig itemsConfig;
    private final LifeTokenService lifeTokenService;
    private final ItemFactory itemFactory;

    public ItemLifecycleListener(ItemsConfig itemsConfig, LifeTokenService lifeTokenService, ItemFactory itemFactory) {
        this.itemsConfig = itemsConfig;
        this.lifeTokenService = lifeTokenService;
        this.itemFactory = itemFactory;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        for (ItemStack item : event.getEntity().getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            Optional<String> id = lifeTokenService.getId(item);
            if (id.isEmpty()) {
                continue;
            }
            int currentLives = lifeTokenService.getLives(item);
            lifeTokenService.setLives(item, currentLives - 1);
            itemsConfig.getTemplate(id.get()).ifPresent(template -> itemFactory.refreshLore(item, template));
        }
    }
}
