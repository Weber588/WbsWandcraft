package wbs.wandcraft.effects;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;

import static wbs.wandcraft.effects.PolymorphedEffect.POLYMORPH_EFFECT;

@NullMarked
public class DisguisedEffect extends StatusEffect {
    @Override
    protected void onRemove(LivingEntity entity, StatusEffectInstance instance) {
        Disguise disguise = DisguiseAPI.getDisguise(entity);
        if (disguise != null) {
            disguise.stopDisguise();
            POLYMORPH_EFFECT.play(Particle.CLOUD, WbsEntityUtil.getMiddleLocation(entity));
        }
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("disguised");
    }
}
