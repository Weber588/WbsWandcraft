package wbs.wandcraft.learning;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.WbsWandcraft;

import java.text.DecimalFormat;
import java.util.List;

@NullMarked
public class LootTableMethod extends RegistrableLearningMethod {
    private final double chance;
    private final String lootTableRegex;
    private final int rolls;

    public LootTableMethod(ConfigurationSection parentSection, String key, String directory) throws InvalidConfigurationException {
        super(parentSection, key, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section == null) {
            throw new InvalidConfigurationException("Must be a section.", directory);
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
        return Component.text("In \"" + lootTableRegex + "\" loot: ")
                .append(Component.text(new DecimalFormat("#.#").format(chance) + "%")
                        .color(NamedTextColor.AQUA)
                ).appendNewline()
                .append(indent)
                .append(indent)
                .append(Component.text("(tell jane to make that look prettier)").color(NamedTextColor.GRAY));
    }
}
