package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

@SuppressWarnings("UnstableApiUsage")
public class MageWand extends Wand {
    private final List<@Nullable ItemStack> items = new LinkedList<>();
    private int currentSlot = 0;

    public MageWand(@NotNull String uuid) {
        super(uuid);
    }

    @Override
    protected @NotNull Queue<@NotNull SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem, PlayerEvent event) {
        LinkedList<SpellInstance> spellList = new LinkedList<>();


        SpellInstance currentSpellInstance = getCurrentSpellInstance();
        if (currentSpellInstance != null) {
            spellList.add(applyModifiers(currentSpellInstance));
        }

        return spellList;
    }

    protected @Nullable SpellInstance getCurrentSpellInstance() {
        if (items.size() <= currentSlot || currentSlot < 0) {
            return null;
        }

        return SpellInstance.fromItem(items.get(currentSlot));
    }

    protected @NotNull List<@NotNull SpellInstance> getSpellInstances() {
        return items.stream()
                .map(SpellInstance::fromItem)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public @NotNull MageWandHolder getMenu(ItemStack item) {
        return new MageWandHolder(this, item);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public @NotNull WandType<MageWand> getWandType() {
        return WandType.MAGE;
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>(super.getLore());

        // TODO: Display Current spell, next spell, and previous spell.

        List<SpellInstance> spellInstances = getSpellInstances();

        SpellInstance currentSpell = getCurrentSpellInstance();

        if (currentSpell == null) {
            lore.add(Component.text("Spells:").color(NamedTextColor.AQUA).append(Component.text(" None").color(NamedTextColor.GOLD)));
            return lore;
        }

        lore.add(Component.text("Current Spell: ")
                .color(NamedTextColor.AQUA)
                .append(currentSpell.getDefinition()
                        .displayName()
                        .color(NamedTextColor.GOLD)
                )
        );

        if (spellInstances.size() > 1) {
            SpellInstance prevInstance = getPrevInstance(spellInstances);
            SpellInstance nextInstance = getNextInstance(spellInstances);

            if (prevInstance != null || nextInstance != null) {
                lore.add(Component.empty());
                if (prevInstance != null) {
                    lore.add(Component.text("Previous Spell: ")
                            .color(NamedTextColor.AQUA)
                            .append(prevInstance.getDefinition()
                                    .displayName()
                                    .color(NamedTextColor.GOLD)
                            )
                    );
                }
                if (nextInstance != null) {
                    lore.add(Component.text("Next Spell: ")
                            .color(NamedTextColor.AQUA)
                            .append(nextInstance.getDefinition()
                                    .displayName()
                                    .color(NamedTextColor.GOLD)
                            )
                    );
                }
            }
        }

        return lore;
    }

    private @Nullable SpellInstance getNextInstance(List<SpellInstance> spellInstances) {
        int nextSlot = (currentSlot + 1) % spellInstances.size();
        return SpellInstance.fromItem(items.get(nextSlot));
    }

    private @Nullable SpellInstance getPrevInstance(List<SpellInstance> spellInstances) {
        int prevSlot = Math.abs((currentSlot - 1) % spellInstances.size());
        return SpellInstance.fromItem(items.get(prevSlot));
    }

    @Override
    public void toItem(ItemStack item) {
        super.toItem(item);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.MAGE_WAND_TYPE, this)
        );
    }


    @Override
    protected @Nullable Color getWandColour() {
        SpellInstance spellInstance = getCurrentSpellInstance();
        if (spellInstance != null) {
            return spellInstance.getDefinition().getPrimarySpellType().wandColor();
        }
        return null;
    }

    public void setItems(List<ItemStack> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        clampCurrentSlot();
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public void setCurrentSlot(int currentSlot) {
        this.currentSlot = currentSlot;
        clampCurrentSlot();
    }

    private void clampCurrentSlot() {
        if (getSpellInstances().isEmpty()) {
            return;
        }
        currentSlot = Math.abs(currentSlot % getSpellInstances().size());
    }

    @Override
    public void handleDrop(PlayerDropItemEvent event, Player player, ItemStack item) {
        event.setCancelled(true);

        if (player.isSneaking()) {
            setCurrentSlot(currentSlot - 1);
        } else {
            setCurrentSlot(currentSlot + 1);
        }

        SpellInstance spell = getCurrentSpellInstance();
        if (spell != null) {
            List<SpellInstance> spellInstances = getSpellInstances();

            Component currentSpellDisplay = spell.getDefinition().displayName().color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED);

            Component spellDisplay = Component.empty();

            SpellInstance prevInstance = getPrevInstance(spellInstances);
            if (prevInstance != null) {
                spellDisplay = spellDisplay.append(prevInstance.getDefinition().displayName().color(NamedTextColor.GRAY));
                spellDisplay = spellDisplay.append(Component.text(" ← ").color(NamedTextColor.GOLD));
            }

            spellDisplay = spellDisplay.append(currentSpellDisplay);

            SpellInstance nextInstance = getNextInstance(spellInstances);
            if (nextInstance != null) {
                spellDisplay = spellDisplay.append(Component.text(" → ").color(NamedTextColor.GOLD));
                spellDisplay = spellDisplay.append(nextInstance.getDefinition().displayName().color(NamedTextColor.GRAY));
            }

            player.sendActionBar(spellDisplay);
        }

        toItem(item);
    }
}
