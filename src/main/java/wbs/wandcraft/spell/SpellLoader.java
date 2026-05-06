package wbs.wandcraft.spell;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellDefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class SpellLoader {
    public static Iterable<SpellDefinition> loadSpells(SpellLoader loader) {
        List<SpellDefinition> loaded = new ArrayList<>();

        Multimap<String, PluginDependentLoader> requiredPlugins = LinkedHashMultimap.create();
        List<Loader> entries = loader.getEntries();
        for (Loader entry : entries) {
            if (entry instanceof PluginDependentLoader pdLoader) {
                requiredPlugins.put(pdLoader.requiredPlugin(), pdLoader);
            } else {
                if (entry.canLoad()) {
                    loaded.add(entry.construct());
                }
            }
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        for (String pluginName : requiredPlugins.keySet()) {
            if (pluginManager.getPlugin(pluginName) != null) {
                for (PluginDependentLoader entry : requiredPlugins.get(pluginName)) {
                    if (entry.canLoad()) {
                        loaded.add(entry.construct());
                    }
                }
            } else {
                WbsWandcraft.getInstance().getLogger().warning("The following spells require the " + pluginName + " plugin:");
                for (PluginDependentLoader entry : requiredPlugins.get(pluginName)) {
                    WbsWandcraft.getInstance().getLogger().warning("\t- " + entry.className());
                }
            }
        }

        WbsWandcraft.getInstance().getLogger().info("Loaded " +
                loaded.size() + " out of " + entries.size() + " spells from " + loader.getClass().getSimpleName() + ".");

        return loaded;
    }

    protected abstract List<Loader> getEntries();

    public interface Loader {
        boolean canLoad();

        Supplier<? extends SpellDefinition> constructor();
        default SpellDefinition construct() {
            return constructor().get();
        }
    }

    public record ConcreteLoader<T extends SpellDefinition>(Supplier<T> constructor) implements Loader {
        @Override
        public boolean canLoad() {
            return true;
        }

        public T construct() {
            return constructor.get();
        }
    }

    public record PluginDependentLoader(String className, String requiredPlugin) implements Loader {
        @Override
        public boolean canLoad() {
            Class<?> spellClass = getSpellClass();

            return spellClass == null;
        }

        private @Nullable Class<?> getSpellClass() {
            Class<?> spellClass;
            try {
                spellClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                spellClass = null;
            }

            if (spellClass != null) {
                if (!SpellDefinition.class.isAssignableFrom(spellClass)) {
                    throw new IllegalStateException("Class " + spellClass + " must extend SpellDefinition");
                }
            }

            return spellClass;
        }

        @Override
        public Supplier<? extends SpellDefinition> constructor() {
            return () -> {
                try {
                    return (SpellDefinition) Objects.requireNonNull(getSpellClass(), "Spell class was null in constructor!")
                            .getConstructor()
                            .newInstance();
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }
}
