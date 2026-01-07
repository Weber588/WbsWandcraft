package wbs.wandcraft.effects;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.WbsWandcraft;

import java.util.Map;

@NullMarked
public class PolymorphedEffect extends StatusEffect {
    public static final NormalParticleEffect POLYMORPH_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setSpeed(0.05)
            .setXYZ(0)
            .setAmount(15);

    private final AttributeModifier speedModifier = new AttributeModifier(getKey(), -0.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    private final AttributeModifier damageModifier = new AttributeModifier(getKey(), -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);

    @Override
    public Map<Attribute, AttributeModifier> getAttributes() {
        return Map.of(
                Attribute.MOVEMENT_SPEED, speedModifier,
                Attribute.ATTACK_DAMAGE, damageModifier
        );
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("polymorphed");
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageByEntityEvent.class, this::onEntityDealDamage);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageEvent.class, this::onEntityTakeDamage);
    }

    @Override
    public BossBar.Color barColour() {
        return BossBar.Color.PINK;
    }

    @Override
    public void onApply(LivingEntity entity, StatusEffectInstance instance) {
        // TODO: Make entity type a writeable attribute?
        MobDisguise disguise = new MobDisguise(DisguiseType.SHEEP);

        POLYMORPH_EFFECT.play(Particle.CLOUD, WbsEntityUtil.getMiddleLocation(entity));

        disguise.setEntity(entity);
        disguise.startDisguise();

        if (entity instanceof Player targetPlayer) {
            WbsWandcraft.getInstance().sendActionBar("Polymorphed!", targetPlayer);
        }
    }

    @Override
    public void onRemove(LivingEntity entity, StatusEffectInstance instance) {
        Disguise disguise = DisguiseAPI.getDisguise(entity);
        if (disguise != null) {
            disguise.stopDisguise();
            POLYMORPH_EFFECT.play(Particle.CLOUD, WbsEntityUtil.getMiddleLocation(entity));
        }

        if (entity instanceof Player targetPlayer) {
            WbsWandcraft.getInstance().sendActionBar("Polymorph wore off", targetPlayer);
        }
    }

    private void onEntityTakeDamage(EntityDamageEvent event) {
        if (event.getDamage() < 1) {
            return;
        }

        ifPresent(event.getEntity(), instance -> instance.cancel(true));
    }

    private void onEntityDealDamage(EntityDamageByEntityEvent event) {
        if (event.getDamage() < 1) {
            return;
        }

        ifPresent(event.getDamager(), instance -> instance.cancel(true));
    }
}
