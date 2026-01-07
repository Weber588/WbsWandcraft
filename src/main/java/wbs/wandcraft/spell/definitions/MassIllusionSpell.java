package wbs.wandcraft.spell.definitions;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.effects.PolymorphedEffect;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.spell.RequiresPlugin;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.extensions.TargetedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RequiresPlugin("LibsDisguises")
public class MassIllusionSpell extends SpellDefinition implements CastableSpell, DurationalSpell, TargetedSpell<LivingEntity> {
    public MassIllusionSpell() {
        super("mass_illusion");

        addSpellType(SpellType.SCULK);
        addSpellType(SpellType.ENDER);

        setAttribute(COST, 750);
        setAttribute(COOLDOWN, 60 * Ticks.TICKS_PER_SECOND);

        setAttribute(DURATION, 20 * Ticks.TICKS_PER_SECOND);

        setAttribute(TARGET, TargeterType.RADIUS);
        setAttribute(TARGET_RANGE, 30d);
        setAttribute(MAX_TARGETS, 25);
    }

    @Override
    public String rawDescription() {
        return "Disguises all mobs in a radius, and shuffles them randomly so nobody knows who's who!";
    }

    @Override
    public void cast(CastContext context) {
        Player caster = context.player();
        SpellInstance instance = context.instance();

        List<LivingEntity> targets = new LinkedList<>(getTargets(context));

        if (targets.isEmpty()) {
            String noTargetsMessage = getNoTargetsMessage(context);
            WbsWandcraft.getInstance().sendActionBar(noTargetsMessage, caster);
        } else {
            targets.add(caster);
            LinkedList<Location> locations = new LinkedList<>();
            targets.forEach(target -> locations.add(target.getLocation()));
            Collections.shuffle(locations);

            LinkedList<LivingEntity> disguiseShuffle = new LinkedList<>(targets);
            Collections.shuffle(disguiseShuffle);

            for (int i = 0; i < targets.size(); i++) {
                LivingEntity target = targets.get(i);
                Location targetLocation = locations.get(i);
                LivingEntity disguiseTarget = disguiseShuffle.get(i);

                Disguise disguise;
                if (disguiseTarget instanceof Player disguisePlayer) {
                    disguise = new PlayerDisguise(disguisePlayer);
                } else {
                    disguise = new MobDisguise(DisguiseType.getType(disguiseTarget));
                }

                disguise.setEntity(target);
                disguise.setSelfDisguiseVisible(false);
                disguise.startDisguise();

                StatusEffectInstance.applyEffect(
                        target,
                        StatusEffectManager.DISGUISED,
                        instance.getAttribute(DURATION),
                        true,
                        caster
                );

                PolymorphedEffect.POLYMORPH_EFFECT.play(Particle.CLOUD, WbsEntityUtil.getMiddleLocation(target));
                target.teleport(targetLocation);
                PolymorphedEffect.POLYMORPH_EFFECT.play(Particle.CLOUD, WbsEntityUtil.getMiddleLocation(target));
            }
        }
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
