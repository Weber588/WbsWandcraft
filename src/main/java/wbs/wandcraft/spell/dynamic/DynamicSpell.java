package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Particle;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.SpellTypeModifiers;
import wbs.wandcraft.spell.attributes.attributable.BurnDamageAttributable;
import wbs.wandcraft.spell.attributes.attributable.ParticleAttributable;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.effect.SpellEffectInstance;

import java.util.List;
import java.util.function.Consumer;

@NullMarked
// Add all attributes with non-affecting values that may be used
public abstract class DynamicSpell extends SpellDefinition implements BurnDamageAttributable, ParticleAttributable {
    public static final int SECONDARY_PARTICLE_CHANCE = 5;

    private final String dynamicType;
    private final SpellAspect aspect;

    private static String getStrippedKey(SpellType primary) {
        return primary.getKey().asString().replace(":", "_");
    }

    public DynamicSpell(SpellAspect aspect, SpellType primary, @Nullable SpellType secondary) {
        String dynamicType = aspect.getKey().value();
        super(WbsWandcraft.getKey(
                        "dynamic/" + dynamicType + "/"
                                + getStrippedKey(primary)
                                + (secondary != null ? "_" + getStrippedKey(secondary) : "")
                )
        );
        this.dynamicType = dynamicType;
        this.aspect = aspect;

        addSpellType(primary);
        if (secondary != null) {
            addSpellType(secondary);
        }

        setAttribute(DAMAGE, 0d);
        setAttribute(BURN_TIME, 0d);
    }

    @Override
    public Component displayName() {
        return Component.text(
                WbsStrings.capitalizeAll("Dynamic " + dynamicType.replace("_", " ") + " Spell")
        ).color(
                getPrimarySpellType().textColor()
        );
    }

    @Override
    public Component description() {
        Component description = Component.text("A ")
                .append(getPrimarySpellType().displayName());

        SpellType secondarySpellType = getSecondarySpellType();
        if (secondarySpellType != null) {
            description = description.append(Component.text("/"))
                    .append(secondarySpellType.displayName());
        }

        description = description.append(Component.text(" " + dynamicType + " spell."));

        return description;
    }

    @Override
    public SpellInstance newInstance() {
        SpellInstance newInstance = super.newInstance();

        Multimap<SpellType, SpellEffectInstance<?>> typedEvents = typedEvents();

        spellTypes.forEach(type -> {
            SpellTypeModifiers.getSpellTypeModifiers(type)
                    .stream()
                    .sorted()
                    .forEachOrdered(modifier -> modifier.modify(newInstance));
            typedEvents.get(type).forEach(newInstance::registerEffect);
        });

        return newInstance;
    }

    protected WbsParticleGroup getParticleGroup(WbsParticleEffect effect) {
        return getParticleGroup(effect.clone(), effect.clone());
    }

    protected WbsParticleGroup getParticleGroup(WbsParticleEffect primaryEffect, WbsParticleEffect secondaryEffect) {
        return getParticleGroup(primaryEffect, secondaryEffect, false);
    }
    protected WbsParticleGroup getParticleGroup(WbsParticleEffect primaryEffect, WbsParticleEffect secondaryEffect, boolean directional) {
        WbsParticleGroup particleGroup = new WbsParticleGroup()
                .perEffectChance(true);

        SpellType primarySpellType = getPrimarySpellType();
        SpellType secondarySpellType = getSecondarySpellType();

        primarySpellType.defaultEffect().accept(primaryEffect);
        particleGroup.addEffect(primaryEffect, directional ? primarySpellType.velocityAffectedParticle() : primarySpellType.defaultParticle());

        if (secondarySpellType != null) {
            Consumer<WbsParticleEffect> secondaryModifier = secondarySpellType.defaultEffect();
            secondaryModifier.accept(secondaryEffect);
            particleGroup.addEffect(secondaryEffect, directional ? secondarySpellType.velocityAffectedParticle() : secondarySpellType.defaultParticle(), SECONDARY_PARTICLE_CHANCE);
        } else {
            Particle secondaryParticle = primarySpellType.secondaryParticle();
            Consumer<WbsParticleEffect> secondaryModifier = primarySpellType.secondaryEffect();
            if (secondaryParticle != null) {
                if (secondaryModifier != null) {
                    secondaryModifier.accept(secondaryEffect);
                }
                particleGroup.addEffect(secondaryEffect, secondaryParticle, SECONDARY_PARTICLE_CHANCE);
            }
        }
        return particleGroup;
    }

    protected abstract Multimap<SpellType, SpellEffectInstance<?>> typedEvents();

    @Override
    public Particle getDefaultParticle() {
        return Particle.INSTANT_EFFECT;
    }

    protected List<TextColor> getTypeColours() {
        return spellTypes.stream()
                .map(SpellType::textColor)
                .toList();
    }

    public SpellAspect aspect() {
        return aspect;
    }

    @Override
    public String modelKey() {
        return namespace() + ":dynamic_spell";
    }
}
