package wbs.wandcraft.spell.dynamic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsKeyed;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.util.MenuUtils;

@NullMarked
public abstract class SpellArchetype implements Keyed {
    private final NamespacedKey key;
    private final Component description;

    public SpellArchetype(String nativeKey, Component description) {
        this(WbsWandcraft.getKey(nativeKey), description);
    }
    public SpellArchetype(NamespacedKey key, Component description) {
        this.key = key;
        this.description = description;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public Component displayName() {
        Component displayName = Component.text(WbsKeyed.toPrettyString(this));

        Style style;
        if (this instanceof FixedDomainSpellArchetype fixed) {
            style = Style.style(fixed.primaryDomain().textColor());
        } else {
            style = MenuUtils.DEFAULT_TITLE_STYLE;
        }
        displayName = displayName.style(style);

        return displayName;
    }

    public Component description() {
        return description;
    }

    public abstract DynamicSpell build(@UnknownNullability MagicDomain primary, @Nullable MagicDomain secondary);
}
