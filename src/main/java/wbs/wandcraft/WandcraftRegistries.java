package wbs.wandcraft;

import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.generator.AttributeInstanceGenerator;
import wbs.wandcraft.generator.AttributeModifierGenerator;
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
    public static final WbsRegistry<RegisteredPersistentDataType<?>> DATA_TYPES = new WbsRegistry<>(
            RegisteredPersistentDataType.INTEGER,
            RegisteredPersistentDataType.BOOLEAN,
            RegisteredPersistentDataType.DOUBLE,
            RegisteredPersistentDataType.STRING,
            RegisteredPersistentDataType.LONG
    );
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
            new StunSpell(),
            new FireBreathSpell(),
            new ArcaneSparkSpell(),
            new PlanarBindingSpell(),
            new DiscoverItemSpell(),
            new DeathWalkSpell(),
            new ChainLightningSpell()
    );
    public static final WbsRegistry<StatusEffect> STATUS_EFFECTS = new WbsRegistry<>(
            StatusEffect.GLIDING,
            StatusEffect.STUNNED,
            StatusEffect.PLANAR_BINDING,
            StatusEffect.DEATH_WALK
    );
    public static final WbsRegistry<WandGenerator> WAND_GENERATORS = new WbsRegistry<>(
            new WandGenerator(WbsWandcraft.getKey("example"), 1, 2, -1, 0, 1, 1, 3)
                    .addAttributeGenerator(
                            new AttributeInstanceGenerator<>(Wand.COOLDOWN)
                                    .setValues(5, 10, 10, 15, 20)
                    ).addModifierGenerator(
                            new AttributeModifierGenerator<>(CastableSpell.COST, AttributeModifierType.MULTIPLY)
                                    .setValues(RegisteredPersistentDataType.DOUBLE, 0.5, 0.75, 0.9, 1.1)
                    ).addModifierGenerator(
                            new AttributeModifierGenerator<>(CastableSpell.DELAY, AttributeModifierType.MULTIPLY)
                                    .setValues(RegisteredPersistentDataType.DOUBLE, 0.5, 0.75, 0.9, 1.1)
                    )
                    .addSpellGenerator(
                            new SpellInstanceGenerator()
                                    .addAttributeGenerator(
                                            new AttributeModifierGenerator<>(CustomProjectileSpell.GRAVITY, AttributeModifierType.MULTIPLY)
                                                    .setValues(RegisteredPersistentDataType.DOUBLE, 0.5d, 0.8d, 1.2d)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeModifierGenerator<>(SpeedSpell.SPEED, AttributeModifierType.MULTIPLY)
                                                    .setValues(RegisteredPersistentDataType.DOUBLE, 0.5d, 0.8d, 1.2d, 2d)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeModifierGenerator<>(CustomProjectileSpell.BOUNCES, AttributeModifierType.ADD)
                                                    .setValues(RegisteredPersistentDataType.INTEGER, 0, 0, 0, 1, 5)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeModifierGenerator<>(RangedSpell.RANGE, AttributeModifierType.MULTIPLY)
                                                    .setValues(RegisteredPersistentDataType.DOUBLE, 0.5d, 0.8d, 1.2d, 2d)
                                    )
                                    .addAttributeGenerator(
                                            new AttributeModifierGenerator<>(IProjectileSpell.IMPRECISION, AttributeModifierType.MULTIPLY)
                                                    .setValues(RegisteredPersistentDataType.DOUBLE, 0.5d, 0.8d, 1.2d, 2d)
                                    )
                    )
    );
}
