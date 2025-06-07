package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;

// TODO: Implement targeting system for these spells that can target one or more entities
public class StunSpell extends SpellDefinition implements CastableSpell, RadiusedSpell {

    public static final int DEFAULT_DURATION = 20;

    public StunSpell() {
        super("stun");
    }

    @Override
    public Component description() {
        return Component.text("Temporarily stuns all living entities in a radius, cancelling eating, drinking, and adding a short cooldown to held items.");
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class)
                .setRange(instance.getAttribute(RADIUS));

        selector.select(player).forEach(target -> StatusEffectInstance.applyEffect(target, StatusEffect.STUNNED, DEFAULT_DURATION, true));
    }
}
