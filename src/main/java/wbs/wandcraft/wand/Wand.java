package wbs.wandcraft.wand;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.UseCooldown;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.utils.util.string.WbsStringify;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.cost.CostUtils;
import wbs.wandcraft.events.EnqueueSpellsEvent;
import wbs.wandcraft.events.SpendManaEvent;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.util.ItemDecorator;
import wbs.wandcraft.wand.types.WandType;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static wbs.wandcraft.util.persistent.AbstractPersistentWandType.WAND_TYPE;

public abstract class Wand implements Attributable {
    public static final NamespacedKey WAND_KEY = WbsWandcraft.getKey("wand");
    public static final NamespacedKey LAST_USED = WbsWandcraft.getKey("last_used");

    public static final WbsParticleEffect FAIL_EFFECT = new NormalParticleEffect()
            .setXYZ(0.35)
            .setY(0.75)
            .setSpeed(0.01)
            .setAmount(20);

    public static boolean isWand(ItemStack item) {
        return fromItem(item) != null;
    }

    @Nullable
    public static Wand fromItem(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        PersistentDataContainerView container = item.getPersistentDataContainer();
        NamespacedKey key = container.get(WAND_TYPE, WbsPersistentDataType.NAMESPACED_KEY);

        if (key == null) {
            return null;
        }

        WandType<?> wandType = WandcraftRegistries.WAND_TYPES.get(key);

        if (wandType == null) {
            return null;
        }

        return wandType.getWand(container);
    }

    public static void updateLastUsed(PersistentDataContainer container) {
        long currentMillis = getTimestamp();
        container.set(LAST_USED, PersistentDataType.LONG, currentMillis);
    }

    protected static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static long getLastUsed(PersistentDataContainerView container) {
        return WbsPersistentDataType.getOrDefault(container, LAST_USED, PersistentDataType.LONG, 0L);
    }

    private final @NotNull String uuid;
    protected final Set<SpellAttributeInstance<?>> attributeInstances = new HashSet<>();
    protected final Set<SpellAttributeModifier<?, ?>> attributeModifiers = new HashSet<>();

    protected final List<@Nullable ItemStack> upgrades = new LinkedList<>();

    protected Wand(@NotNull String uuid) {
        this.uuid = uuid;
        setAttribute(CastableSpell.COOLDOWN.defaultInstance());
    }

