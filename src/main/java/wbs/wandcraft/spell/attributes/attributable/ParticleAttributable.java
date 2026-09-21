package wbs.wandcraft.spell.attributes.attributable;

import org.bukkit.Particle;
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
            });

    @AttributableSetupHandler
    default void setupParticles() {
        setAttribute(PARTICLE, getDefaultParticle());
    }

    Particle getDefaultParticle();
    default Particle getParticle(AttributeHolder attributeHolder) {
        return attributeHolder.getAttribute(PARTICLE, getDefaultParticle());
    }
}
