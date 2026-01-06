package wbs.wandcraft.effects;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.WbsWandcraft;

import java.util.Map;
import java.util.Set;

@NullMarked
public class HoldEffect extends StatusEffect {
    private static final RingParticleEffect EFFECT = (RingParticleEffect) new RingParticleEffect()
            .setRadius(0.5)
            .setData(Material.GRAVEL.createBlockData());

    private final AttributeModifier speedModifier = new AttributeModifier(getKey(), -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    private final AttributeModifier jumpModifier = new AttributeModifier(getKey(), -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    private static final PotionEffect GLOW = new PotionEffect(PotionEffectType.GLOWING, Ticks.TICKS_PER_SECOND, 0, false, false);

    @Override
    public Map<Attribute, AttributeModifier> getAttributes() {
        return Map.of(
                Attribute.MOVEMENT_SPEED, speedModifier,
                Attribute.JUMP_STRENGTH, jumpModifier
        );
    }

    @Override
    public Set<PotionEffect> getPotionEffects() {
        return Set.of(GLOW);
    }

    @Override
    public boolean onTick(LivingEntity entity, StatusEffectInstance instance) {
        EFFECT.setRotation(Bukkit.getCurrentTick())
                .setRadius(entity.getWidth())
                .setAmount((int) (entity.getWidth() * 10))
                .buildAndPlay(Particle.FALLING_DUST, entity.getLocation().add(0, entity.getHeight(), 0));

        return false;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("hold");
    }

    @Override
    public void onApply(LivingEntity entity, StatusEffectInstance instance) {
        if (entity instanceof Player targetPlayer) {
            WbsWandcraft.getInstance().sendActionBar("Held!", targetPlayer);
        }
    }
}
