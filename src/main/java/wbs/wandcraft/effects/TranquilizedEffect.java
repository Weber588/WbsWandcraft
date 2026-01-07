package wbs.wandcraft.effects;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;

import java.util.Map;

@NullMarked
public class TranquilizedEffect extends StatusEffect {
    private final AttributeModifier speedModifier = new AttributeModifier(getKey(), -0.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    private final AttributeModifier jumpModifier = new AttributeModifier(getKey(), -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);

    @Override
    public Map<Attribute, AttributeModifier> getAttributes() {
        return Map.of(
                Attribute.MOVEMENT_SPEED, speedModifier,
                Attribute.JUMP_STRENGTH, jumpModifier
        );
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("tranquilized");
    }
}
