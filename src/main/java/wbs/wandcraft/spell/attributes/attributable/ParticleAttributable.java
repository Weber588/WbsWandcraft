package wbs.wandcraft.spell.attributes.attributable;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.EnumSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

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
            })
            .setWritable(true);

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
        Particle particle = getParticle(attributeHolder);

        Class<?> dataType = particle.getDataType();

        if (effect.getData() == null && dataType != Void.class) {
            if (dataType == Float.class) {
                effect.setData(0f);
            } else if (dataType == Integer.class) {
                effect.setData(0);
            } else if (dataType == Color.class) {
                effect.setData(Color.fromRGB(255, 100, 255));
            } else if (dataType == BlockData.class) {
                effect.setData(Material.BEDROCK.createBlockData());
            }
        }

        effect.play(particle, location);
    }
}
