package wbs.wandcraft.spell.definitions;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.events.SpellCastEvent;
import wbs.wandcraft.spell.WandEntry;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.event.SpellEffectInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SpellInstance implements WandEntry<SpellInstance>, Attributable {
    public static final NamespacedKey SPELL_INSTANCE_KEY = WbsWandcraft.getKey("spell_instance");

    public static boolean isSpellInstance(ItemStack itemStack) {
        return fromItem(itemStack) != null;
    }

    @Contract("null -> null")
    @Nullable
    public static SpellInstance fromItem(@Nullable ItemStack itemStack){
        if (itemStack == null) {
            return null;
        }
        PersistentDataContainerView container = itemStack.getPersistentDataContainer();

        return container.get(SpellInstance.SPELL_INSTANCE_KEY, CustomPersistentDataTypes.SPELL_INSTANCE);
    }

    private final SpellDefinition definition;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    private final Set<SpellEffectInstance<?>> triggeredEffects = new HashSet<>();

    public SpellInstance(SpellDefinition definition) {
        this.definition = definition;

        attributeValues.addAll(definition.getAttributeInstances());
    }

    public SpellInstance(SpellInstance other) {
        this.definition = other.definition;

        for (SpellAttributeInstance<?> otherAttribute : other.getAttributeInstances()) {
            attributeValues.add(otherAttribute.clone());
        }
    }

    public Set<SpellAttributeInstance<?>> getAttributeInstances() {
        return attributeValues;
    }

    @Override
    public void writeAttributes(PersistentDataContainer container, NamespacedKey key) {
        PersistentDataContainer attributes = container.getAdapterContext().newPersistentDataContainer();
        for (SpellAttributeInstance<?> attribute : getAttributeInstances()) {
            if (!getAttribute(attribute.attribute()).equals(definition.getAttribute(attribute.attribute()))) {
                attribute.writeTo(attributes);
            }
        }

        container.set(key, PersistentDataType.TAG_CONTAINER, attributes);
    }

    @Nullable
    public CastContext cast(Player player, @Nullable Wand wand, EquipmentSlot slot, Runnable callback) {
        if (definition instanceof CastableSpell castable) {
            CastContext context = new CastContext(player, this, wand, slot, player.getEyeLocation(), null, callback);
            SpellCastEvent castEvent = new SpellCastEvent(player, context);
            if (!castEvent.callEvent()) {
                return null;
            }
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

    @Override
    public String toString() {
        return definition.getKey().asString();
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>();

        Component types = definition.getTypesDisplay();

        lore.add(types);
        lore.addAll(definition.loreDescription());

        deriveAttributeValues().stream()
                .sorted()
                .filter(instance -> !instance.value().equals(definition.getDefault(instance.attribute())))
                .map(instance ->
                        (Component) Component.text("  - ").style(Style.style(NamedTextColor.GOLD, Set.of()))
                                .append(instance.toComponent())
                )
                .forEachOrdered(lore::add);

        return lore;
    }
}
