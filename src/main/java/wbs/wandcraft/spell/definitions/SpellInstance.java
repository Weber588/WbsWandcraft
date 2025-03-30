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

        attributeValues.addAll(definition.getAttributeValues());
    }

    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return attributeValues;
    }

    @Nullable
    public CastContext cast(Player player, Runnable callback) {
        if (definition instanceof CastableSpell castable) {
            CastContext context = new CastContext(player, this, player.getEyeLocation(), null, callback);
            castable.cast(context);

            // If a spell has completeAfterCast = false, then it will handle the callback itself at a later time.
            if (castable.completeAfterCast()) {
                context.finish();
            }
            return context;
        }

        return null;
    }

    public void cast(CastContext context) {
        if (definition instanceof CastableSpell castable) {
            castable.cast(context);
        }
    }

    public SpellDefinition getDefinition() {
        return definition;
    }

    public void registerEffect(SpellEffectInstance<?> spellEffect) {
        triggeredEffects.add(spellEffect);
    }

    public <T> Set<SpellEffectInstance<?>> getEffects(SpellTriggeredEvent<T> event) {
        Set<SpellEffectInstance<?>> effects = new HashSet<>();

        for (SpellEffectInstance<?> effect : triggeredEffects) {
            if (effect.getDefinition().getSupportFor(event) != null) {
                effects.add(effect);
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
