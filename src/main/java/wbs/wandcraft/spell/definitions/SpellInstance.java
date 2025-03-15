package wbs.wandcraft.spell.definitions;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.WandEntry;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.event.SpellEffectInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;
import wbs.wandcraft.util.CustomPersistentDataTypes;

import java.util.HashSet;
import java.util.Set;

public class SpellInstance implements WandEntry<SpellInstance>, Attributable {
    public static final NamespacedKey SPELL_INSTANCE_KEY = WbsWandcraft.getKey("spell_instance");

    public static SpellInstance fromItem(ItemStack itemStack){
        PersistentDataContainerView container = itemStack.getPersistentDataContainer();

        return container.get(SpellInstance.SPELL_INSTANCE_KEY, CustomPersistentDataTypes.SPELL_INSTANCE);
    }

    private final SpellDefinition definition;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    private final Set<SpellEffectInstance<?>> triggeredEffects = new HashSet<>();

    public SpellInstance(SpellDefinition definition) {
        this.definition = definition;

        for (SpellAttribute<?> attribute : definition.getAttributes()) {
            attributeValues.add(attribute.getInstance());
        }
    }

    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return attributeValues;
    }

    public void cast(Player player) {
        if (definition instanceof CastableSpell castable) {
            castable.cast(new CastContext(player, this));
        }
    }

    public SpellDefinition getDefinition() {
        return definition;
    }

    public void registerEffect(SpellEffectInstance<?> spellEffect) {
        triggeredEffects.add(spellEffect);
    }

    public <T> Set<SpellEffectInstance<T>> getEffects(SpellTriggeredEvent<T> event) {
        Set<SpellEffectInstance<T>> effects = new HashSet<>();

        for (SpellEffectInstance<?> effect : triggeredEffects) {
            if (effect.getDefinition().getTrigger().equals(event)) {
                //noinspection unchecked
                effects.add((SpellEffectInstance<T>) effect);
            }
        }

        return effects;
    }

    @Override
    public @Nullable Component getItemName() {
        return definition.displayName();
    }

    @Override
    public NamespacedKey getTypeKey() {
        return SPELL_INSTANCE_KEY;
    }

    @Override
    public PersistentDataType<?, SpellInstance> getThisType() {
        return CustomPersistentDataTypes.SPELL_INSTANCE;
    }
}