    public void tryCasting(@NotNull Player player, ItemStack wandItem, Event event) {
        if (player.getCooldown(wandItem) > 0) {
            return;
        }

        WbsWandcraft plugin = WbsWandcraft.getInstance();
        if (CastingManager.isCasting(player)) {
            plugin.sendActionBar("Already casting!", player);
            return;
        }

        PersistentDataContainerView wandContainer = wandItem.getPersistentDataContainer();

        Queue<SpellInstance> spellList = getSpellQueue(player, wandItem, event);

        if (spellList.isEmpty()) {
            handleNoSpellAvailable(player, wandItem, event);
            return;
        }

        int additionalCooldownTicks = getAdditionalCooldownTicks(spellList);

        EnqueueSpellsEvent enqueueSpellsEvent = new EnqueueSpellsEvent(player, spellList, additionalCooldownTicks);

        if (!enqueueSpellsEvent.callEvent()) {
            return;
        }

        additionalCooldownTicks = enqueueSpellsEvent.getAdditionalCooldown();

        if (!checkCooldown(player, wandContainer, event, additionalCooldownTicks)) {
            return;
        }

        int cost = 0;
        for (SpellInstance instance : spellList) {
            Integer instanceCost = instance.getAttribute(CastableSpell.COST, 0);

            cost += instanceCost;
        }

        SpendManaEvent spendManaEvent = new SpendManaEvent(player, spellList, cost, wandItem);
        spendManaEvent.callEvent();

        cost = spendManaEvent.getCost();

        switch (spendManaEvent.getResult()) {
            case BYPASS -> {
                onSucceedCost(player, cost, wandItem);
            }
            case CONTINUE -> {
                if (cost > 0) {
                    int remainder = CostUtils.takeCost(player, cost);

                    if (remainder > 0) {
                        onFailCost(player, wandItem, event, additionalCooldownTicks);
                        return;
                    }
                }
                onSucceedCost(player, cost, wandItem);
            }
            case CANCEL -> {
                return;
            }
            case FAIL -> {
                onFailCost(player, wandItem, event, additionalCooldownTicks);
                return;
            }
        }

        CastingQueue queue = new CastingQueue(spellList, this);

        queue.startCasting(player);

        setCooldown(player, wandItem, event, additionalCooldownTicks);

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

    protected boolean checkCooldown(@NotNull Player player, PersistentDataContainerView cooldownContainer, Event event, int additionalCooldownTicks) {
        long lastUsedMilli = getLastUsed(cooldownContainer);
        long usableMilli = lastUsedMilli + (getAttribute(CastableSpell.COOLDOWN) + additionalCooldownTicks) * Ticks.SINGLE_TICK_DURATION_MS;
        long timestamp = getTimestamp();
        if (timestamp <= usableMilli) {
            Duration timeLeft = Duration.ofMillis(usableMilli - timestamp);
            String timeLeftString = WbsStringify.toString(timeLeft, false);

            WbsWandcraft.getInstance().sendActionBar("You can use that again in " + timeLeftString, player);
            return false;
        }
        return true;
    }

    protected void setCooldown(@NotNull Player player, ItemStack itemForCooldown, Event event, int additionalCooldownTicks) {
        itemForCooldown.editMeta(meta -> {
            int cooldownTicks = additionalCooldownTicks + this.getAttribute(CastableSpell.COOLDOWN);

            updateLastUsed(meta.getPersistentDataContainer());
            UseCooldown useCooldown = itemForCooldown.getData(DataComponentTypes.USE_COOLDOWN);
            if (useCooldown != null && useCooldown.cooldownGroup() != null) {
                Key cooldownKey = useCooldown.cooldownGroup();

                player.setCooldown(cooldownKey, cooldownTicks);
            } else {
                player.setCooldown(itemForCooldown, cooldownTicks);
            }
        });
    }

    protected void onFailCost(@NotNull Player player, ItemStack wandItem, Event event, int additionalCooldown) {
        WbsWandcraft.getInstance().sendActionBar("&wNot enough mana!", player);
        setCooldown(player, wandItem, event, additionalCooldown);
    }

    protected void onSucceedCost(@NotNull Player player, int cost, ItemStack wandItem) {

    }

    protected void handleNoSpellAvailable(@NotNull Player player, ItemStack wandItem, Event event) {
        WbsWandcraft.getInstance().sendActionBar("&wThis wand is empty...", player);
        FAIL_EFFECT.play(Particle.SMOKE, WbsEntityUtil.getMiddleLocation(player));
    }

    protected void handleWandBreak(@NotNull Player player, ItemStack preDamageItem) {
        // TODO: Drop all spells
    }

    protected int getAdditionalCooldownTicks(Queue<SpellInstance> spellList) {
        int additionalCooldown = 0;

        for (SpellInstance spell : spellList) {
            additionalCooldown += spell.getAttribute(CastableSpell.COOLDOWN);
        }

        return additionalCooldown;
    }

    protected abstract @NotNull Queue<@NotNull SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem, Event event);

    public Set<SpellAttributeInstance<?>> getAttributeInstances() {
        return attributeInstances;
    }

