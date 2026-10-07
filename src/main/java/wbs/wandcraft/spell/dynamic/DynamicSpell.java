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
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.MagicDomainModifiers;
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
    private final SpellArchetype archetype;

    private static String getStrippedKey(MagicDomain primary) {
        return primary.getKey().asString().replace(":", "_");
    }

    public DynamicSpell(SpellArchetype archetype, MagicDomain primary, @Nullable MagicDomain secondary) {
        String dynamicType = archetype.getKey().value();
        super(WbsWandcraft.getKey(
                        "dynamic/" + dynamicType + "/"
                                + getStrippedKey(primary)
                                + (secondary != null ? "_" + getStrippedKey(secondary) : "")
                )
        );
        this.dynamicType = dynamicType;
        this.archetype = archetype;

        addMagicDomain(primary);
        if (secondary != null) {
            addMagicDomain(secondary);
        }

        setAttribute(DAMAGE, 0d);
        setAttribute(BURN_TIME, 0d);
    }

    @Override
    public Component displayName() {
        return Component.text(
                WbsStrings.capitalizeAll("Dynamic " + dynamicType.replace("_", " ") + " Spell")
        ).color(
                getPrimaryDomain().textColor()
        );
    }

    @Override
    public Component description() {
        Component description = Component.text("A ")
                .append(getPrimaryDomain().displayName());

        MagicDomain secondaryMagicDomain = getSecondaryDomain();
        if (secondaryMagicDomain != null) {
            description = description.append(Component.text("/"))
                    .append(secondaryMagicDomain.displayName());
        }

        description = description.append(Component.text(" " + dynamicType + " spell."));

        return description;
    }

    @Override
    public SpellInstance newInstance() {
        SpellInstance newInstance = super.newInstance();

        Multimap<MagicDomain, SpellEffectInstance<?>> typedEvents = typedEvents();

        magicDomains.forEach(type -> {
            MagicDomainModifiers.getMagicDomainModifiers(type)
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

        MagicDomain primaryMagicDomain = getPrimaryDomain();
        MagicDomain secondaryMagicDomain = getSecondaryDomain();

        primaryMagicDomain.defaultEffect().accept(primaryEffect);
        particleGroup.addEffect(primaryEffect, directional ? primaryMagicDomain.velocityAffectedParticle() : primaryMagicDomain.defaultParticle());

        if (secondaryMagicDomain != null) {
            Consumer<WbsParticleEffect> secondaryModifier = secondaryMagicDomain.defaultEffect();
            secondaryModifier.accept(secondaryEffect);
            particleGroup.addEffect(secondaryEffect, directional ? secondaryMagicDomain.velocityAffectedParticle() : secondaryMagicDomain.defaultParticle(), SECONDARY_PARTICLE_CHANCE);
        } else {
            Particle secondaryParticle = primaryMagicDomain.secondaryParticle();
            Consumer<WbsParticleEffect> secondaryModifier = primaryMagicDomain.secondaryEffect();
            if (secondaryParticle != null) {
                if (secondaryModifier != null) {
                    secondaryModifier.accept(secondaryEffect);
                }
                particleGroup.addEffect(secondaryEffect, secondaryParticle, SECONDARY_PARTICLE_CHANCE);
            }
        }
        return particleGroup;
    }

    protected abstract Multimap<MagicDomain, SpellEffectInstance<?>> typedEvents();

    @Override
    public Particle getDefaultParticle() {
        return Particle.INSTANT_EFFECT;
    }

    protected List<TextColor> getTypeColours() {
        return magicDomains.stream()
                .map(MagicDomain::textColor)
                .toList();
    }

    public SpellArchetype archetype() {
        return archetype;
    }

    @Override
    public String modelKey() {
        return namespace() + ":dynamic_spell";
    }
}
