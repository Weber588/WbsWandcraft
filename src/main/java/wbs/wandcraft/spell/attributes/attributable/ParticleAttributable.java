package wbs.wandcraft.spell.attributes.attributable;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.EnumSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.SpellType;

public interface ParticleAttributable extends AttributeHolder {
    SpellAttribute<Particle> PARTICLE = new EnumSpellAttribute<>("particle_effect",
            null,
            AttributeDataType.PARTICLE,
            Particle.class
    ).addSuggestions(Particle.values())
            .setShowAttribute((val, attributable) -> {
                if (attributable instanceof ParticleAttributable spell) {
                    return val != spell.getDefaultParticle();
                }

                return true;
            });

    @AttributableSetupHandler
    default void setupParticles() {
        setAttribute(PARTICLE, getDefaultParticle());
    }

    Particle getDefaultParticle();
    default Particle getParticle(AttributeHolder attributeHolder) {
        return attributeHolder.getAttribute(PARTICLE, getDefaultParticle());
    }
    @Contract(mutates = "param1")
    default void playEffectSafely(WbsParticleEffect effect, AttributeHolder attributeHolder, Location location) {
        playEffectSafely(effect, attributeHolder, location, this);
    }

    @Contract(mutates = "param1")
    static void playEffectSafely(WbsParticleEffect effect, AttributeHolder attributeHolder, Location location, ParticleAttributable source) {
        Particle particle = source.getParticle(attributeHolder);

        playEffectSafely(effect, location, particle, source);
    }

    @Contract(mutates = "param1")
    static void playEffectSafely(WbsParticleEffect effect, Location location, Particle particle, AttributeHolder source) {
        Class<?> dataType = particle.getDataType();
        Object oldData = effect.getData();
        Object newData = oldData;

        if (oldData == null || dataType.isAssignableFrom(oldData.getClass())) {
            Color primaryColour;
            Color secondaryColour;

            if (source instanceof SpellDefinition spellDefinition) {
                SpellType primary = spellDefinition.getPrimarySpellType();
                @Nullable SpellType secondary = spellDefinition.getSecondarySpellType();

                primaryColour = primary.color();
                secondaryColour = secondary != null ? secondary.color() : primary.mulColor(0.5);
            } else {
                primaryColour = Color.fromRGB(255, 100, 255);
                secondaryColour = Color.fromRGB(127, 50, 127);
            }

            if (dataType == Float.class) {
                newData = 0f;
            } else if (dataType == Integer.class) {
                newData = 0;
            } else if (dataType == BlockData.class) {
                newData = Material.BEDROCK.createBlockData();
            } else if (dataType == Color.class) {
                newData = primaryColour;
            } else if (dataType == Particle.DustOptions.class) {
                newData =new Particle.DustOptions(primaryColour, 1f);
            } else if (dataType == Particle.Spell.class) {
                newData =new Particle.Spell(primaryColour, 1f);
            } else if (dataType == Particle.DustTransition.class) {
                newData = new Particle.DustTransition(primaryColour, secondaryColour, 1);
            } else if (dataType == Particle.Trail.class) {
                newData = new Particle.Trail(location, primaryColour, 20);
            } else if (dataType == Vibration.class) {
                newData = new Vibration(new Vibration.Destination.BlockDestination(location), 20);
            }
            // I'm not doing geysers lmao
        }

        effect.setData(newData);
        effect.play(particle, location);
        effect.setData(oldData);
    }
}
