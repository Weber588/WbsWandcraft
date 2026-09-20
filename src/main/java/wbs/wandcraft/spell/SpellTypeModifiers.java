package wbs.wandcraft.spell;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SpellTypeModifiers {
    private static final Multimap<SpellType, SpellAttributeModifier<?, ?>> SPELL_TYPE_MODIFIERS = HashMultimap.create();

    public static <T> Multimap<SpellType, SpellAttributeModifier<T, ?>> getAttributeModifiers(SpellAttribute<T> attribute) {
        Multimap<SpellType, SpellAttributeModifier<T, ?>> modifiers = HashMultimap.create();

        for (SpellType type : SPELL_TYPE_MODIFIERS.keySet()) {
            Collection<SpellAttributeModifier<?, ?>> typeModifiers = SPELL_TYPE_MODIFIERS.get(type);

            for (SpellAttributeModifier<?, ?> modifier : typeModifiers) {
                if (modifier.attribute().equals(attribute)) {
                    //noinspection unchecked
                    modifiers.put(type, (SpellAttributeModifier<T, ?>) modifier);
                    break;
                }
            }
        }

        return modifiers;
    }

    public static Set<SpellAttributeModifier<?, ?>> getSpellTypeModifiers(SpellType type) {
        return new HashSet<>(SPELL_TYPE_MODIFIERS.get(type));
    }

    public static void registerTypeModifier(SpellType type, SpellAttributeModifier<?, ?> modifier) {
        SPELL_TYPE_MODIFIERS.put(type, modifier);
    }
}
