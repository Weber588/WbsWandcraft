package wbs.wandcraft;

import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeAddModifierType;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.AttributeMultiplyModifierType;
import wbs.wandcraft.spell.attributes.modifier.AttributeSetModifierType;
import wbs.wandcraft.spell.definitions.FireballSpell;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.event.SpellEffectDefinition;

public class WandcraftRegistries {
    public static final WbsRegistry<SpellDefinition> SPELLS = new WbsRegistry<>(
            new FireballSpell()
    );
    public static final WbsRegistry<SpellEffectDefinition<?>> EFFECTS = new WbsRegistry<>(

    );
    public static final WbsRegistry<AttributeModifierType> MODIFIER_TYPES = new WbsRegistry<>(
            new AttributeSetModifierType(),
            new AttributeMultiplyModifierType(),
            new AttributeAddModifierType()
    );
    public static final WbsRegistry<SpellAttribute<?>> ATTRIBUTES = new WbsRegistry<>();
}
