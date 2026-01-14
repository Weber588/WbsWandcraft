package wbs.wandcraft.learning;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.WbsWandcraft;

import java.text.DecimalFormat;
import java.util.List;

@NullMarked
public class BarteringMethod extends RegistrableLearningMethod {
    private final double chance;

    public BarteringMethod(ConfigurationSection parentSection, String key, String directory) throws InvalidConfigurationException {
        super(parentSection, key, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section == null) {
            throw new InvalidConfigurationException("Must be a section.", directory);
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
        return Component.text("From Piglin bartering: ")
                .append(Component.text(new DecimalFormat("#.##").format(chance) + "%")
                        .color(NamedTextColor.AQUA)
                );
    }
}
