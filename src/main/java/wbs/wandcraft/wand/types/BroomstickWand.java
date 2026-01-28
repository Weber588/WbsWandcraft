package wbs.wandcraft.wand.types;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsMath;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class BroomstickWand extends Wand {
    public static final NamespacedKey BROOMSTICK_ITEM = WbsWandcraft.getKey("broomstick_item");
    public static final Vector DEFAULT_DIRECTION = new Vector(0, 0, 1);
    private static final double ACCELERATION = 0.125;
    private static final double DRAG = ACCELERATION / 4;
    private static final double MAX_SPEED = 1;

    private @Nullable ItemStack item;

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

    public @Nullable ItemStack getItem() {
        return item;
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

    public void setItem(@Nullable ItemStack newItem) {
        this.item = newItem;
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

            AttributeInstance scaleAttribute = toSpawn.getAttribute(Attribute.SCALE);
            if (scaleAttribute != null) {
                scaleAttribute.setBaseValue(0.5);
            }

            PersistentDataContainer container = toSpawn.getPersistentDataContainer();
            container.set(BROOMSTICK_ITEM, WbsPersistentDataType.ITEM_AS_BYTES, item);
            player.getInventory().removeItem(item);
        });


        ItemDisplay itemDisplay = player.getWorld().spawn(point, ItemDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, display -> {
            display.setItemStack(item);
            display.setInterpolationDuration(2);

            new Transformation(
                    new Vector3f(0, -0.25f, 0),
                    new AxisAngle4f(),
                    new Vector3f(1, 1, 1),
                    new AxisAngle4f()
            );
        });

        Interaction interaction = player.getWorld().spawn(point, Interaction.class, CreatureSpawnEvent.SpawnReason.CUSTOM, toSpawn -> {
            toSpawn.setInteractionHeight(1);
            toSpawn.setInteractionWidth(1.5f);
        });

        broomstickEntity.addPassenger(itemDisplay);
        broomstickEntity.addPassenger(interaction);

        startBroomstickTimer(broomstickEntity);
    }

    public static void startBroomstickTimer(LivingEntity initialBroomstick) {
        WbsWandcraft.getInstance().runTimer(runnable -> {
            Entity updated = Bukkit.getEntity(initialBroomstick.getUniqueId());

            if (!(updated instanceof LivingEntity broomstick)) {
                return;
            }

            if (!broomstick.isValid()) {
                runnable.cancel();
                return;
            }

            PersistentDataContainer container = broomstick.getPersistentDataContainer();
            ItemStack broomstickItem = container.get(BroomstickWand.BROOMSTICK_ITEM, WbsPersistentDataType.ITEM_AS_BYTES);
            if (broomstickItem == null) {
                runnable.cancel();
                return;
            }

            if (!(Wand.fromItem(broomstickItem) instanceof BroomstickWand wand)) {
                runnable.cancel();
                return;
            }

            Vector originalVelocity = broomstick.getVelocity();
            // TODO: Make this get from modifiers instead of direct attribute
            if (originalVelocity.length() > MAX_SPEED + wand.getAttribute(SpeedSpell.SPEED, 0d)) {
                return;
            }

            if (originalVelocity.length() > MAX_SPEED / 10) {
                broomstick.setVelocity(WbsMath.scaleVector(originalVelocity, originalVelocity.length() - DRAG));
            }

            boolean riddenByPlayer = false;
            for (Entity passenger : broomstick.getPassengers()) {
                List<Entity> indirectPassengers = passenger.getPassengers();
                if (passenger instanceof ItemDisplay) {
                    for (Entity indirectPassenger : indirectPassengers) {
                        if (indirectPassenger instanceof Player player) {
                            handlePlayerControl(broomstick, player);
                            riddenByPlayer = true;
                            break;
                        }
                    }
                }
            }

            AttributeInstance gravityAttribute = broomstick.getAttribute(Attribute.GRAVITY);
            if (gravityAttribute != null) {
                double gravityDampener = riddenByPlayer ? 2 : 4;
                gravityAttribute.setBaseValue(gravityAttribute.getDefaultValue() / gravityDampener);
            }

            broomstick.setFallDistance(0);

            Vector updatedVelocity = broomstick.getVelocity();
            if (updatedVelocity.length() > ACCELERATION) {
                Quaternionf rightRotation = getRotation(updatedVelocity);

                for (Entity otherPassenger : broomstick.getPassengers()) {
                    if (otherPassenger instanceof ItemDisplay display) {
                        Transformation existingTransformation = display.getTransformation();
                        Transformation transformation = new Transformation(
                                existingTransformation.getTranslation(),
                                existingTransformation.getLeftRotation(),
                                existingTransformation.getScale(),
                                rightRotation
                        );
                        display.setTransformation(transformation);
                    }
                }
            }
        }, 1, 1);
    }

    private static Quaternionf getRotation(Vector to) {
        Vector direction = to.normalize();

        double yaw = Math.atan2(direction.getX(), direction.getZ());
        double pitch = Math.clamp(Math.asin(direction.getY()), -Math.PI / 5, Math.PI / 5);
        double roll = 0;

        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);

        Quaternionf quaternion = new Quaternionf();

        quaternion.w = (float) (cr * cp * cy + sr * sp * sy);
        quaternion.x = (float) (sr * cp * cy - cr * sp * sy);
        quaternion.y = (float) (cr * cp * sy - sr * sp * cy);
        quaternion.z = (float) (cr * sp * cy + sr * cp * sy);

        return quaternion;
    }

    private static void handlePlayerControl(Entity broomstick, Player player) {
        Input input = player.getCurrentInput();

        Vector clampedFacing = WbsMath.getFacingVector(player);
        clampedFacing = clampedFacing.setY(Math.clamp(clampedFacing.getY(), -0.5, 0.5));
        Vector direction = clampedFacing.clone();
        Vector flatFacing = direction.clone().setY(0);

        boolean isMoving = false;
        boolean isBackwards = false;

        if (input.isLeft() && !input.isRight()) {
            isMoving = true;
            direction.rotateAroundY(Math.PI / 2).multiply(0.75);
        } else if (input.isRight() && !input.isLeft()) {
            isMoving = true;
            direction.rotateAroundY(-Math.PI / 2).multiply(0.75);
        }

        if (input.isForward() && !input.isBackward()) {
            isMoving = true;
            direction.add(flatFacing);
        } else if (input.isBackward() && !input.isForward()) {
            isMoving = true;
            isBackwards = true;
            direction.add(flatFacing.multiply(-0.5));
        }

        if (input.isJump()) {
            if (!isMoving) {
                Vector addVelocity = new Vector(0, ACCELERATION, 0);
                broomstick.setVelocity(broomstick.getVelocity().add(addVelocity));
            } else {
                direction.add(new Vector(0, 1, 0));
            }
        }

        if (isMoving) {
            Vector addVelocity = WbsMath.scaleVector(direction, isBackwards ? ACCELERATION / 2 : ACCELERATION);
            broomstick.setVelocity(broomstick.getVelocity().add(addVelocity));
        }
    }

    public static @Nullable BroomstickWand getBroomstickWand(Entity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();

        ItemStack broomstickItem = container.get(BroomstickWand.BROOMSTICK_ITEM, WbsPersistentDataType.ITEM_AS_BYTES);
        if (broomstickItem == null) {
            return null;
        }
        if (!(Wand.fromItem(broomstickItem) instanceof BroomstickWand wand)) {
            return null;
        }
        wand.setItem(broomstickItem);
        assert wand.getItem() != null;
        return wand;
    }

    public static void breakBroomstick(Entity entity, BroomstickWand wand) {
        entity.getWorld().dropItemNaturally(entity.getLocation(), Objects.requireNonNull(wand.getItem()));
        entity.getPassengers().forEach(passenger -> {
            if (passenger instanceof Display || passenger instanceof Interaction) {
                passenger.remove();
            }
        });
        entity.remove();
    }

}
