package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.brigadier.argument.WbsEnumArgumentType;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;
import wbs.wandcraft.util.CustomPersistentDataTypes;

public interface ParticleSpell extends AbstractSpellDefinition {
    SpellAttribute<@NotNull Particle> PARTICLE = new SpellAttribute<>("particle_effect",
            new CustomPersistentDataTypes.PersistentEnumType<>(Particle.class),
            new WbsEnumArgumentType<>(Particle.class),
            null,
            stringValue -> WbsEnums.getEnumFromString(Particle.class, stringValue))
            .addSuggestions(Particle.values());

    default void setupParticles() {
        addAttribute(PARTICLE);
    }

    Particle getDefaultParticle();
}
