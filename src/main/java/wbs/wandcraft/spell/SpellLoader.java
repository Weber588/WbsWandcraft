package wbs.wandcraft.spell;

import org.bukkit.Bukkit;
import wbs.wandcraft.spell.definitions.SpellDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public abstract class SpellLoader {
    public static Iterable<SpellDefinition> loadSpells(SpellLoader loader) {
        List<SpellDefinition> list = new ArrayList<>();
        for (Loader<?> spellRegistrationEntry : loader.getEntries()) {
            if (spellRegistrationEntry.shouldLoad()) {
                SpellDefinition construct = spellRegistrationEntry.construct();
                list.add(construct);
            }
        }
        return list;
    }

    protected abstract Set<Loader<?>> getEntries();


    public record Loader<T extends SpellDefinition>(Class<T> spellClass, Supplier<T> constructor) {
        public boolean shouldLoad() {
            RequiresPlugin requiresPlugin = spellClass.getAnnotation(RequiresPlugin.class);

            if (requiresPlugin != null) {
                return Bukkit.getPluginManager().isPluginEnabled(requiresPlugin.value());
            }

            return true;
        }

        public T construct() {
            return constructor.get();
        }
    }
}
