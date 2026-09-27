package wbs.wandcraft.spell.dynamic;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public abstract class SpellAspect implements Keyed {
    private final NamespacedKey key;

    public SpellAspect(String nativeKey) {
        this(WbsWandcraft.getKey(nativeKey));
    }
    public SpellAspect(NamespacedKey key) {
        this.key = key;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
