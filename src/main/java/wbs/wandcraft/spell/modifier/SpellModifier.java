package wbs.wandcraft.spell.modifier;

import com.google.common.collect.Table;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.WandEntry;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellEffectInstance;
import wbs.wandcraft.util.CustomPersistentDataTypes;
import wbs.wandcraft.wand.WandInventoryType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SpellModifier implements WandEntry<SpellModifier> {
    public static final NamespacedKey SPELL_MODIFIER_KEY = WbsWandcraft.getKey("spell_modifier");

    public static SpellModifier fromItem(ItemStack itemStack) {
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

    private final ModifierScope scope;

    private final List<SpellAttributeModifier<?>> modifiers = new LinkedList<>();
    private final List<SpellEffectInstance<?>> effects = new LinkedList<>();

    public SpellModifier(ModifierScope scope) {
        this.scope = scope;
    }

    public void modify(SpellInstance instance) {
        modifiers.forEach(instance::applyModifier);
        effects.forEach(instance::registerEffect);
    }

    public List<SpellAttributeModifier<?>> getModifiers() {
        return new LinkedList<>(modifiers);
    }

    public List<SpellEffectInstance<?>> getEffects() {
        return new LinkedList<>(effects);
    }

    public SpellModifier addModifier(SpellAttributeModifier<?> modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public SpellModifier addEffect(SpellEffectInstance<?> effect) {
        this.effects.add(effect);
        return this;
    }

    public ModifierScope getScope() {
        return scope;
    }

    public void modify(Table<Integer, Integer, SpellInstance> spellTable, int row, int column, WandInventoryType type) {
        switch (getScope()) {
            case NEXT -> {
                int targetRow = row;
                int targetColumn = column + 1;

                if (targetColumn >= type.getColumns()) {
                    if (type.getRows() <= targetRow) {
                        return;
                    }

                    targetRow++;
                    targetColumn = 0;
                }

                SpellInstance instance = spellTable.get(targetRow, targetColumn);
                if (instance != null) {
                    modify(instance);
                }
            }
            case PREVIOUS -> {
                int targetRow = row;
                int targetColumn = column - 1;

                if (targetColumn < 0) {
                    if (targetRow <= 0) {
                        return;
                    }

                    targetRow--;
                    targetColumn = type.getColumns();
                }

                SpellInstance instance = spellTable.get(targetRow, targetColumn);
                if (instance != null) {
                    modify(instance);
                }
            }
            case LEFT -> {
                int targetColumn = column - 1;

                if (targetColumn < 0) {
                    return;
                }

                SpellInstance instance = spellTable.get(row, targetColumn);
                if (instance != null) {
                    modify(instance);
                }
            }
            case RIGHT -> {
                int targetColumn = column + 1;

                if (targetColumn >= type.getColumns()) {
                    return;
                }

                SpellInstance instance = spellTable.get(row, targetColumn);
                if (instance != null) {
                    modify(instance);
                }
            }
            case ABOVE -> {
                int targetRow = row - 1;

                if (targetRow < 0) {
                    return;
                }

                SpellInstance instance = spellTable.get(targetRow, column);
                if (instance != null) {
                    modify(instance);
                }
            }
            case BELOW -> {
                int targetRow = row + 1;

                if (targetRow >= type.getRows()) {
                    return;
                }

                SpellInstance instance = spellTable.get(targetRow, column);
                if (instance != null) {
                    modify(instance);
                }
            }
            case GLOBAL -> {
                Map<Integer, Map<Integer, SpellInstance>> rowMap = spellTable.rowMap();

                rowMap.forEach((rowIndex, columnMap) -> {
                    for (int columnIndex = 0; columnIndex < rowIndex; columnIndex++) {
                        modify(columnMap.get(columnIndex));
                    }
                });
            }
        }
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> loreList = new LinkedList<>();
        loreList.add(
                Component.text("Scope: ").color(NamedTextColor.AQUA)
                        .append(Component.text(scope.name()).color(NamedTextColor.GOLD))
        );
        if (!modifiers.isEmpty()) {
            loreList.add(Component.text("Attributes:").color(NamedTextColor.AQUA));

            loreList.addAll(modifiers.stream()
                    .map(modifier ->
                            (Component) Component.text("  - ").color(NamedTextColor.GOLD)
                                    .append(modifier.toComponent())
                    )
                    .toList());
        }

        if (!effects.isEmpty()) {
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

    public void removeModifier(SpellAttributeModifier<?> modifier) {
        modifiers.remove(modifier);
    }
}
