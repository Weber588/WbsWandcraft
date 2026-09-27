package wbs.wandcraft.spell.dynamic;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.spell.SpellType;

import java.util.function.Function;

public class FixedTypeSpellAspect extends SpellAspect {
    private final Function<@Nullable SpellType, DynamicSpell> baseBuilder;

    public FixedTypeSpellAspect(String nativeKey, Function<@Nullable SpellType, DynamicSpell> baseBuilder) {
        super(nativeKey);
        this.baseBuilder = baseBuilder;
    }

    public FixedTypeSpellAspect(NamespacedKey key, Function<@Nullable SpellType, DynamicSpell> baseBuilder) {
        super(key);
        this.baseBuilder = baseBuilder;
    }

    public DynamicSpell build(@Nullable SpellType secondary) {
        return baseBuilder.apply(secondary);
    }

}
