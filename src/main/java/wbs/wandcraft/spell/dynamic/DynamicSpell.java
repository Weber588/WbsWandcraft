package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
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

import java.util.function.Consumer;

// Add all attributes with non-affecting values that may be used
public abstract class DynamicSpell extends SpellDefinition implements BurnDamageAttributable, ParticleAttributable {
    private final String dynamicType;

    private static String getStrippedKey(SpellType primary) {
        return primary.getKey().asString().replace(":", "_");
    }

    public DynamicSpell(String dynamicType, SpellType primary, @Nullable SpellType secondary) {
        super(WbsWandcraft.getKey(
                        "dynamic/" + dynamicType + "/"
                                + getStrippedKey(primary)
                                + (secondary != null ? "_" + getStrippedKey(secondary) : "")
                )
        );
        this.dynamicType = dynamicType;

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
    @NotNull
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

    @NotNull
    protected WbsParticleGroup getParticleGroup(WbsParticleEffect effect) {
        return getParticleGroup(effect.clone(), effect.clone());
    }

    @NotNull
    protected WbsParticleGroup getParticleGroup(WbsParticleEffect primaryEffect, WbsParticleEffect secondaryEffect) {
        WbsParticleGroup particleGroup = new WbsParticleGroup();

        SpellType primarySpellType = getPrimarySpellType();
        SpellType secondarySpellType = getSecondarySpellType();

        primarySpellType.defaultEffect().accept(primaryEffect);
        particleGroup.addEffect(primaryEffect, primarySpellType.defaultParticle());

        if (secondarySpellType != null) {
            Consumer<WbsParticleEffect> secondaryModifier = secondarySpellType.defaultEffect();
            secondaryModifier.accept(secondaryEffect);
            particleGroup.addEffect(secondaryEffect, secondarySpellType.defaultParticle(), 5);
        } else {
            Particle secondaryParticle = primarySpellType.secondaryParticle();
            Consumer<WbsParticleEffect> secondaryModifier = primarySpellType.secondaryEffect();
            if (secondaryParticle != null) {
                if (secondaryModifier != null) {
                    secondaryModifier.accept(secondaryEffect);
                }
                particleGroup.addEffect(secondaryEffect, secondaryParticle, 5);
            }
        }
        return particleGroup;
    }

    protected abstract Multimap<SpellType, SpellEffectInstance<?>> typedEvents();

    @Override
    public Particle getDefaultParticle() {
        return Particle.INSTANT_EFFECT;
    }
}
