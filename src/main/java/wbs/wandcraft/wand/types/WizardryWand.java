package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.attributes.BooleanSpellAttribute;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class WizardryWand extends Wand {
    public static final SpellAttribute<Integer> DELAY = new IntegerSpellAttribute("cast_delay", CastingQueue.DEFAULT_CAST_DELAY)
            .setShowAttribute(delay -> delay > 0 && delay != CastingQueue.DEFAULT_CAST_DELAY)
            .setTicksToSecondsFormatter()
            .overrideTextureValue("duration")
            .sentiment(SpellAttribute.Sentiment.NEGATIVE);
    public static final SpellAttribute<Boolean> SHUFFLE = new BooleanSpellAttribute("shuffle", false)
            .setShowAttribute(shuffle -> shuffle);
    public static final SpellAttribute<Integer> SLOTS = new IntegerSpellAttribute("slots", 10);

    private final List<@Nullable ItemStack> items = new LinkedList<>();

    public WizardryWand(@NotNull String uuid) {
        super(uuid);
        setAttribute(DELAY.defaultInstance());
        setAttribute(SHUFFLE.defaultInstance());
        setAttribute(SLOTS.defaultInstance());
    }

    @Override
    protected @NotNull Queue<@NotNull SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem, Event event) {
        LinkedList<SpellInstance> spellList = new LinkedList<>();

        getSpellInstances().stream()
                .map(this::applyModifiers)
                .forEach(spellList::add);

        Boolean shuffle = getAttribute(SHUFFLE);
        if (shuffle != null && shuffle) {
            Collections.shuffle(spellList);
        }

        return spellList;
    }

    protected @NotNull List<@NotNull SpellInstance> getSpellInstances() {
        return items.stream()
                .map(SpellInstance::fromItem)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public @NotNull WizardryWandHolder getMenu(ItemStack item) {
        return new WizardryWandHolder(this, item);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public @NotNull WandType<WizardryWand> getWandType() {
        return WandType.WIZARDRY;
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>(super.getLore());

        List<SpellInstance> spellInstances = getSpellInstances();
        if (spellInstances.isEmpty()) {
            lore.add(Component.text("Spells:").color(NamedTextColor.AQUA).append(Component.text(" None").color(NamedTextColor.GOLD)));
        } else {
            lore.add(Component.text("Spells:").color(NamedTextColor.AQUA));

            for (SpellInstance instance : spellInstances) {
                lore.add(Component.text("  - ").color(NamedTextColor.GOLD)
                        .append(
                                instance.getDefinition().displayName()
                        ));
            }
        }

        return lore;
    }

    @Override
    public void toItem(ItemStack item) {
        super.toItem(item);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.WIZARDRY_WAND_TYPE, this)
        );
    }

    @Override
    protected @Nullable Color getWandColour() {
        List<Color> colors = new LinkedList<>(getSpellInstances().stream()
                .map(SpellInstance::getDefinition)
                .map(SpellDefinition::getPrimarySpellType)
                .map(SpellType::wandColor)
                .toList());

        if (colors.isEmpty()) {
            return null;
        }

        return colors.removeFirst().mixColors(colors.toArray(Color[]::new));
    }

    public void setItems(List<ItemStack> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
    }
}
