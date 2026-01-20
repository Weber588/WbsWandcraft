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
import wbs.wandcraft.spell.definitions.extensions.DirectionalSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;

import java.util.List;

public class MarksmanHat extends MagicHat {
    private static final SpellAttributeModifier<Double, Double> IMPRECISION_MODIFIER = DirectionalSpell.IMPRECISION.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            0d
    );
    private static final SpellAttributeModifier<Double, Double> RANGE_MODIFIER = RangedSpell.RANGE.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            1.5
    );

    public MarksmanHat() {
        super("marksman", HatModel.MARKSMAN);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x2E4805);
    }

    @Override
    public List<String> getEffectsLore() {
        return List.of(
                "-100% Spell Imprecision",
                "+50% Spell Range"
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
                instance.applyModifier(IMPRECISION_MODIFIER);
                instance.applyModifier(RANGE_MODIFIER);
            }
        });
    }
}
