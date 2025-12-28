package wbs.wandcraft.spell.modifier;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.WandEntry;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellEffectInstance;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SpellModifier implements WandEntry<SpellModifier> {
    public static final NamespacedKey SPELL_MODIFIER_KEY = WbsWandcraft.getKey("spell_modifier");

    public static SpellModifier fromItem(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        PersistentDataContainerView container = itemStack.getPersistentDataContainer();

        return container.get(SpellModifier.SPELL_MODIFIER_KEY, CustomPersistentDataTypes.SPELL_MODIFIER);
    }

    @Override
    public NamespacedKey getTypeKey() {
        return SPELL_MODIFIER_KEY;
    }

    @Override
    public PersistentDataType<?, SpellModifier> getThisType() {
        return CustomPersistentDataTypes.SPELL_MODIFIER;
    }

    private final List<SpellAttributeModifier<?, ?>> modifiers = new LinkedList<>();
    private final List<SpellEffectInstance<?>> effects = new LinkedList<>();

    public void modify(SpellInstance instance) {
        modifiers.forEach(instance::applyModifier);
        effects.forEach(instance::registerEffect);
    }

    public List<SpellAttributeModifier<?, ?>> getModifiers() {
        return new LinkedList<>(modifiers);
    }

    public List<SpellEffectInstance<?>> getEffects() {
        return new LinkedList<>(effects);
    }

    public SpellModifier addModifier(SpellAttributeModifier<?, ?> modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public SpellModifier addEffect(SpellEffectInstance<?> effect) {
        this.effects.add(effect);
        return this;
    }


    public void modify(Collection<SpellInstance> spellQueue) {
        spellQueue.forEach(this::modify);
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> loreList = new LinkedList<>();

        if (modifiers.isEmpty()) {
            loreList.add(Component.text("Attributes:").color(NamedTextColor.AQUA).append(Component.text(" None").color(NamedTextColor.GOLD)));
        } else {
            loreList.add(Component.text("Attributes:").color(NamedTextColor.AQUA));

            loreList.addAll(modifiers.stream()
                    .map(modifier ->
                            (Component) Component.text("  - ").color(NamedTextColor.GOLD)
                                    .append(modifier.toComponent())
                    )
                    .toList());
        }

        if (effects.isEmpty()) {
        //    loreList.add(Component.text("Effects:").color(NamedTextColor.AQUA).append(Component.text(" None").color(NamedTextColor.GOLD)));
        } else {
            loreList.add(Component.text("Effects:").color(NamedTextColor.AQUA));

            loreList.addAll(effects.stream()
                    .map(effect ->
                            (Component) Component.text("  - ").color(NamedTextColor.GOLD)
                                    .append(effect.toComponent())
                    )
                    .toList());
        }

        return loreList;
    }

    public void removeModifier(SpellAttributeModifier<?, ?> modifier) {
        modifiers.remove(modifier);
    }

    @Override
    public @Nullable Component getItemName() {
        return Component.text("Spell Modifier");
    }
}
