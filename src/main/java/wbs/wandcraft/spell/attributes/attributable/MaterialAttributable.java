package wbs.wandcraft.spell.attributes.attributable;

import org.bukkit.Material;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.EnumSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface MaterialAttributable extends AttributeHolder {
    SpellAttribute<Material> MATERIAL = new EnumSpellAttribute<>("material",
            null,
            AttributeDataType.MATERIAL,
            Material.class
    ).addSuggestions(Material.values())
            .setShowAttribute((value, attributable) -> {
                if (attributable instanceof MaterialAttributable spell) {
                    return value != spell.getDefaultMaterial();
                }

                return true;
            })
            .setWritable(true);

    @AttributableSetupHandler
    default void setupMaterials() {
        setAttribute(MATERIAL, getDefaultMaterial());
    }

    Material getDefaultMaterial();
}