    @Override
    public @Unmodifiable Set<SpellAttributeInstance<?>> deriveAttributeValues() {
        List<SpellAttributeModifier<?, ?>> modifiers = new LinkedList<>();

        List<SpellModifier> upgradeModifiers = getUpgradeModifiers();
        upgradeModifiers.forEach(spellModifier -> {
            modifiers.addAll(spellModifier.getModifiers());
        });

        return Attributable.super.deriveAttributeValues().stream()
                .map(instance -> {
                    SpellAttributeInstance<?> clone = instance.clone();
                    for (SpellAttributeModifier<?, ?> modifier : modifiers) {
                        clone.modify(modifier);
                    }
                    return clone;
                })
                .collect(Collectors.toSet());
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

    public List<ItemStack> getUpgrades() {
        return upgrades;
    }

    public List<SpellModifier> getUpgradeModifiers() {
        return getUpgrades().stream()
                .map(SpellModifier::fromItem)
                .filter(Objects::nonNull)
                .toList();
    }

    public void setUpgrades(List<ItemStack> newUpgrades) {
        this.upgrades.clear();
        this.upgrades.addAll(newUpgrades);
    }

    public void toItem(ItemStack item) {
        item.editMeta(meta -> {
            PersistentDataContainer container = meta.getPersistentDataContainer();

            container.set(WAND_TYPE, WbsPersistentDataType.NAMESPACED_KEY, getWandType().getKey());

            ItemDecorator.decorate(this, meta);
        });

        Color wandColour = getWandColour();
        wandColour = wandColour == null ? Color.WHITE : wandColour;
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
                .addString(getWandType().getWandTexture().getKey().asString())
                .addColor(wandColour)
        );
    }

    protected abstract @Nullable Color getWandColour();

    public @NotNull String getUUID() {
        return uuid;
    }

    public void startEditing(Player player, ItemStack item) {
        player.openInventory(getMenu(item).getInventory());
        player.clearActiveItem();
    }

    public abstract @NotNull WandHolder<?> getMenu(ItemStack item);

    @Override
    public @NotNull Component getItemName() {
        return getWandType().getItemName();
    }

    public void setModifier(SpellAttributeModifier<?, ?> updatedModifier) {
        attributeModifiers.removeIf(modifier -> modifier.attribute().equals(updatedModifier.attribute()));

        attributeModifiers.add(updatedModifier);
    }

    public abstract @NotNull WandType<?> getWandType();

    @Contract("_ -> new")
    public SpellInstance applyModifiers(SpellInstance spellInstance) {
        spellInstance = new SpellInstance(spellInstance);
        for (SpellAttributeModifier<?, ?> attributeModifier : attributeModifiers) {
            attributeModifier.modify(spellInstance);
        }

        for (SpellModifier modifier : getUpgradeModifiers()) {
            modifier.modify(spellInstance);
        }

        return spellInstance;
    }

    public void handleDrop(PlayerDropItemEvent event, Player player, ItemStack item) {

    }

    public void handleLeftClick(Player player, ItemStack item, PlayerInteractEvent event) {
        // Don't try casting if it's a wand with a consumable component -- it needs to complete an animation first.
        Consumable consumable = item.getData(DataComponentTypes.CONSUMABLE);
        if (consumable == null || consumable.consumeSeconds() < (1 / (float) Ticks.TICKS_PER_SECOND)) {
            tryCasting(player, item, event);
            event.setCancelled(true);
        }
    }

    public void handleRightClick(Player player, ItemStack item, PlayerInteractEvent event) {
        // Don't try casting if it's a wand with a consumable component -- it needs to complete an animation first.
        Consumable consumable = item.getData(DataComponentTypes.CONSUMABLE);
        if (consumable == null || consumable.consumeSeconds() < (1 / (float) Ticks.TICKS_PER_SECOND)) {
            tryCasting(player, item, event);
            event.setCancelled(true);
        }
    }

    public void handleConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        tryCasting(player, item, event);
    }

    public void handleRightClickEntity(Player player, ItemStack item, PlayerInteractEntityEvent event) {
        switch (event.getRightClicked()) {
            case AbstractVillager ignored -> {
                return;
            }
            case Vehicle vehicle -> {
                if (vehicle.getPassengers().isEmpty()) {
                    if (vehicle instanceof AbstractHorse horse) {
                        if (horse.getInventory().getSaddle() != null) {
                            return;
                        }
                    } else if (vehicle instanceof Pig pig) {
                        if (pig.hasSaddle()) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            default -> {}
        }

        tryCasting(player, item, event);
    }

    public void handleDamageEntity(Player player, ItemStack item, EntityDamageByEntityEvent event) {
        tryCasting(player, item, event);
    }
}
