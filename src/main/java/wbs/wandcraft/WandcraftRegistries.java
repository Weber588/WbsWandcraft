package wbs.wandcraft;

import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.equipment.hat.*;
import wbs.wandcraft.generation.AttributeModifierGenerator;
import wbs.wandcraft.generation.SpellInstanceGenerator;
import wbs.wandcraft.generation.WandGenerator;
import wbs.wandcraft.learning.*;
import wbs.wandcraft.spell.NativeSpellLoader;
import wbs.wandcraft.spell.SpellLoader;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.SpellDefinition;
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
        WandType.MAGE,
        WandType.WILDEN,
        WandType.BARBARIAN
    );

    public static final WbsRegistry<WandTexture> WAND_TEXTURES = new WbsRegistry<>(
            WandTexture.BASIC,
            WandTexture.MAGE,
            WandTexture.WIZARDRY,
            WandTexture.SORCERY,
            WandTexture.TRIDENT,
            WandTexture.FIRE,
            WandTexture.WILDEN,
            WandTexture.BARBARIAN,
            WandTexture.MIMIC
    );

    public static final WbsRegistry<HatModel> HAT_TEXTURES = new WbsRegistry<>(
            HatModel.WITCH,
            HatModel.APPRENTICE,
            HatModel.ARCANIST,
            HatModel.DRUID,
            HatModel.FIREMANCER,
            HatModel.HEALER,
            HatModel.MARKSMAN,
            HatModel.OLD,
            HatModel.SEER,
            HatModel.SORCERER,
            HatModel.SPEEDSTER,
            HatModel.SPELLSLINGER,
            HatModel.WARLOCK
    );

    public static final WbsRegistry<MagicEquipmentType> MAGIC_EQUIPMENT_TYPES = new WbsRegistry<>(
            new WitchHat(),
            new ApprenticeHat(),
            new ArcanistHat(),
            new DruidHat(),
            new FiremancerHat(),
            new HealerHat(),
            new MarksmanHat(),
            new OldHat(),
            new SeerHat(),
            new SorcererHat(),
            new SpeedsterHat(),
            new SpellslingerHat(),
            new WarlockHat()
    );

    public static final WbsRegistry<SpellType> SPELL_TYPES = new WbsRegistry<>();
    public static final WbsRegistry<SpellDefinition> SPELLS = new WbsRegistry<>(SpellLoader.loadSpells(new NativeSpellLoader()));
    public static final WbsRegistry<LearningMethodType<?>> LEARNING_PROVIDERS = new WbsRegistry<>(
            LearningMethodType.build("advancements", AdvancementLearningTrigger::new),
            LearningMethodType.build("deal-damage", DealDamageLearningTrigger::new),
            LearningMethodType.build("enter-structure", EnterStructureLearningTrigger::new),
            LearningMethodType.build("kill", KillLearningTrigger::new),
            LearningMethodType.build("take-damage", TakeDamageLearningTrigger::new),
            LearningMethodType.build("place-blocks", PlaceBlockLearningTrigger::new)
    );
    public static final WbsRegistry<StatusEffect> STATUS_EFFECTS = new WbsRegistry<>(
            StatusEffectManager.GLIDING,
            StatusEffectManager.STUNNED,
            StatusEffectManager.PLANAR_BINDING,
            StatusEffectManager.DEATH_WALK,
            StatusEffectManager.CHARMED,
            StatusEffectManager.POLYMORPHED,
            StatusEffectManager.HOLD,
            StatusEffectManager.TRANQUILIZED,
            StatusEffectManager.DISGUISED,
            StatusEffectManager.INVISIBLE,
            StatusEffectManager.NATURE_PHASING
    );
    public static final WbsRegistry<SpellEffectDefinition<?>> EFFECTS = new WbsRegistry<>(
            new ForcePullEffect(),
            new CastSpellEffect()
    );
    public static final WbsRegistry<WandGenerator> WAND_GENERATORS = new WbsRegistry<>();
    public static final WbsRegistry<SpellInstanceGenerator> SPELL_GENERATORS = new WbsRegistry<>();
    public static final WbsRegistry<AttributeModifierGenerator<?>> MODIFIER_GENERATORS = new WbsRegistry<>();
}
