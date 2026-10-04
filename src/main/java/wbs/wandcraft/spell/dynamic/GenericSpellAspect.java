package wbs.wandcraft.spell.dynamic;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.spell.SpellType;

import java.util.function.BiFunction;

public class GenericSpellAspect extends SpellAspect {
    private final BiFunction<SpellType, @Nullable SpellType, DynamicSpell> baseBuilder;

    public GenericSpellAspect(String nativeKey, Component description, BiFunction<SpellType, @Nullable SpellType, DynamicSpell> baseBuilder) {
        super(nativeKey, description);
        this.baseBuilder = baseBuilder;
    }

    public GenericSpellAspect(NamespacedKey key, Component description, BiFunction<SpellType, @Nullable SpellType, DynamicSpell> baseBuilder) {
        super(key, description);
        this.baseBuilder = baseBuilder;
    }

    public DynamicSpell build(SpellType primary, @Nullable SpellType secondary) {
        return baseBuilder.apply(primary, secondary);
    }

}
