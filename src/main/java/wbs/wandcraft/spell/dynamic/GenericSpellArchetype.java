package wbs.wandcraft.spell.dynamic;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.spell.MagicDomain;

import java.util.function.BiFunction;

@NullMarked
public class GenericSpellArchetype extends SpellArchetype {
    private final BiFunction<MagicDomain, @Nullable MagicDomain, DynamicSpell> baseBuilder;

    public GenericSpellArchetype(String nativeKey, Component description, BiFunction<MagicDomain, @Nullable MagicDomain, DynamicSpell> baseBuilder) {
        super(nativeKey, description);
        this.baseBuilder = baseBuilder;
    }

    public GenericSpellArchetype(NamespacedKey key, Component description, BiFunction<MagicDomain, @Nullable MagicDomain, DynamicSpell> baseBuilder) {
        super(key, description);
        this.baseBuilder = baseBuilder;
    }

    public DynamicSpell build(MagicDomain primary, @Nullable MagicDomain secondary) {
        return baseBuilder.apply(primary, secondary);
    }
}
