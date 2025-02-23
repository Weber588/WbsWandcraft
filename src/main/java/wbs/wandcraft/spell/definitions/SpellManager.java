package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

public class SpellManager {
    private static final Map<Key, SpellDefinition> REGISTERED_SPELLS = new HashMap<>();

    public static void register(SpellDefinition definition) {
        REGISTERED_SPELLS.put(definition.getKey(), definition);
    }

    static {
        register(new FireballSpell());
    }

    public static SpellDefinition get(NamespacedKey definitionKey) {
        return REGISTERED_SPELLS.get(definitionKey);
    }
}
