package de.opalium.dasloch.enchant;

import de.opalium.dasloch.item.ItemCategory;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantDefinitionTest {

    @Test
    void tokensForTierReturnsConfiguredValue() {
        EnchantDefinition definition = new EnchantDefinition(
                "test",
                "Test Enchant",
                "",
                null,
                EnchantDefinition.Rarity.COMMON,
                Set.of(ItemCategory.SWORD),
                3,
                Map.of(1, 5, 2, 10),
                null
        );

        assertEquals(10, definition.tokensForTier(2));
    }

    @Test
    void tokensForTierGracefullyHandlesMissingOrInvalidTiers() {
        EnchantDefinition definition = new EnchantDefinition(
                "test",
                "Test Enchant",
                EnchantDefinition.Rarity.COMMON,
                Set.of(ItemCategory.SWORD),
                2,
                Map.of(1, 4),
                null
        );

        assertEquals(0, definition.tokensForTier(3));
        assertEquals(0, definition.tokensForTier(0));
        assertEquals(0, definition.tokensForTier(2));
    }
}
