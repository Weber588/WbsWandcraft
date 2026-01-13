package wbs.wandcraft.learning;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.generation.ItemGenerator;
import wbs.wandcraft.generation.ItemGenerator.ItemGeneratorType;

import java.util.List;

@NullMarked
public class LootTableMethod extends RegistrableLearningMethod {
    private final ItemGenerator resultGenerator;
    private final double chance;
    private final String lootTableRegex;
    private final int rolls;

    public LootTableMethod(ConfigurationSection parentSection, String key, String directory) throws InvalidConfigurationException {
        super(parentSection, key, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section == null) {
            throw new InvalidConfigurationException("Must be a section.", directory);
        }

        String itemTypeString = section.getString("item-type");
        if (itemTypeString == null) {
            throw new InvalidConfigurationException("item-type is a required field. Pick from the following: " +
                    String.join(", ", WbsEnums.toStringList(ItemGeneratorType.class)), directory + "/item-type");
        }

        ItemGeneratorType checkType = WbsEnums.getEnumFromString(ItemGeneratorType.class, itemTypeString);
        if (checkType == null) {
            throw new InvalidConfigurationException("Invalid item-type. Pick from the following: " +
                    String.join(", ", WbsEnums.toStringList(ItemGeneratorType.class)), directory + "/item-type");
        }

        String generatorPath = "item-generator";
        ConfigurationSection generatorSection = section.getConfigurationSection(generatorPath);
        if (generatorSection != null) {
            resultGenerator = checkType.construct(generatorSection, WbsWandcraft.getInstance().getSettings(), directory + "/" + generatorPath);
        } else {
            String resultGeneratorKeyString = section.getString(generatorPath);
            if (resultGeneratorKeyString == null) {
                throw new InvalidConfigurationException(generatorPath + " is a required field.", directory + "/" + generatorPath);
            }

            NamespacedKey generatorKey = NamespacedKey.fromString(resultGeneratorKeyString, WbsWandcraft.getInstance());
            if (generatorKey == null) {
                throw new InvalidConfigurationException("Invalid key: " + resultGeneratorKeyString, directory + "/" + generatorPath);
            }

            ItemGenerator checkGenerator = checkType.registry().get(generatorKey);
            if (checkGenerator == null) {
                throw new InvalidConfigurationException("Invalid result key for " + generatorPath + " " + generatorKey + ". Pick from the following: " +
                        String.join(", ", WbsEnums.toStringList(ItemGeneratorType.class)), directory + "/" + generatorPath);
            }

            resultGenerator = checkGenerator;
        }

        String checkLootTable = section.getString("loot-table");
        if (checkLootTable == null) {
            throw new InvalidConfigurationException("loot-table is a required field.", directory + "/loot-table");
        }
        lootTableRegex = checkLootTable;

        chance = section.getDouble("chance", 100);
        int rollsCheck = section.getInt("rolls", 1);
        if (rollsCheck <= 0) {
            WbsWandcraft.getInstance().getSettings().logError("rolls must be greater than 0.", directory + "/rolls");
        }
        rolls = Math.max(1, rollsCheck);
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), LootGenerateEvent.class, this::onLootGenerate);
    }

    private void onLootGenerate(LootGenerateEvent event) {
        String lootTableName = event.getLootTable().key().asString();
        if (!lootTableName.matches(lootTableRegex)) {
            return;
        }

        for (int i = 0; i < rolls; i++) {
            tryAdding(event);
        }
    }

    private void tryAdding(LootGenerateEvent event) {
        if (!WbsMath.chance(chance)) {
            return;
        }

        ItemStack itemStack = resultGenerator.generateItem();

        List<ItemStack> loot = event.getLoot();

        loot.add(itemStack);
    }

    @Override
    public Component describe(Component indent, boolean shorten) {
        return Component.text("From Piglin bartering: " + chance + "%");
    }
}
