package wbs.wandcraft.learning;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.PiglinBarterEvent;
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
public class BarteringMethod extends RegistrableLearningMethod {
    private final ItemGenerator resultGenerator;
    private final double chance;

    public BarteringMethod(ConfigurationSection parentSection, String key, String directory) throws InvalidConfigurationException {
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

        chance = section.getDouble("chance", 100);
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), PiglinBarterEvent.class, this::onBarter);
    }

    private void onBarter(PiglinBarterEvent event) {
        if (!WbsMath.chance(chance)) {
            return;
        }

        ItemStack itemStack = resultGenerator.generateItem();

        List<ItemStack> outcome = event.getOutcome();
        outcome.clear();
        outcome.add(itemStack);
    }

    @Override
    public Component describe(Component indent, boolean shorten) {
        return Component.text("From Piglin bartering: " + chance + "%");
    }
}
