package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.events.EnqueueSpellsEvent;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.IProjectileSpell;

import java.util.List;

public class SpellslingerHat extends MagicHat {
    private static final SpellAttributeModifier<Double, Double> SPEED_MODIFIER = CustomProjectileSpell.SPEED.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            2d
    );
    private static final SpellAttributeModifier<Double, Double> RANGE_MODIFIER = CustomProjectileSpell.RANGE.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            1.5d
    );
    private static final double PROJECTILE_COOLDOWN_REDUCTION = 0.5;

    public SpellslingerHat() {
        super("spellslinger", HatModel.SPELLSLINGER);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x907CC0);
    }

    @Override
    public List<String> getEffectsLore() {
        return List.of(
                "-50% Projectile Spell Cooldown",
                "+100% Projectile Spell Speed",
                "+50% Projectile Spell Range"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), EnqueueSpellsEvent.class, this::onEnqueueSpells);
    }

    private void onEnqueueSpells(EnqueueSpellsEvent event) {
        Player player = event.getPlayer();
        ifEquipped(player, () -> {
            int cooldownToRemove = 0;
            for (SpellInstance instance : event.getSpellList()) {
                if (instance.getDefinition() instanceof IProjectileSpell) {
                    int cooldown = instance.getAttribute(IProjectileSpell.COOLDOWN, 0);
                    cooldownToRemove += (int) (PROJECTILE_COOLDOWN_REDUCTION * cooldown);
                    instance.applyModifier(SPEED_MODIFIER);
                    instance.applyModifier(RANGE_MODIFIER);
                }
            }
            event.setAdditionalCooldown(Math.max(0, event.getAdditionalCooldown() - cooldownToRemove));
        });

    }
}
