package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsColours;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@NullMarked
public class ApprenticeWand extends Wand {
    private @Nullable ItemStack left;
    private @Nullable ItemStack right;

    public ApprenticeWand(String uuid) {
        super(uuid);
    }

    @Override
    protected Queue<SpellInstance> getSpellQueue(Player player, ItemStack wandItem, Event event) {
        LinkedList<SpellInstance> spellList = new LinkedList<>();
        SpellInstance spellInstance;

        switch (event) {
            case PlayerInteractEvent interactEvent -> {
                Action action = interactEvent.getAction();
                if (action.isLeftClick()) {
                    spellInstance = SpellInstance.fromItem(left);
                    interactEvent.setCancelled(true);
                } else if (action.isRightClick()) {
                    spellInstance = SpellInstance.fromItem(right);
                    interactEvent.setCancelled(true);
                } else {
                    return spellList;
                }
            }
            case PlayerItemConsumeEvent consumeEvent -> {
                spellInstance = SpellInstance.fromItem(right);
            }
            default -> throw new IllegalStateException("Unexpected value: " + event);
        }

        if (spellInstance != null) {
            spellList.add(applyModifiers(spellInstance));
        }

        return spellList;
    }

    protected @Nullable SpellInstance getLeftSpell() {
        return SpellInstance.fromItem(left);
    }
    protected @Nullable SpellInstance getRightSpell() {
        return SpellInstance.fromItem(right);
    }

    @Override
    public ApprenticeWandHolder getMenu(ItemStack item) {
        return new ApprenticeWandHolder(this, item);
    }

    @Override
    public WandType<ApprenticeWand> getWandType() {
        return WandType.APPRENTICE;
    }

    // Allow left click to be instant, even though it's consumable.
    @Override
    public void handleLeftClick(Player player, ItemStack item, PlayerInteractEvent event) {
        tryCasting(player, item, event);
        event.setCancelled(true);
    }

    @Override
    public boolean hasSpells() {
        return left != null || right != null;
    }

    @Override
    public List<Component> getLore() {
        List<Component> lore = new LinkedList<>(super.getLore());

        Component leftSpellText = getSpellText(getLeftSpell());
        Component rightSpellText = getSpellText(getRightSpell());

        lore.add(Component.text("Left: ").color(NamedTextColor.AQUA)
                .append(leftSpellText.color(NamedTextColor.GOLD))
        );

        lore.add(Component.text("Right: ").color(NamedTextColor.AQUA)
                .append(rightSpellText.color(NamedTextColor.GOLD))
        );

        return lore;
    }

    private Component getSpellText(@Nullable SpellInstance spell) {
        if (spell == null) {
            return Component.text("None");
        } else {
            return spell.getDefinition().displayName();
        }
    }

    @Override
    public void toItem(ItemStack item) {
        super.toItem(item);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.APPRENTICE_WAND_TYPE, this)
        );
    }

    public @Nullable ItemStack getLeft() {
        return left;
    }
    public void setLeft(@Nullable ItemStack newItem) {
        this.left = newItem;
    }

    public @Nullable ItemStack getRight() {
        return right;
    }
    public void setRight(@Nullable ItemStack newItem) {
        this.right = newItem;
    }

    @Override
    protected @Nullable Color getWandColour() {
        SpellInstance leftSpell = getLeftSpell();
        SpellInstance rightSpell = getRightSpell();

        Color leftColor = leftSpell != null ? leftSpell.getDefinition().getPrimarySpellType().wandColor() : null;
        Color rightColor = rightSpell != null ? rightSpell.getDefinition().getPrimarySpellType().wandColor() : null;

        if (leftColor != null && rightColor != null) {
            return WbsColours.mix(leftColor, rightColor);
        } else if (leftColor != null) {
            return leftColor;
        } else {
            return rightColor;
        }
    }

    @Override
    protected void handleNoSpellAvailable(Player player, ItemStack wandItem, Event event) {
        if (left == null && right == null) {
            WbsWandcraft.getInstance().sendActionBar("&wThis wand is empty...", player);
        } else if (right != null) {
            WbsWandcraft.getInstance().sendActionBar("&wNo spell bound to left click!", player);
        } else {
            WbsWandcraft.getInstance().sendActionBar("&wNo spell bound to right click!", player);
        }

        FAIL_EFFECT.play(Particle.SMOKE, WbsEntityUtil.getMiddleLocation(player));
    }
}
