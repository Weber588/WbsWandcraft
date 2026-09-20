package wbs.wandcraft.spell.dynamic;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.SpellType;

import java.util.function.BiFunction;

@NullMarked
public class SpellAspect implements Keyed {
    private final BiFunction<SpellType, @Nullable SpellType, DynamicSpell> baseBuilder;
    private final NamespacedKey key;

    public SpellAspect(String nativeKey, BiFunction<SpellType, SpellType, DynamicSpell> baseBuilder) {
        this(WbsWandcraft.getKey(nativeKey), baseBuilder);
    }
    public SpellAspect(NamespacedKey key, BiFunction<SpellType, SpellType, DynamicSpell> baseBuilder) {
        this.key = key;
        this.baseBuilder = baseBuilder;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public DynamicSpell build(SpellType primary, @Nullable SpellType secondary) {
        return baseBuilder.apply(primary, secondary);
    }
}
