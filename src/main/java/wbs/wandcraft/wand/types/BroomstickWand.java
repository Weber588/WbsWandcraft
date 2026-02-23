package wbs.wandcraft.wand.types;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.entities.Broomstick;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class BroomstickWand extends Wand {
    public static final NamespacedKey BROOMSTICK_ITEM = WbsWandcraft.getKey("broomstick_item");

    private transient @Nullable ItemStack wandItem;

    public BroomstickWand(@NotNull String uuid) {
        super(uuid);
    }

    @Override
    protected @NotNull Queue<@NotNull SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem, Event event) {
        LinkedList<@NotNull SpellInstance> spellList = new LinkedList<>();

        SpellInstance spellInstance = getSpellInstance(player);

        if (spellInstance != null) {
            spellList.add(applyModifiers(spellInstance));
        }

        return spellList;
    }

    @Nullable
    private static SpellInstance getSpellInstance(@NotNull Player player) {
        NamespacedKey lastCastKey = player.getPersistentDataContainer().get(SpellInstance.LAST_CAST_KEY, WbsPersistentDataType.NAMESPACED_KEY);
        if (lastCastKey != null) {
            SpellDefinition spellDefinition = WandcraftRegistries.SPELLS.get(lastCastKey);
            if (spellDefinition == null) {
                WbsWandcraft.getInstance().getLogger().warning(SpellInstance.LAST_CAST_KEY.asString() + " present on player " + player.getName() + ", but not registered to registries.");
            } else {
                return new SpellInstance(spellDefinition);
            }
        }
        return null;
    }

    @Override
    public @NotNull BroomstickWandHolder getMenu(ItemStack item) {
        return new BroomstickWandHolder(this, item);
    }

    public @Nullable ItemStack getWandItem() {
        return wandItem;
    }


    @Override
    public @NotNull WandType<BroomstickWand> getWandType() {
        return WandType.BROOMSTICK;
    }

    @Override
    public void toItem(ItemStack item) {
        super.toItem(item);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.BROOMSTICK_WAND_TYPE, this)
        );
    }

    public void setWandItem(@Nullable ItemStack newItem) {
        this.wandItem = newItem;
    }

    @Override
    protected @Nullable Color getWandColour() {
        return null;
    }

    @Override
    public void handleRightClick(Player player, ItemStack item, PlayerInteractEvent event) {
        Location point = event.getInteractionPoint();
        if (event.getClickedBlock() == null || point == null) {
            super.handleRightClick(player, item, event);
            return;
        }

        LivingEntity broomstickEntity = player.getWorld().spawn(point, ArmorStand.class, CreatureSpawnEvent.SpawnReason.CUSTOM, toSpawn -> {
            toSpawn.setInvisible(true);
            toSpawn.setSilent(true);
            toSpawn.setPersistent(true);
            toSpawn.setCanMove(true);
            toSpawn.setCanTick(true);

            PersistentDataContainer container = toSpawn.getPersistentDataContainer();
            container.set(BROOMSTICK_ITEM, WbsPersistentDataType.ITEM_AS_BYTES, item);
            player.getInventory().removeItem(item);
        });

        float totalHeight = (float) broomstickEntity.getHeight();

        ItemDisplay itemDisplay = player.getWorld().spawn(point, ItemDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, display -> {
            display.setItemStack(item);
            display.setInterpolationDuration(20);

            display.setTransformation(new Transformation(
                    new Vector3f(0, -totalHeight / 2, 0),
                    new AxisAngle4f(),
                    new Vector3f(1, 1, 1),
                    new AxisAngle4f()
            ));
        });

        float hitboxHeight = 1;

        // Create a 2nd interaction with negative height so we can ride below the top of the armour stand. It's weird lol
        Interaction rideInteraction = player.getWorld().spawn(point, Interaction.class, CreatureSpawnEvent.SpawnReason.CUSTOM, toSpawn -> {
            toSpawn.setInteractionHeight(-totalHeight / 2);
            toSpawn.setInteractionWidth(0);
        });

        Interaction interactionTop = player.getWorld().spawn(point, Interaction.class, CreatureSpawnEvent.SpawnReason.CUSTOM, toSpawn -> {
            toSpawn.setInteractionHeight(-hitboxHeight / 2);
            toSpawn.setInteractionWidth(1.5f);
        });

        Interaction interactionBottom = player.getWorld().spawn(point, Interaction.class, CreatureSpawnEvent.SpawnReason.CUSTOM, toSpawn -> {
            toSpawn.setInteractionHeight(hitboxHeight / 2);
            toSpawn.setInteractionWidth(1.5f);
        });

        broomstickEntity.addPassenger(itemDisplay);
        broomstickEntity.addPassenger(rideInteraction);

        rideInteraction.addPassenger(interactionTop);
        rideInteraction.addPassenger(interactionBottom);

        new Broomstick(broomstickEntity);
    }
}
