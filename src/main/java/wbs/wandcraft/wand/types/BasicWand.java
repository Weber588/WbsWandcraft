package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("UnstableApiUsage")
public class BasicWand extends Wand {
    private @Nullable ItemStack item;

    public BasicWand(@NotNull String uuid) {
        super(uuid);
    }

    @Override
    protected int getAdditionalCooldown(@NotNull PlayerEvent event, ItemStack wandItem) {
        int additionalCooldown = 0;

        SpellInstance spell = getSpellInstance();
        if (spell != null) {
            additionalCooldown += (int) (spell.getAttribute(CastableSpell.COOLDOWN) * Ticks.SINGLE_TICK_DURATION_MS);
            additionalCooldown += (int) (spell.getAttribute(CastableSpell.DELAY) * Ticks.SINGLE_TICK_DURATION_MS);
        }

        return additionalCooldown;
    }

    @Override
    protected @NotNull Queue<@NotNull SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem, PlayerEvent event) {
        LinkedList<@NotNull SpellInstance> spellList = new LinkedList<>();

        SpellInstance spellInstance = getSpellInstance();
        if (spellInstance != null) {
            spellList.add(applyModifiers(spellInstance));
        }

        return spellList;
    }

    protected @Nullable SpellInstance getSpellInstance() {
        return SpellInstance.fromItem(item);
    }

    @Override
    public @NotNull BasicWandHolder getMenu(ItemStack item) {
        return new BasicWandHolder(this, item);
    }

    public ItemStack getItem() {
        return item;
    }


    @Override
    public @NotNull WandType<BasicWand> getWandType() {
        return WandType.BASIC;
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>(super.getLore());

        Component spellText;
        SpellInstance spellInstance = getSpellInstance();
        if (spellInstance == null) {
            spellText = Component.text("None");
        } else {
            spellText = spellInstance.getDefinition().displayName();
        }

        lore.add(Component.text("Spell: ").color(NamedTextColor.AQUA)
                .append(spellText.color(NamedTextColor.GOLD)));

        return lore;
    }

    @Override
    public void toItem(ItemStack item) {
        super.toItem(item);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.BASIC_WAND_TYPE, this)
        );
    }

    public void setItem(@Nullable ItemStack newItem) {
        this.item = newItem;
    }

    @Override
    protected Color getWandColour() {
        SpellInstance spellInstance = getSpellInstance();
        if (spellInstance != null) {
            return spellInstance.getDefinition().getPrimarySpellType().wandColor();
        }
        return null;
    }
}
