package wbs.wandcraft.generation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;

@NullMarked
public interface ItemGenerator {
    ItemStack generateItem();

    enum ItemGeneratorType {
        WAND(WandcraftRegistries.WAND_GENERATORS, WandGenerator::new),
        SPELL(WandcraftRegistries.SPELL_GENERATORS, SpellInstanceGenerator::new),
        MODIFIER(WandcraftRegistries.MODIFIER_GENERATORS, AttributeModifierGenerator::fromConfig),
        ;

        private final WbsRegistry<? extends ItemGenerator> registry;
        private final GeneratorConstructor constructor;

        ItemGeneratorType(WbsRegistry<? extends ItemGenerator> registry, GeneratorConstructor constructor) {
            this.registry = registry;
            this.constructor = constructor;
        }

        public WbsRegistry<? extends ItemGenerator> registry() {
            return registry;
        }

        public ItemGenerator construct(@NotNull ConfigurationSection section, WandcraftSettings settings, String directory) {
            return constructor.construct(section, settings, directory);
        }
    }

    @FunctionalInterface
    interface GeneratorConstructor {
        ItemGenerator construct(@NotNull ConfigurationSection section, WandcraftSettings settings, String directory);
    }
}
