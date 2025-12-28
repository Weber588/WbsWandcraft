package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import wbs.wandcraft.RegisteredPersistentDataType;
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
}
