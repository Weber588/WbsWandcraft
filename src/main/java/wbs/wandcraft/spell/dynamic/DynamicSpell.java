package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.SpellTypeModifiers;
import wbs.wandcraft.spell.attributes.attributable.BurnDamageAttributable;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.effect.SpellEffectInstance;

// TODO: Give implementing classes default effect triggers based on spell types
// Add all attributes with non-affecting values that may be used
public abstract class DynamicSpell extends SpellDefinition implements BurnDamageAttributable {
    private final String dynamicType;

    private static String getStrippedKey(SpellType primary) {
        return primary.getKey().asString().replace(":", "_");
    }

    public DynamicSpell(String dynamicType, SpellType primary, @Nullable SpellType secondary) {
        super(WbsWandcraft.getKey(
                        "dynamic/" + dynamicType + "/"
                                + getStrippedKey(primary)
                                + (secondary != null ? "_" + getStrippedKey(secondary) : "")
                )
        );
        this.dynamicType = dynamicType;

        addSpellType(primary);
        if (secondary != null) {
            addSpellType(secondary);
        }

        setAttribute(DAMAGE, 0d);
        setAttribute(BURN_TIME, 0d);
    }

    @Override
    public Component displayName() {
        return Component.text(
                WbsStrings.capitalizeAll("Dynamic " + dynamicType.replace("_", " ") + " Spell")
        ).color(
                getPrimarySpellType().textColor()
        );
    }

    @Override
    public String rawDescription() {
        return "A dynamic " + dynamicType + " spell.";
    }

    @Override
    @NotNull
    public SpellInstance newInstance() {
        SpellInstance newInstance = super.newInstance();

        Multimap<SpellType, SpellEffectInstance<?>> typedEvents = typedEvents();

        spellTypes.forEach(type -> {
            SpellTypeModifiers.getSpellTypeModifiers(type)
                    .stream()
                    .sorted()
                    .forEachOrdered(modifier -> modifier.modify(newInstance));
            typedEvents.get(type).forEach(newInstance::registerEffect);
        });

        return newInstance;
    }

    protected abstract Multimap<SpellType, SpellEffectInstance<?>> typedEvents();
}
