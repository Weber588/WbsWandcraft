package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class SorceryWand extends Wand {
    public static final SpellAttribute<Integer> TIERS = new IntegerSpellAttribute("tiers", 1);

    private final Map<Integer, Map<WandControl, @Nullable ItemStack>> items = new HashMap<>();
    private int tier;

    public SorceryWand(@NotNull String uuid) {
        this(uuid, 0);
    }
    public SorceryWand(@NotNull String uuid, int tier) {
        super(uuid);

        this.tier = tier;

        setAttribute(TIERS.defaultInstance());
    }

    @Override
    protected void handleNoSpellAvailable(@NotNull Player player, ItemStack wandItem, PlayerEvent event) {
        if (isEmpty()) {
            WbsWandcraft.getInstance().sendActionBar("&wThis wand is empty...", player);
        } else if (getTieredItems().isEmpty()) {
            WbsWandcraft.getInstance().sendActionBar("&wNo spells on tier" + (tier + 1) + "!", player);
        } else {
            WandControl control = getWandControl(event);
            if (getTierCount() > 1) {
                WbsWandcraft.getInstance().sendActionBar("&wNo spell bound to " + WbsEnums.toPrettyString(control) + " on tier " + (tier + 1) + "!", player);
            } else {
                WbsWandcraft.getInstance().sendActionBar("&wNo spell bound to " + WbsEnums.toPrettyString(control) + "!", player);
            }
        }

        FAIL_EFFECT.play(Particle.SMOKE, WbsEntityUtil.getMiddleLocation(player));
    }

    private boolean isEmpty() {
        return getAllItems().entrySet().stream()
                .allMatch(entry ->
                        entry.getValue().isEmpty()
                );
    }

    @Override
    protected @NotNull Queue<@NotNull SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem, PlayerEvent event) {
        LinkedList<SpellInstance> spellList = new LinkedList<>();

        WandControl control = getWandControl(event);

        ItemStack item = getTieredItems().get(control);
        if (item == null) {
            return spellList;
        }

        SpellInstance instance = SpellInstance.fromItem(item);
        if (instance == null) {
            throw new IllegalStateException("Sorcery Wand item did not have a Spell Instance!");
        }

        spellList.add(instance);
        tier = 0;
        toItem(wandItem);

        return spellList;
    }

    private static @NotNull WandControl getWandControl(PlayerEvent event) {
        final Player player = event.getPlayer();

        return switch (event) {
            case PlayerInteractEvent interactEvent -> {
                Action action = interactEvent.getAction();
                if (action.isRightClick()) {
                    yield player.isSneaking() ? WandControl.SHIFT_RIGHT_CLICK : WandControl.RIGHT_CLICK;
                } else if (action.isLeftClick()) {
                    yield player.isSneaking() ? WandControl.SHIFT_PUNCH : WandControl.PUNCH;
                }
                throw new IllegalStateException();
            }
            case PlayerInteractEntityEvent ignored -> WandControl.RIGHT_CLICK;
            case PlayerDropItemEvent ignored -> {
                if (player.isSneaking()) {
                    yield WandControl.SHIFT_DROP;
                }
                throw new IllegalStateException("Player not sneaking in drop event getSpellQueue");
            }
            case PlayerItemConsumeEvent ignored -> WandControl.RIGHT_CLICK;
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };
    }

    @Override
    public @NotNull SorceryWandHolder getMenu(ItemStack item) {
        return new SorceryWandHolder(this, item);
    }

    public Map<Integer, Map<WandControl, ItemStack>> getAllItems() {
        return items;
    }

    // TODO: Make this configurable
    @Override
    public @NotNull Component getItemName() {
        return Component.text("Sorcery Wand");
    }

    @Override
    public @NotNull WandType<SorceryWand> getWandType() {
        return WandType.SORCERY;
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>(super.getLore());

        Map<WandControl, SpellInstance> spellInstances = getSpellInstances();
        if (getAllItems().size() > 1) {
            lore.add(Component.text("Current Tier:").color(NamedTextColor.AQUA).append(Component.text(" " + (tier + 1)).color(NamedTextColor.GOLD)));
        }

        if (spellInstances.isEmpty()) {
            lore.add(Component.text("Spells:").color(NamedTextColor.AQUA).append(Component.text(" None").color(NamedTextColor.GOLD)));
        } else {
            lore.add(Component.text("Spells:").color(NamedTextColor.AQUA));

            spellInstances.forEach((control, spell) -> {
                lore.add(Component.text("  " + WbsEnums.toPrettyString(control) + " - ").color(NamedTextColor.GOLD)
                        .append(
                                spell.getDefinition().displayName().color(NamedTextColor.AQUA)
                        ));
            });
        }

        return lore;
    }

    private Map<WandControl, SpellInstance> getSpellInstances() {
        Map<WandControl, SpellInstance> spellInstances = new HashMap<>();

        getTieredItems().forEach((control, item) -> {
            if (item != null) {
                SpellInstance instance = SpellInstance.fromItem(item);
                if (instance != null) {
                    spellInstances.put(control, instance);
                }
            }
        });

        return spellInstances;
    }

    @Override
    public void toItem(ItemStack item) {
        super.toItem(item);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.SORCERY_WAND_TYPE, this)
        );
    }

    @NotNull
    public Map<WandControl, ItemStack> getTieredItems() {
        return getTieredItems(tier);
    }

    @NotNull
    public Map<WandControl, ItemStack> getTieredItems(int tier) {
        return items.getOrDefault(tier, new HashMap<>());
    }

    public void setTierItems(Map<WandControl, ItemStack> newItems) {
        setTierItems(tier, newItems);
    }

    public void setTierItems(int tier, Map<WandControl, ItemStack> newItems) {
        this.items.put(tier, newItems);
    }

    public void setAllItems(Map<Integer, Map<WandControl, ItemStack>> newItems) {
        this.items.clear();
        this.items.putAll(newItems);
    }

    public void changeTier(Player player, ItemStack item) {
        if (tier >= getMaxTier()) {
            // Only go back to 0 if not already there (happens if only one tier)
            if (tier == 0) {
                WbsWandcraft.getInstance().sendActionBar("&hThis wand does not have tiers.", player);
                return;
            }

            tier = 0;
        } else {
            tier++;
        }

        toItem(item);
        if (tier == 0) {
            WbsWandcraft.getInstance().sendActionBar("&hTier reverted to 1!", player);
        } else {
            WbsWandcraft.getInstance().sendActionBar("&hTier " + (tier + 1) + " spell primed!", player);
        }
    }

    public int getTier() {
        return tier;
    }

    public int getMaxTier() {
        return getTierCount() - 1;
    }

    public int getTierCount() {
        return getAttribute(TIERS);
    }

    @Override
    public void handleDrop(PlayerDropItemEvent event, Player player, ItemStack item) {
        event.setCancelled(true);
        if (player.isSneaking()) {
            // Run next tick, or it'll look in the players inventory and say "oh, they're not holding the wand!"
            WbsWandcraft.getInstance().runLater(() -> tryCasting(player, item, event), 1);
        } else {
            changeTier(player, item);
        }
    }

    public enum WandControl {
        PUNCH,
        RIGHT_CLICK,
        SHIFT_PUNCH,
        SHIFT_RIGHT_CLICK,
        SHIFT_DROP,
    }
}
