package wbs.wandcraft.entities;

import org.bukkit.Input;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import wbs.utils.util.WbsMath;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.types.BroomstickWand;

import java.util.List;
import java.util.Objects;

public class Broomstick extends CustomEntity {
    private static final double ACCELERATION = 0.1;
    private static final double DRAG = ACCELERATION / 4;
    private static final double MAX_SPEED = 6;

    private static final Vector DEFAULT_DIRECTION = new Vector(0, 0, 1);

    public Broomstick(LivingEntity wrapped) {
        super(wrapped);
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
        wand.setWandItem(broomstickItem);
        assert wand.getWandItem() != null;
        return wand;
    }

    @Override
    protected void tick(LivingEntity broomstick, BukkitRunnable runnable) {
        BroomstickWand wand = getBroomstickWand(broomstick);

        if (wand == null) {
            runnable.cancel();
            return;
        }

        Vector originalVelocity = broomstick.getVelocity();
        // TODO: Make this get from modifiers instead of direct attribute
        if (originalVelocity.length() > MAX_SPEED + wand.getAttribute(SpeedSpell.SPEED, 0d)) {
            return;
        }

        if (originalVelocity.length() > MAX_SPEED / 5) {
            broomstick.setVelocity(WbsMath.scaleVector(originalVelocity, originalVelocity.length() - DRAG));
        }

        Player controllingPlayer = null;
        for (Entity passenger : broomstick.getPassengers()) {
            List<Entity> indirectPassengers = passenger.getPassengers();
            if (passenger instanceof Interaction) {
                for (Entity indirectPassenger : indirectPassengers) {
                    if (indirectPassenger instanceof Player player) {
                        handlePlayerControl(broomstick, player);
                        controllingPlayer = player;
                        break;
                    }
                }
                if (controllingPlayer != null) {
                    break;
                }
            }
        }

        AttributeInstance gravityAttribute = broomstick.getAttribute(Attribute.GRAVITY);
        if (gravityAttribute != null) {
            double gravityDampener = controllingPlayer != null ? 3 : 6;
            gravityAttribute.setBaseValue(gravityAttribute.getDefaultValue() / gravityDampener);
        }

        broomstick.setFallDistance(0);

        Vector updatedVelocity = broomstick.getVelocity();
        if (updatedVelocity.length() > ACCELERATION) {
            Vector facingVector = null;
            if (controllingPlayer != null) {
                facingVector = WbsMath.getFacingVector(controllingPlayer);
            }

            Quaternionf rightRotation;
            Vector targetDirection = updatedVelocity;
            if (facingVector != null) {
                targetDirection = targetDirection.add(facingVector.clone().multiply(0.5));
            }

            rightRotation = getRotation(targetDirection);

            if (updatedVelocity.clone().setY(0).length() <= 0.1) {
                return;
            }

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
    }

    private static Quaternionf getRotation(Vector direction) {
        direction = direction.clone().normalize();

        double maxAngle = Math.PI / 5;
        double originalY = direction.getY();
        double maxY = Math.sin(maxAngle);
        direction = direction.setY(0).normalize().setY(Math.clamp(originalY, -maxY, maxY)).normalize();

        float angle = -direction.angle(DEFAULT_DIRECTION);
        Vector axisOfRotation = direction.getCrossProduct(DEFAULT_DIRECTION).normalize();

        AxisAngle4f axisAngle = new AxisAngle4f(angle, axisOfRotation.toVector3f());

        return new Quaternionf(axisAngle);
    }

    private static void handlePlayerControl(Entity broomstick, Player player) {
        Input input = player.getCurrentInput();

        Vector clampedFacing = WbsMath.getFacingVector(player);
        clampedFacing = clampedFacing.setY(Math.clamp(clampedFacing.getY(), -0.5, 0.5));
        Vector direction = clampedFacing.clone().normalize();
        Vector flatFacing = direction.clone().setY(0).normalize();

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
            direction.add(flatFacing.clone().multiply(-2));
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

    public static void breakBroomstick(Entity entity, BroomstickWand wand) {
        entity.getWorld().dropItemNaturally(entity.getLocation(), Objects.requireNonNull(wand.getWandItem()));
        removeRiders(entity);
        entity.remove();
    }

    private static void removeRiders(Entity entity) {
        entity.getPassengers().forEach(passenger -> {
            if (passenger instanceof Display || passenger instanceof Interaction) {
                removeRiders(passenger);
                passenger.remove();
            }
        });
    }
}
