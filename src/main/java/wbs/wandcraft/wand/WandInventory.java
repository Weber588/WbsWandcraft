package wbs.wandcraft.wand;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.WandEntry;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.util.CustomPersistentDataTypes;

import java.util.LinkedList;
import java.util.List;

public class WandInventory implements InventoryHolder {

    private final Inventory inventory;
    private final WandInventoryType type;

    public WandInventory(WandInventoryType type) {
        this.inventory = type.newInventory(this);
        this.type = type;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public List<WandEntry> getRawEntries() {
        List<WandEntry> entries = new LinkedList<>();

        for (ItemStack itemStack : inventory) {
            if (itemStack != null) {
                PersistentDataContainerView container = itemStack.getPersistentDataContainer();

                SpellInstance instance = container.get(SpellInstance.SPELL_INSTANCE_KEY, CustomPersistentDataTypes.SPELL_INSTANCE);
                if (instance != null) {
                    entries.add(instance);
                }

                container.get(SpellModifier.SPELL_MODIFIER_KEY, CustomPersistentDataTypes.SPELL_MODIFIER);
            }
        }

        return entries;
    }
}
