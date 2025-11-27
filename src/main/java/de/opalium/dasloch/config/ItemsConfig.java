package de.opalium.dasloch.config;

import de.opalium.dasloch.model.ItemCategory;
import de.opalium.dasloch.model.ItemTemplate;
import de.opalium.dasloch.model.ItemType;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemsConfig {
    private final JavaPlugin plugin;
    private Map<String, ItemTemplate> templates = Collections.emptyMap();

    public ItemsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() throws IOException {
        File file = new File(plugin.getDataFolder(), "items.yml");
        if (!file.exists()) {
            plugin.saveResource("items.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            this.templates = Collections.emptyMap();
            return;
        }

        Map<String, ItemTemplate> loaded = new HashMap<>();
        for (String id : itemsSection.getKeys(false)) {
            ConfigurationSection section = itemsSection.getConfigurationSection(id);
            if (section == null) {
                continue;
            }

            ItemCategory category = ItemCategory.fromString(section.getString("category", "SWORD"));
            ItemType type = ItemType.fromString(section.getString("type", "MYSTIC"));
            Material material = Material.valueOf(section.getString("material", "STONE_SWORD"));

            // Erwachter Zustand
            String displayName = section.getString("display-name", id);
            int customModelData = section.getInt("custom-model-data", 0);
            int baseLives = section.getInt("base-lives", 1);
            int maxLives = section.getInt("max-lives", baseLives);
            List<String> lore = section.getStringList("lore");

            // Rohling-Zustand (optional, für Mystic-Rohlinge)
            String displayNameDormant = section.getString("display-name-dormant", null);

            Integer customModelDataDormant = null;
            if (section.contains("custom-model-data-dormant")) {
                customModelDataDormant = section.getInt("custom-model-data-dormant");
            }

            List<String> loreDormant = section.getStringList("lore-dormant");
            if (loreDormant == null) {
                loreDormant = Collections.emptyList();
            }

            Color dye = readColor(section.getConfigurationSection("dye-color"));

            ItemTemplate template = new ItemTemplate(
                    id,
                    category,
                    type,
                    material,
                    displayName,
                    displayNameDormant,
                    customModelData,
                    customModelDataDormant,
                    baseLives,
                    maxLives,
                    lore,
                    loreDormant,
                    dye
            );
            loaded.put(id.toLowerCase(), template);
        }

        this.templates = Collections.unmodifiableMap(loaded);
    }

    private Color readColor(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        Integer red = section.getInt("red", -1);
        Integer green = section.getInt("green", -1);
        Integer blue = section.getInt("blue", -1);
        if (red < 0 || green < 0 || blue < 0) {
            return null;
        }
        return Color.fromRGB(red, green, blue);
    }

    public Optional<ItemTemplate> getTemplate(String id) {
        return Optional.ofNullable(templates.get(id.toLowerCase()));
    }

    public Map<String, ItemTemplate> getTemplates() {
        return templates;
    }
}
