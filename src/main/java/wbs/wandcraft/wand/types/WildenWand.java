package wbs.wandcraft.wand.types;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

@SuppressWarnings("UnstableApiUsage")
public class WildenWand extends Wand {
    private final List<@Nullable ItemStack> items = new LinkedList<>();
    private int lastSpellCooldownTicks = 0;

    public WildenWand(@NotNull String uuid) {
        super(uuid);
    }

    @Override
    protected @NotNull Queue<@NotNull SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem, Event event) {
        LinkedList<SpellInstance> spellList = new LinkedList<>();

        spellList.add(WbsCollectionUtil.getRandom(getSpellInstances()));

        return spellList;
    }

    // Ignore the spell list actually cast, give them a random cooldown
    @Override
    protected int getAdditionalCooldownTicks(Queue<SpellInstance> spellList) {
        SpellInstance random = WbsCollectionUtil.getRandom(getSpellInstances());

        return random.getAttribute(CastableSpell.COOLDOWN);
    }

    @Override
    protected void setCooldown(@NotNull Player player, ItemStack itemForCooldown, Event event, int additionalCooldownTicks) {
        this.lastSpellCooldownTicks = additionalCooldownTicks;
        toItem(itemForCooldown);

        super.setCooldown(player, itemForCooldown, event, additionalCooldownTicks);
    }

    @Override
    protected boolean checkCooldown(@NotNull Player player, PersistentDataContainerView cooldownContainer, Event event, int additionalCooldownTicks) {
        return super.checkCooldown(player, cooldownContainer, event, lastSpellCooldownTicks);
    }

    protected @NotNull List<@NotNull SpellInstance> getSpellInstances() {
        return items.stream()
                .map(SpellInstance::fromItem)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public @NotNull WildenWandHolder getMenu(ItemStack item) {
        return new WildenWandHolder(this, item);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public @NotNull WandType<WildenWand> getWandType() {
        return WandType.WILDEN;
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
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.WILDEN_WAND_TYPE, this)
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

    public int getLastSpellCooldownTicks() {
        return lastSpellCooldownTicks;
    }

    public WildenWand setLastSpellCooldownTicks(int lastSpellCooldownTicks) {
        this.lastSpellCooldownTicks = lastSpellCooldownTicks;
        return this;
    }
}
