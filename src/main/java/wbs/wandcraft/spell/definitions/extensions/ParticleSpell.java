package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

import java.util.Objects;

public interface ParticleSpell extends ISpellDefinition {
    SpellAttribute<@NotNull Particle> PARTICLE = new SpellAttribute<>("particle_effect",
            RegisteredPersistentDataType.PARTICLE,
            null,
            stringValue -> WbsEnums.getEnumFromString(Particle.class, stringValue)
    ).addSuggestions(Particle.values())
            .setShowAttribute(Objects::nonNull);

    default void setupParticles() {
        addAttribute(PARTICLE, getDefaultParticle());
    }

    Particle getDefaultParticle();
}
