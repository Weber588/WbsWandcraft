package wbs.wandcraft.spell;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class MagicDomainModifiers {
    private static final Multimap<MagicDomain, SpellAttributeModifier<?, ?>> DOMAIN_MODIFIERS = HashMultimap.create();

    public static <T> Multimap<MagicDomain, SpellAttributeModifier<T, ?>> getAttributeModifiers(SpellAttribute<T> attribute) {
        Multimap<MagicDomain, SpellAttributeModifier<T, ?>> modifiers = HashMultimap.create();

        for (MagicDomain domain : DOMAIN_MODIFIERS.keySet()) {
            Collection<SpellAttributeModifier<?, ?>> typeModifiers = DOMAIN_MODIFIERS.get(domain);

            for (SpellAttributeModifier<?, ?> modifier : typeModifiers) {
                if (modifier.attribute().equals(attribute)) {
                    //noinspection unchecked
                    modifiers.put(domain, (SpellAttributeModifier<T, ?>) modifier);
                    break;
                }
            }
        }

        return modifiers;
    }

    public static Set<SpellAttributeModifier<?, ?>> getMagicDomainModifiers(MagicDomain domain) {
        return new HashSet<>(DOMAIN_MODIFIERS.get(domain));
    }

    public static void registerDomainModifier(MagicDomain domain, SpellAttributeModifier<?, ?> modifier) {
        DOMAIN_MODIFIERS.put(domain, modifier);
    }
}
