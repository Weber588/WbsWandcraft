package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SpellExtensionManager {
    private static final Set<SpellExtensionRegistration<?>> SETUP_EVENTS = new HashSet<>();

    static {
        registerSpellExtension(RangedSpell.class, RangedSpell::setupRanged);
        registerSpellExtension(AbstractProjectileSpell.class, AbstractProjectileSpell::setupProjectile);
        registerSpellExtension(DamageSpell.class, DamageSpell::setUpDamage);
        registerSpellExtension(DurationalSpell.class, DurationalSpell::setUpDurational);
        registerSpellExtension(EntityProjectileSpell.class, EntityProjectileSpell::setupEntityProjectile);
    }

    public static <T extends AbstractSpellDefinition> void registerSpellExtension(Class<T> clazz, Consumer<T> setupMethod) {
        SETUP_EVENTS.add(new SpellExtensionRegistration<>(clazz, setupMethod));
    }

    public static void setup(SpellDefinition definition) {
        for (SpellExtensionRegistration<?> setupEvent : SETUP_EVENTS) {
            setupEvent.trySetup(definition);
        }
    }

    private record SpellExtensionRegistration<T extends AbstractSpellDefinition>(Class<T> clazz, Consumer<T> setup) {
        public void trySetup(SpellDefinition definition) {
                if (clazz.isInstance(definition)) {
                    T extension = clazz.cast(definition);

                    setup.accept(extension);
                }
            }
        }
}
