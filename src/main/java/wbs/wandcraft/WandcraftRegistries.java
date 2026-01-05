package wbs.wandcraft;

import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.generation.AttributeModifierGenerator;
import wbs.wandcraft.generation.SpellInstanceGenerator;
import wbs.wandcraft.generation.WandGenerator;
import wbs.wandcraft.learning.*;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.*;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spell.event.CastSpellEffect;
import wbs.wandcraft.spell.event.ForcePullEffect;
import wbs.wandcraft.spell.event.SpellEffectDefinition;
import wbs.wandcraft.wand.WandTexture;
import wbs.wandcraft.wand.types.WandType;

public class WandcraftRegistries {
    public static final WbsRegistry<RegisteredPersistentDataType<?>> DATA_TYPES = new WbsRegistry<>(
            RegisteredPersistentDataType.INTEGER,
            RegisteredPersistentDataType.BOOLEAN,
            RegisteredPersistentDataType.DOUBLE,
            RegisteredPersistentDataType.STRING,
            RegisteredPersistentDataType.LONG,
            RegisteredPersistentDataType.PARTICLE,
            RegisteredPersistentDataType.MATERIAL,
            RegisteredPersistentDataType.TARGETER,
            RegisteredPersistentDataType.SPELL
    );
    public static final WbsRegistry<SpellAttribute<?>> ATTRIBUTES = new WbsRegistry<>();
    public static final WbsRegistry<AttributeModifierType> MODIFIER_TYPES = new WbsRegistry<>(
            AttributeModifierType.SET,
            AttributeModifierType.ADD,
            AttributeModifierType.MULTIPLY
    );

    public static final WbsRegistry<WandType<?>> WAND_TYPES = new WbsRegistry<>(
        WandType.BASIC,
        WandType.WIZARDRY,
        WandType.SORCERY,
        WandType.MAGE
    );

    public static final WbsRegistry<WandTexture> WAND_TEXTURES = new WbsRegistry<>(
            WandTexture.BASIC,
            WandTexture.MAGE,
            WandTexture.WIZARDRY,
            WandTexture.SORCERY,
            WandTexture.TRIDENT,
            WandTexture.FIRE,
            WandTexture.OVERGROWN
    );
    public static final WbsRegistry<SpellType> SPELL_TYPES = new WbsRegistry<>();
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
            new WindWalkSpell(),
            new CrowsCallSpell(),
            new StunSpell(),
            new FireBreathSpell(),
            new ArcaneSparkSpell(),
            new PlanarBindingSpell(),
            new DiscoverItemSpell(),
            new DeathWalkSpell(),
            new ChainLightningSpell(),
            new CharmSpell(),
            new DisplaceSpell(),
            new ArcaneBurstSpell(),
            new ShieldSpell(),
            new CarveSpell()
    );
    public static final WbsRegistry<LearningMethodType<?>> LEARNING_PROVIDERS = new WbsRegistry<>(
            LearningMethodType.build("advancements", AdvancementLearningTrigger::new),
            LearningMethodType.build("deal-damage", DealDamageLearningTrigger::new),
            LearningMethodType.build("enter-structure", EnterStructureLearningTrigger::new),
            LearningMethodType.build("kill", KillLearningTrigger::new),
            LearningMethodType.build("take-damage", TakeDamageLearningTrigger::new)
    );
    public static final WbsRegistry<StatusEffect> STATUS_EFFECTS = new WbsRegistry<>(
            StatusEffect.GLIDING,
            StatusEffect.STUNNED,
            StatusEffect.PLANAR_BINDING,
            StatusEffect.DEATH_WALK,
            StatusEffect.CHARMED
    );
    public static final WbsRegistry<SpellEffectDefinition<?>> EFFECTS = new WbsRegistry<>(
            new ForcePullEffect(),
            new CastSpellEffect()
    );
    public static final WbsRegistry<WandGenerator> WAND_GENERATORS = new WbsRegistry<>();
    public static final WbsRegistry<SpellInstanceGenerator> SPELL_GENERATORS = new WbsRegistry<>();
    public static final WbsRegistry<AttributeModifierGenerator<?>> MODIFIER_GENERATORS = new WbsRegistry<>();
}
