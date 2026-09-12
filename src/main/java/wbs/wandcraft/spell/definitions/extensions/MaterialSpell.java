package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Material;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.spell.attributes.EnumSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface MaterialSpell extends ISpellDefinition {
    SpellAttribute<Material> MATERIAL = new EnumSpellAttribute<>("material",
            null,
            AttributeDataType.MATERIAL,
            Material.class
    ).addSuggestions(Material.values())
            .setShowAttribute((value, attributable) -> {
                if (attributable instanceof MaterialSpell spell) {
                    return value != spell.getDefaultMaterial();
                }

                return true;
            })
            .setWritable(true);

    default void setupMaterials() {
        setAttribute(MATERIAL, getDefaultMaterial());
    }

    Material getDefaultMaterial();
}
