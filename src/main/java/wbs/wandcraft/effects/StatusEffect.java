package wbs.wandcraft.effects;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Keyed;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsKeyed;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public abstract class StatusEffect implements Keyed {
    public Component display() {
        return Component.text(WbsKeyed.toPrettyString(this)).color(NamedTextColor.YELLOW);
    }
    public BossBar.Color barColour() {
        return BossBar.Color.YELLOW;
    }
    public BossBar.Overlay barStyle() {
        return BossBar.Overlay.PROGRESS;
    }

    public Map<Attribute, AttributeModifier> getAttributes() {
        return Map.of();
    }
    public void applyAttributes(LivingEntity entity) {
        getAttributes().forEach((attribute, modifier) -> {
            AttributeInstance movementAttribute = entity.getAttribute(attribute);
            if (movementAttribute != null) {
                movementAttribute.addTransientModifier(modifier);
            }
        });
    }
    public void removeAttributes(LivingEntity entity) {
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE).stream().forEach(attribute -> {
            AttributeInstance attributeInstance = entity.getAttribute(attribute);
            if (attributeInstance != null) {
                attributeInstance.removeModifier(getKey());
            }
        });
    }

    public Set<PotionEffect> getPotionEffects() {
        return Set.of();
    }
    public void applyPotions(LivingEntity entity) {
        getPotionEffects().forEach(entity::addPotionEffect);
    }
    public void removePotions(LivingEntity entity) {
        getPotionEffects().stream().map(PotionEffect::getType).forEach(entity::removePotionEffect);
    }

    public final void applyTo(LivingEntity entity, StatusEffectInstance instance) {
        applyAttributes(entity);
        applyPotions(entity);
        onApply(entity, instance);
    }
    protected void onApply(LivingEntity entity, StatusEffectInstance instance) {}
    public final boolean tick(LivingEntity entity, StatusEffectInstance instance) {
        applyPotions(entity);
        return onTick(entity, instance);
    }
    public boolean onTick(LivingEntity entity, StatusEffectInstance instance) {
        return false;
    }
    public final void removeFrom(LivingEntity entity, StatusEffectInstance instance) {
        removeAttributes(entity);
        removePotions(entity);
        onRemove(entity, instance);
    }
    protected void onRemove(LivingEntity entity, StatusEffectInstance instance) {}
    public boolean isValid(LivingEntity entity, StatusEffectInstance instance) {
        return true;
    }

    public void registerEvents() {}

    public @Nullable StatusEffectInstance getInstance(@NotNull Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return StatusEffectManager.getInstance(livingEntity, this);
        }
        return null;
    }

    public void ifPresent(@NotNull Entity entity, Consumer<@NotNull StatusEffectInstance> ifPresent) {
        StatusEffectInstance instance = getInstance(entity);
        if (instance != null) {
            ifPresent.accept(instance);
        }
    }
}
