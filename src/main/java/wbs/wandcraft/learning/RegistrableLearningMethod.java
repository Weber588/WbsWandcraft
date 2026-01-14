package wbs.wandcraft.learning;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.generation.ItemGenerator;

public abstract class RegistrableLearningMethod extends LearningMethod {
    protected final ItemGenerator resultGenerator;

    public RegistrableLearningMethod(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section == null) {
            throw new InvalidConfigurationException("Must be a section.", directory);
        }

        String itemTypeString = section.getString("item-type");
        if (itemTypeString == null) {
            throw new InvalidConfigurationException("item-type is a required field. Pick from the following: " +
                    String.join(", ", WbsEnums.toStringList(ItemGenerator.ItemGeneratorType.class)), directory + "/item-type");
        }

        ItemGenerator.ItemGeneratorType checkType = WbsEnums.getEnumFromString(ItemGenerator.ItemGeneratorType.class, itemTypeString);
        if (checkType == null) {
            throw new InvalidConfigurationException("Invalid item-type. Pick from the following: " +
                    String.join(", ", WbsEnums.toStringList(ItemGenerator.ItemGeneratorType.class)), directory + "/item-type");
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
                        String.join(", ", WbsEnums.toStringList(ItemGenerator.ItemGeneratorType.class)), directory + "/" + generatorPath);
            }

            resultGenerator = checkGenerator;
        }
    }

    public ItemGenerator getResultGenerator() {
        return resultGenerator;
    }

    public abstract void registerEvents();
}
