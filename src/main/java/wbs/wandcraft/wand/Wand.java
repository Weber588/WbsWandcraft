package wbs.wandcraft.wand;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.utils.util.string.WbsStringify;
import wbs.wandcraft.ItemDecorator;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.wand.types.WandType;

import java.time.Duration;
import java.util.*;

import static wbs.wandcraft.util.persistent.AbstractPersistentWandType.WAND_TYPE;

public abstract class Wand implements Attributable {
    public static final NamespacedKey WAND_KEY = WbsWandcraft.getKey("wand");
    public static final NamespacedKey LAST_USED = WbsWandcraft.getKey("last_used");

    public static final WbsParticleEffect FAIL_EFFECT = new NormalParticleEffect()
            .setXYZ(0.35)
            .setY(0.75)
            .setSpeed(0.01)
            .setAmount(20);

    public static final SpellAttribute<Integer> COOLDOWN = new IntegerSpellAttribute("wand_cooldown", 10)
            .setTicksToSecondsFormatter()
            .displayName(Component.text("Recharge Time"));

    @Nullable
    public static Wand getIfValid(ItemStack item) {
        PersistentDataContainerView container = item.getPersistentDataContainer();
        NamespacedKey key = container.get(WAND_TYPE, WbsPersistentDataType.NAMESPACED_KEY);

        WandType<?> wandType = WandcraftRegistries.WAND_TYPES.get(key);

        if (wandType == null) {
            return null;
        }

        return wandType.getWand(container);
    }

    public static void updateLastUsed(PersistentDataContainer container) {
        long currentTick = getTimestamp();
        container.set(LAST_USED, PersistentDataType.LONG, currentTick);
    }

    protected static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static long getLastUsed(PersistentDataContainerView container) {
        return WbsPersistentDataType.getOrDefault(container, LAST_USED, PersistentDataType.LONG, 0L);
    }

    private final @NotNull String uuid;
    protected final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    protected final Set<SpellAttributeModifier<?, ?>> attributeModifiers = new HashSet<>();

    protected Wand(@NotNull String uuid) {
        this.uuid = uuid;
        setAttribute(COOLDOWN.defaultInstance());
    }

    public void tryCasting(@NotNull Player player, ItemStack wandItem) {
        if (player.getCooldown(wandItem) > 0) {
            return;
        }

        WbsWandcraft plugin = WbsWandcraft.getInstance();
        if (CastingManager.isCasting(player)) {
            plugin.sendActionBar("Already casting!", player);
            return;
        }

        PersistentDataContainerView wandContainer = wandItem.getPersistentDataContainer();

        int additionalCooldown = getAdditionalCooldown(player, wandItem);

        long lastUsed = getLastUsed(wandContainer);
        long usableTick = lastUsed + getAttribute(COOLDOWN) * 1000 / Ticks.TICKS_PER_SECOND + additionalCooldown;
        long timestamp = getTimestamp();
        if (timestamp <= usableTick) {
            Duration timeLeft = Duration.ofMillis(usableTick - timestamp);
            String timeLeftString = WbsStringify.toString(timeLeft, false);

            plugin.sendActionBar("You can use that again in " + timeLeftString, player);
            return;
        }

        Queue<SpellInstance> spellList = getSpellQueue(player, wandItem);

        if (spellList.isEmpty()) {
            plugin.sendActionBar("&wThis wand is empty...", player);
            FAIL_EFFECT.play(Particle.SMOKE, WbsEntityUtil.getMiddleLocation(player));
            return;
        }

        CastingQueue queue = new CastingQueue(spellList, this);

        queue.startCasting(player);

        wandItem.editMeta(meta -> {
            updateLastUsed(meta.getPersistentDataContainer());
            player.setCooldown(wandItem, additionalCooldown * 20 / 1000 + getAttribute(COOLDOWN));
        });

        Integer maxDamage = wandItem.getData(DataComponentTypes.MAX_DAMAGE);
        if (maxDamage != null && maxDamage > 0) {
            ItemStack preDamageItem = wandItem.clone();
            ItemStack damaged = wandItem.damage(1, player);
            if (damaged.isEmpty()) {
                // Item was destroyed
                handleWandBreak(player, preDamageItem);
            }
        }
    }

    protected void handleWandBreak(@NotNull Player player, ItemStack preDamageItem) {
        // TODO: Drop all spells
    }

    protected int getAdditionalCooldown(@NotNull Player player, ItemStack wandItem) {
        return 0;
    }

    protected abstract @NotNull Queue<SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem);


    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return attributeValues;
    }
    public Set<SpellAttributeModifier<?, ?>> getAttributeModifiers() {
        return new HashSet<>(attributeModifiers);
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>();

        lore.add(Component.text("Attributes:").color(NamedTextColor.AQUA));
        lore.addAll(Attributable.super.getLore());

        if (!attributeModifiers.isEmpty()) {
            lore.add(Component.text("Modifiers:").color(NamedTextColor.AQUA));

            // TODO: Unify this code with code from SpellModifier
            lore.addAll(attributeModifiers.stream()
                    .map(modifier ->
                            (Component) Component.text("  - ").color(NamedTextColor.GOLD)
                                    .append(modifier.toComponent())
                    )
                    .toList());
        }
        return lore;
    }

    public void toItem(ItemStack item) {
        item.editMeta(meta -> {
            PersistentDataContainer container = meta.getPersistentDataContainer();

            container.set(WAND_TYPE, WbsPersistentDataType.NAMESPACED_KEY, getWandType().getKey());
            ItemDecorator.decorate(this, meta);
        });
    }

    public @NotNull String getUUID() {
        return uuid;
    }

    public void startEditing(Player player, ItemStack item) {
        player.openInventory(getMenu(item).getInventory());
        player.clearActiveItem();
    }

    public abstract @NotNull WandHolder<?> getMenu(ItemStack item);

    public boolean canContain(@NotNull ItemStack addedItem) {
        return SpellInstance.fromItem(addedItem) != null;
    }

    public abstract void updateItems(Inventory inventory);

    @Override
    public abstract @NotNull Component getItemName();

    public void setModifier(SpellAttributeModifier<?, ?> updatedModifier) {
        attributeModifiers.removeIf(modifier -> modifier.attribute().equals(updatedModifier.attribute()));

        attributeModifiers.add(updatedModifier);
    }

    public abstract @NotNull WandType<?> getWandType();

    public abstract void toContainer(PersistentDataContainer container);

    public abstract boolean isItemSlot(int slot);
}
