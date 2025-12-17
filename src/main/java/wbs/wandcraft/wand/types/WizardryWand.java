package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.spell.attributes.BooleanSpellAttribute;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class WizardryWand extends Wand {
    public static final SpellAttribute<Boolean> SHUFFLE = new BooleanSpellAttribute("shuffle", false)
            .setShowAttribute(shuffle -> shuffle);
    public static final SpellAttribute<Integer> SLOTS = new IntegerSpellAttribute("slots", 10);

    private final List<@Nullable ItemStack> items = new LinkedList<>();

    public WizardryWand(@NotNull String uuid) {
        super(uuid);
        setAttribute(SHUFFLE.defaultInstance());
        setAttribute(SLOTS.defaultInstance());
    }

    @Override
    protected int getAdditionalCooldown(@NotNull Player player, ItemStack wandItem) {
        int additionalCooldown = 0;

        for (SpellInstance spell : getSpellInstances()) {
            additionalCooldown += (int) (spell.getAttribute(CastableSpell.COOLDOWN) * Ticks.SINGLE_TICK_DURATION_MS);
            additionalCooldown += (int) (spell.getAttribute(CastableSpell.DELAY) * Ticks.SINGLE_TICK_DURATION_MS);
        }

        return additionalCooldown;
    }

    @Override
    protected @NotNull Queue<SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem) {
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

    private @NotNull List<@NotNull SpellInstance> getSpellInstances() {
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

    // TODO: Make this configurable
    @Override
    public @NotNull Component getItemName() {
        return Component.text("Wizardry Wand");
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
}
