package wbs.wandcraft;

import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.generator.AttributeInstanceGenerator;
import wbs.wandcraft.generator.SpellInstanceGenerator;
import wbs.wandcraft.generator.WandGenerator;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.*;
import wbs.wandcraft.spell.definitions.extensions.*;
import wbs.wandcraft.spell.event.ForcePullEffect;
import wbs.wandcraft.spell.event.SpellEffectDefinition;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.WandInventoryType;
import wbs.wandcraft.wand.WandTexture;

public class WandcraftRegistries {
    public static final WbsRegistry<SpellAttribute<?>> ATTRIBUTES = new WbsRegistry<>();
    public static final WbsRegistry<AttributeModifierType> MODIFIER_TYPES = new WbsRegistry<>(
            AttributeModifierType.SET,
            AttributeModifierType.ADD,
            AttributeModifierType.MULTIPLY
    );
    public static final WbsRegistry<SpellEffectDefinition<?>> EFFECTS = new WbsRegistry<>(
            new ForcePullEffect()
    );
    public static final WbsRegistry<WandTexture> WAND_TEXTURES = new WbsRegistry<>(
            WandTexture.GEM,
            WandTexture.SCEPTRE,
            WandTexture.TRIDENT,
            WandTexture.FIRE,
            WandTexture.GEM_OVERGROWN
    );
    public static final WbsRegistry<WandInventoryType> WAND_INVENTORY_TYPES = WandInventoryType.WAND_INVENTORY_TYPES;
    public static final WbsRegistry<SpellDefinition> SPELLS = new WbsRegistry<>(
            new FireballSpell(),
            new LeapSpell(),
            new BlinkSpell(),
            new PrismaticRaySpell(),
            new EldritchBlastSpell(),
            new WarpSpell(),
            new RecallSpell(),
            new AntiMagicShellSpell(),
            new ArcaneSurgeSpell(),
            new ConflagrationSpell(),
            new VortexSpell(),
            new CrowsCallSpell(),
            new StunSpell()
    );
    public static final WbsRegistry<StatusEffect> STATUS_EFFECTS = new WbsRegistry<>(
            StatusEffect.GLIDING,
            StatusEffect.STUNNED
    );
    public static final WbsRegistry<WandGenerator> WAND_GENERATORS = new WbsRegistry<>(
            new WandGenerator(WbsWandcraft.getKey("test"), 1, 2, -1, 1, 3)
                    .addSpellGenerator(
                            new SpellInstanceGenerator()
                                    .addAttributeGenerator(
                                            new AttributeInstanceGenerator<>(CustomProjectileSpell.GRAVITY)
                                                    .setValues(0.5d, 0.8d, 1.2d)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeInstanceGenerator<>(SpeedSpell.SPEED)
                                                    .setValues(2d, 3d, 4d)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeInstanceGenerator<>(CustomProjectileSpell.BOUNCES)
                                                    .setValues(0, 0, 0, 1, 5)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeInstanceGenerator<>(RangedSpell.RANGE)
                                                    .setValues(25d, 50d, 50d, 75d, 100d)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeInstanceGenerator<>(CustomProjectileSpell.GRAVITY)
                                                    .setValues(4d, 6d, 10d)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeInstanceGenerator<>(IProjectileSpell.IMPRECISION)
                                                    .setValues(5d, 10d, 10d, 15d, 20d)
                                    )
                    ).addAttributeGenerator(
                            new AttributeInstanceGenerator<>(Wand.COOLDOWN)
                                    .setValues(1, 2, 3, 4)
                    ).addAttributeGenerator(
                            new AttributeInstanceGenerator<>(CastableSpell.COST)
                                    .setValues(1, 2, 3, 4)
                    ).addAttributeGenerator(
                            new AttributeInstanceGenerator<>(CastableSpell.DELAY)
                                    .setValues(1, 2, 3, 4)
                    )
    );
}
