package wbs.wandcraft.spell.dynamic;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.spell.MagicDomain;

import java.util.function.Function;

@NullMarked
public class FixedDomainSpellArchetype extends SpellArchetype {
    private final Function<@Nullable MagicDomain, DynamicSpell> baseBuilder;
    private final MagicDomain primaryDomain;

    public FixedDomainSpellArchetype(String nativeKey, MagicDomain primaryDomain, Component description, Function<@Nullable MagicDomain, DynamicSpell> baseBuilder) {
        super(nativeKey, description);
        this.baseBuilder = baseBuilder;
        this.primaryDomain = primaryDomain;
    }

    public FixedDomainSpellArchetype(NamespacedKey key, MagicDomain primaryDomain, Component description, Function<@Nullable MagicDomain, DynamicSpell> baseBuilder) {
        super(key, description);
        this.baseBuilder = baseBuilder;
        this.primaryDomain = primaryDomain;
    }

    public DynamicSpell build(@Nullable MagicDomain secondary) {
        return baseBuilder.apply(secondary);
    }

    @Override
    public DynamicSpell build(@UnknownNullability MagicDomain primary, @Nullable MagicDomain secondary) {
        return build(secondary);
    }

    public MagicDomain primaryDomain() {
        return primaryDomain;
    }
}
