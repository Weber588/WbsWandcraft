package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.spellbook.Spellbook;

import java.util.LinkedList;
import java.util.List;

public abstract class MagicHat implements MagicEquipmentType {
    private final String hatName;
    private final NamespacedKey key;
    private final HatModel model;

    public MagicHat(String hatName, HatModel model) {
        key = WbsWandcraft.getKey("hat_" + hatName);
        this.hatName = hatName;
        this.model = model;
    }

    @Override
    public @Nullable final Component getItemName() {
        return Component.text(getName()).color(getNameColour());
    }

    protected @NotNull String getName() {
        return WbsStrings.capitalizeAll(hatName) + "'s Hat";
    }

    protected abstract @NotNull TextColor getNameColour();

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public HatModel getModel() {
        return model;
    }

    @Override
    public final @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>(getEffectsLore());

        String credit = getCredit();

        if (credit != null) {
            lore.add(Component.empty());
            lore.add(Component.text("Artist: ").color(Spellbook.DESCRIPTION_COLOR).append(Component.text(credit).decorate(TextDecoration.ITALIC)));
        }

        return lore;
    }

    protected @Nullable String getCredit() {
        return "SolariumYT";
    }

    public abstract List<Component> getEffectsLore();

    // TODO: Add passive particle effects
}
