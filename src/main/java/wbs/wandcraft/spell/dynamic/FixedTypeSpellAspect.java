package wbs.wandcraft.spell.dynamic;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.spell.SpellType;

import java.util.function.Function;

@NullMarked
public class FixedTypeSpellAspect extends SpellAspect {
    private final Function<@Nullable SpellType, DynamicSpell> baseBuilder;
    private final SpellType primaryType;

    public FixedTypeSpellAspect(String nativeKey, SpellType primaryType, Component description, Function<@Nullable SpellType, DynamicSpell> baseBuilder) {
        super(nativeKey, description);
        this.baseBuilder = baseBuilder;
        this.primaryType = primaryType;
    }

    public FixedTypeSpellAspect(NamespacedKey key, SpellType primaryType, Component description, Function<@Nullable SpellType, DynamicSpell> baseBuilder) {
        super(key, description);
        this.baseBuilder = baseBuilder;
        this.primaryType = primaryType;
    }

    public DynamicSpell build(@Nullable SpellType secondary) {
        return baseBuilder.apply(secondary);
    }

    @Override
    public DynamicSpell build(@UnknownNullability SpellType primary, @Nullable SpellType secondary) {
        return build(secondary);
    }

    public SpellType primaryType() {
        return primaryType;
    }
}
