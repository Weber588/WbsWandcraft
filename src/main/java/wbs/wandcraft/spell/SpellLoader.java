package wbs.wandcraft.spell;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public abstract class SpellLoader {
    public static Iterable<SpellDefinition> loadSpells(SpellLoader loader) {
        List<SpellDefinition> loaded = new ArrayList<>();

        Multimap<String, Loader<?>> requiredPlugins = LinkedHashMultimap.create();
        Set<Loader<?>> entries = loader.getEntries();
        for (Loader<?> entry : entries) {
            String requiredPlugin = entry.getRequiredPlugin();
            if (requiredPlugin == null) {
                SpellDefinition constructed = entry.construct();
                loaded.add(constructed);
            } else {
                requiredPlugins.put(requiredPlugin, entry);
            }
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        for (String pluginName : requiredPlugins.keySet()) {
            if (pluginManager.getPlugin(pluginName) != null) {
                requiredPlugins.get(pluginName).forEach(entry -> {
                    SpellDefinition constructed = entry.construct();
                    loaded.add(constructed);
                });
            } else {
                WbsWandcraft.getInstance().getLogger().warning("The following spells require the " + pluginName + " plugin:");
                for (Loader<?> entry : requiredPlugins.get(pluginName)) {
                    WbsWandcraft.getInstance().getLogger().warning("\t- " + entry.spellClass().getCanonicalName());
                }
            }
        }

        WbsWandcraft.getInstance().getLogger().info("Loaded " +
                loaded.size() + " out of " + entries.size() + " spells from " + loader.getClass().getSimpleName() + ".");

        return loaded;
    }

    protected abstract Set<Loader<?>> getEntries();

    public record Loader<T extends SpellDefinition>(Class<T> spellClass, Supplier<T> constructor) {
        public @Nullable String getRequiredPlugin() {
            RequiresPlugin requiresPlugin = spellClass.getAnnotation(RequiresPlugin.class);

            if (requiresPlugin != null) {
                return requiresPlugin.value();
            }

            return null;
        }

        public T construct() {
            return constructor.get();
        }
    }
}
