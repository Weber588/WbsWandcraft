package wbs.wandcraft.wand;

import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

import java.util.HashSet;
import java.util.Set;

public class Wand {

    public static final SpellAttribute<Double> COOLDOWN = new SpellAttribute<>("wand_cooldown", PersistentDataType.DOUBLE, 0.5);

    private SpellAttributeInstance<Integer> baseCooldown;
    private SpellAttributeInstance<Integer> baseCastDelay;
    private final WandInventory inventory;

    private final Set<SpellAttributeModifier<?>> modifiers = new HashSet<>();

    public Wand(WandInventory inventory) {
        this.inventory = inventory;
    }


}
