package wbs.wandcraft;

import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeAddModifierType;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.AttributeMultiplyModifierType;
import wbs.wandcraft.spell.attributes.modifier.AttributeSetModifierType;
import wbs.wandcraft.spell.definitions.*;
import wbs.wandcraft.spell.event.SpellEffectDefinition;
import wbs.wandcraft.wand.WandInventoryType;

public class WandcraftRegistries {
    public static final WbsRegistry<SpellAttribute<?>> ATTRIBUTES = new WbsRegistry<>();
    public static final WbsRegistry<AttributeModifierType> MODIFIER_TYPES = new WbsRegistry<>(
            new AttributeSetModifierType(),
            new AttributeMultiplyModifierType(),
            new AttributeAddModifierType()
    );
    public static final WbsRegistry<SpellEffectDefinition<?>> EFFECTS = new WbsRegistry<>(

    );
    public static final WbsRegistry<WandInventoryType> WAND_INVENTORY_TYPES = WandInventoryType.WAND_INVENTORY_TYPES;
    public static final WbsRegistry<SpellDefinition> SPELLS = new WbsRegistry<>(
            new FireboltSpell(),
            new FireballSpell(),
            new LeapSpell(),
            new BlinkSpell(),
            new PrismaticRaySpell(),
            new EldritchBlastSpell(),
            new WarpSpell()
    );
}
