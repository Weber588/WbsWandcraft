package wbs.wandcraft;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.data.PaperEnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO;

@SuppressWarnings("unused")
public class WbsWandcraftBootstrap implements PluginBootstrap {
    public static final TagKey<BlockType> ALL_BLOCKS = BlockTypeTagKeys.create(getKey("all"));

    private static @NotNull NamespacedKey getKey(String value) {
        return new NamespacedKey("wbswandcraft", value);
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new WbsWandcraft();
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();
        RegistryKey<BlockType> registryKey = RegistryKey.BLOCK;
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(registryKey).newHandler(event -> {
            event.registrar().addToTag(BlockTypeTagKeys.SCULK_REPLACEABLE, Set.of(TypedKey.create(registryKey, BlockType.AMETHYST_BLOCK.key())));

            Set<TypedKey<BlockType>> allBlocks = RegistryAccess.registryAccess().getRegistry(registryKey).keyStream().map(registryKey::typedKey).collect(Collectors.toSet());
            event.registrar().setTag(ALL_BLOCKS, allBlocks);
        }));

        manager.registerEventHandler(RegistryEvents.DAMAGE_TYPE.compose().newHandler(event -> {

        }));
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
            event.registry().register(
                    TypedKey.create(RegistryKey.ENCHANTMENT, getKey("magic_protection")),
                    builder -> {
                        // NMS start
                        DataComponentMap.Builder effects = DataComponentMap.builder();

                        DamageSourcePredicate damageSourcePredicate = new DamageSourcePredicate(
                                List.of(TagPredicate.is(WITCH_RESISTANT_TO)),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()
                        );
                        EnchantmentValueEffect enchantmentValueEffect = new AddValue(LevelBasedValue.perLevel(2));
                        ConditionalEffect<EnchantmentValueEffect> conditionalEffect = new ConditionalEffect<>(
                                enchantmentValueEffect,
                                Optional.empty()
                        );
                        effects.set(EnchantmentEffectComponents.DAMAGE_PROTECTION, List.of(conditionalEffect));

                        if (builder instanceof PaperEnchantmentRegistryEntry.PaperBuilder paperBuilder) {
                            try {
                                Field effectsField = PaperEnchantmentRegistryEntry.class.getDeclaredField("effects");
                                effectsField.setAccessible(true);
                                DataComponentMap internalEffects = (DataComponentMap) effectsField.get(paperBuilder);
                                if (internalEffects.equals(DataComponentMap.EMPTY)) {
                                    effectsField.set(paperBuilder, effects.build());
                                }
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        // NMS end

                        builder
                                .description(Component.text("Magic Protection"))
                                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_ARMOR))
                                .anvilCost(1)
                                .maxLevel(4)
                                .weight(10)
                                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
                                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
                                .activeSlots(EquipmentSlotGroup.ARMOR);
                    }
            );
        }));
    }
}
