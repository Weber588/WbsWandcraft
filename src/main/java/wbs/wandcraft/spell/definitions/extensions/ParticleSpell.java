package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.EnumSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface ParticleSpell extends ISpellDefinition {
    SpellAttribute<Particle> PARTICLE = new EnumSpellAttribute<>("particle_effect",
            null,
            RegisteredPersistentDataType.PARTICLE,
            Particle.class
    ).addSuggestions(Particle.values())
            .setShowAttribute((value, attributable) -> {
                if (attributable instanceof ParticleSpell spell) {
                    return value != spell.getDefaultParticle();
                }

                return true;
            })
            .setWritable(true);

    default void setupParticles() {
        setAttribute(PARTICLE, getDefaultParticle());
    }

    Particle getDefaultParticle();
    default Particle getParticle(Attributable attributable) {
        return attributable.getAttribute(PARTICLE, getDefaultParticle());
    }
    @Contract(mutates = "param1")
    default void playEffectSafely(WbsParticleEffect effect, Attributable attributable, Location location) {
        Particle particle = getParticle(attributable);

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
