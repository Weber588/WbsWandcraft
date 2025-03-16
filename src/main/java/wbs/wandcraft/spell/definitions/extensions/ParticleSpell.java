package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;
import wbs.wandcraft.util.CustomPersistentDataTypes;

import java.util.Objects;

public interface ParticleSpell extends AbstractSpellDefinition {
    SpellAttribute<@NotNull Particle> PARTICLE = new SpellAttribute<>("particle_effect",
            new CustomPersistentDataTypes.PersistentEnumType<>(Particle.class),
            null,
            stringValue -> WbsEnums.getEnumFromString(Particle.class, stringValue))
            .addSuggestions(Particle.values())
            .setShowAttribute(Objects::nonNull);

    default void setupParticles() {
        addAttribute(PARTICLE);
    }

    Particle getDefaultParticle();
}
