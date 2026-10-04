package wbs.wandcraft.commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.commands.brigadier.argument.WbsRegistrySimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.dynamic.DynamicSpell;
import wbs.wandcraft.spell.dynamic.FixedTypeSpellAspect;
import wbs.wandcraft.spell.dynamic.GenericSpellAspect;
import wbs.wandcraft.spell.dynamic.SpellAspect;

public interface CommandDynamicSpell {
    WbsRegistrySimpleArgument<SpellAspect> ASPECT = new WbsRegistrySimpleArgument<>(
            "aspect",
            WbsWandcraft.getInstance(),
            "spell aspect",
            SpellAspect.class,
            WandcraftRegistries.SPELL_ASPECTS
    ).isRequired(true);
    WbsRegistrySimpleArgument<SpellType> SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    );
    WbsRegistrySimpleArgument<SpellType> SECONDARY_SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type_secondary",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    );


    @Nullable
    default DynamicSpell getDynamicSpell(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        SpellAspect aspect = ASPECT.getRequiredValue(context);

        if (aspect == null) {
            return null;
        }

        DynamicSpell built;
        if (aspect instanceof GenericSpellAspect genericAspect) {
            SpellType primarySpellType = SPELL_TYPE.getRequiredValue(context);
            if (primarySpellType == null) {
                return null;
            }

            SpellType secondarySpellType = configuredArgumentMap.get(SECONDARY_SPELL_TYPE);

            built = genericAspect.build(primarySpellType, secondarySpellType);
        } else if (aspect instanceof FixedTypeSpellAspect fixedAspect) {
            SpellType primarySpellType = configuredArgumentMap.get(SPELL_TYPE);

            built = fixedAspect.build(primarySpellType);
        } else {
            throw new IllegalStateException("Unknown spell aspect!");
        }

        return built;
    }
}
