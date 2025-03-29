package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SpellExtensionManager {
    private static final Set<SpellExtensionRegistration<?>> SETUP_EVENTS = new HashSet<>();

    static {
        registerSpellExtension(CastableSpell.class, CastableSpell::setupCastable);
        registerSpellExtension(RangedSpell.class, RangedSpell::setupRanged);
        registerSpellExtension(DirectionalSpell.class, DirectionalSpell::setupDirectional);
        registerSpellExtension(DamageSpell.class, DamageSpell::setUpDamage);
        registerSpellExtension(DurationalSpell.class, DurationalSpell::setUpDurational);
        registerSpellExtension(ParticleSpell.class, ParticleSpell::setupParticles);
        registerSpellExtension(SpeedSpell.class, SpeedSpell::setupSpeed);
        registerSpellExtension(CustomProjectileSpell.class, CustomProjectileSpell::setupCustomProjectile);
        registerSpellExtension(RadiusedSpell.class, RadiusedSpell::setupRadiused);
    }

    public static <T extends ISpellDefinition> void registerSpellExtension(Class<T> clazz, Consumer<T> setupMethod) {
        SETUP_EVENTS.add(new SpellExtensionRegistration<>(clazz, setupMethod));
    }

    public static void setup(SpellDefinition definition) {
        for (SpellExtensionRegistration<?> setupEvent : SETUP_EVENTS) {
            setupEvent.trySetup(definition);
        }
    }

    private record SpellExtensionRegistration<T extends ISpellDefinition>(Class<T> clazz, Consumer<T> setup) {
        public void trySetup(SpellDefinition definition) {
                if (clazz.isInstance(definition)) {
                    T extension = clazz.cast(definition);

                    setup.accept(extension);
                }
            }
        }
}
