package wbs.wandcraft.generator;

import wbs.utils.util.WbsCollectionUtil;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SpellInstanceGenerator{
    private final List<SpellDefinition> definitions = new LinkedList<>(WandcraftRegistries.SPELLS.values());
    private final List<AttributeInstanceGenerator<?>> attributeGenerators = new LinkedList<>();

    public SpellInstance get() {
        SpellInstance instance = new SpellInstance(WbsCollectionUtil.getRandom(definitions));

        for (AttributeInstanceGenerator<?> generator : attributeGenerators) {
            SpellAttributeInstance<?> attributeInstance = generator.get();

            // Only add the attribute if it's valid for the chosen spell
            if (instance.getAttribute(attributeInstance.attribute()) != null) {
                instance.setAttribute(attributeInstance);
            }
        }

        return instance;
    }

    public SpellInstanceGenerator setSpellDefinitions(SpellDefinition ... definitions) {
        return setSpellDefinitions(Arrays.asList(definitions));
    }
    public SpellInstanceGenerator setSpellDefinitions(List<SpellDefinition> definitions) {
        this.definitions.clear();

        if (definitions.isEmpty()) {
            this.definitions.addAll(WandcraftRegistries.SPELLS.values());
        } else {
            this.definitions.addAll(definitions);
        }

        return this;
    }

    public SpellInstanceGenerator addAttributeGenerator(AttributeInstanceGenerator<?> generator) {
        attributeGenerators.add(generator);
        return this;
    }
}
