package wbs.wandcraft.commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.commands.brigadier.argument.WbsRegistrySimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.dynamic.DynamicSpell;
import wbs.wandcraft.spell.dynamic.FixedDomainSpellArchetype;
import wbs.wandcraft.spell.dynamic.GenericSpellArchetype;
import wbs.wandcraft.spell.dynamic.SpellArchetype;

public interface CommandDynamicSpell {
    WbsRegistrySimpleArgument<SpellArchetype> ARCHETYPE = new WbsRegistrySimpleArgument<>(
            "archetype",
            WbsWandcraft.getInstance(),
            "spell archetype",
            SpellArchetype.class,
            WandcraftRegistries.SPELL_ARCHETYPES
    ).isRequired(true);
    WbsRegistrySimpleArgument<MagicDomain> MAGIC_DOMAIN = new WbsRegistrySimpleArgument<>(
            "magic_domain",
            WbsWandcraft.getInstance(),
            "magic domain",
            MagicDomain.class,
            WandcraftRegistries.MAGIC_DOMAINS
    );
    WbsRegistrySimpleArgument<MagicDomain> SECONDARY_DOMAIN = new WbsRegistrySimpleArgument<>(
            "magic_domain_secondary",
            WbsWandcraft.getInstance(),
            "magic domain",
            MagicDomain.class,
            WandcraftRegistries.MAGIC_DOMAINS
    );


    @Nullable
    default DynamicSpell getDynamicSpell(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        SpellArchetype archetype = ARCHETYPE.getRequiredValue(context);

        if (archetype == null) {
            return null;
        }

        DynamicSpell built;
        if (archetype instanceof GenericSpellArchetype genericAspect) {
            MagicDomain primaryMagicDomain = MAGIC_DOMAIN.getRequiredValue(context);
            if (primaryMagicDomain == null) {
                return null;
            }

            MagicDomain secondaryMagicDomain = configuredArgumentMap.get(SECONDARY_DOMAIN);

            built = genericAspect.build(primaryMagicDomain, secondaryMagicDomain);
        } else if (archetype instanceof FixedDomainSpellArchetype fixedAspect) {
            MagicDomain primaryMagicDomain = configuredArgumentMap.get(MAGIC_DOMAIN);

            built = fixedAspect.build(primaryMagicDomain);
        } else {
            throw new IllegalStateException("Unknown spell aspect!");
        }

        return built;
    }
}
