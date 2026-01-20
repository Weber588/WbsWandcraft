package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.events.EnqueueSpellsEvent;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.HealthSpell;

import java.util.List;

public class HealerHat extends MagicHat {
    private static final SpellAttributeModifier<Double, Double> HEALTH_MODIFIER = HealthSpell.HEALTH.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            3d
    );

    public HealerHat() {
        super("healer", HatModel.HEALER);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0xE4E4E4);
    }

    @Override
    public List<String> getEffectsLore() {
        return List.of(
                "+200% Spell Healing"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), EnqueueSpellsEvent.class, this::onEnqueueSpells);
    }

    private void onEnqueueSpells(EnqueueSpellsEvent event) {
        ifEquipped(event.getPlayer(), () -> {
            for (SpellInstance instance : event.getSpellList()) {
                instance.applyModifier(HEALTH_MODIFIER);
            }
        });
    }
}
