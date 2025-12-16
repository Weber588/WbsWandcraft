package wbs.wandcraft.generation;

import org.bukkit.configuration.ConfigurationSection;
import wbs.utils.util.WbsCollectionUtil;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AttributeInstanceGenerator<T> {
    private final SpellAttribute<T> attribute;
    private final List<T> values = new LinkedList<>();

    public AttributeInstanceGenerator(SpellAttribute<T> attribute) {
        this.attribute = attribute;
        values.add(attribute.defaultValue());
    }

    public AttributeInstanceGenerator(SpellAttribute<T> attribute, ConfigurationSection generatorSection, WandcraftSettings settings, String directory) {
        this(attribute);

        List<?> values = generatorSection.getList("values");

        if (values != null && !values.isEmpty()) {
            Class<T> tClass = attribute.getTClass();
            List<T> valuesToAdd = new LinkedList<>();

            for (Object value : values) {
                if (tClass.isInstance(value)) {
                    valuesToAdd.add(tClass.cast(value));
                } else {
                    settings.logError("Value \"" + value + "\" is not valid for attribute of type " + tClass.getCanonicalName(), directory + "/values");
                }
            }

            if (!valuesToAdd.isEmpty()) {
                setValues(valuesToAdd);
            }
        }
    }

    public SpellAttributeInstance<T> get() {
        return new SpellAttributeInstance<>(attribute, WbsCollectionUtil.getRandom(values));
    }

    @SafeVarargs
    public final AttributeInstanceGenerator<T> setValues(T... values) {
        return setValues(Arrays.asList(values));
    }
    public AttributeInstanceGenerator<T> setValues(List<T> values) {
        this.values.clear();

        if (values.isEmpty()) {
            this.values.add(attribute.defaultValue());
        } else {
            this.values.addAll(values);
        }

        return this;
    }
}
