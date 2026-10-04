package wbs.wandcraft.spell.dynamic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsKeyed;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.MenuUtils;

@NullMarked
public abstract class SpellAspect implements Keyed {
    private final NamespacedKey key;
    private final Component description;

    public SpellAspect(String nativeKey, Component description) {
        this(WbsWandcraft.getKey(nativeKey), description);
    }
    public SpellAspect(NamespacedKey key, Component description) {
        this.key = key;
        this.description = description;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public Component displayName() {
        Component displayName = Component.text(WbsKeyed.toPrettyString(this));

        TextColor color;
        if (this instanceof FixedTypeSpellAspect fixed) {
            color = fixed.primaryType().textColor();
        } else {
            color = MenuUtils.DEFAULT_TITLE_COLOUR;
        }
        displayName = displayName.color(color);

        return displayName;
    }

    public Component description() {
        return description;
    }
}
