package wbs.wandcraft.spell.dynamic;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.spell.SpellType;

import java.util.function.BiFunction;

public class GenericSpellAspect extends SpellAspect {
    private final BiFunction<SpellType, @Nullable SpellType, DynamicSpell> baseBuilder;

    public GenericSpellAspect(String nativeKey, BiFunction<SpellType, @Nullable SpellType, DynamicSpell> baseBuilder) {
        super(nativeKey);
        this.baseBuilder = baseBuilder;
    }

    public GenericSpellAspect(NamespacedKey key, BiFunction<SpellType, @Nullable SpellType, DynamicSpell> baseBuilder) {
        super(key);
        this.baseBuilder = baseBuilder;
    }

    public DynamicSpell build(SpellType primary, @Nullable SpellType secondary) {
        return baseBuilder.apply(primary, secondary);
    }

}
