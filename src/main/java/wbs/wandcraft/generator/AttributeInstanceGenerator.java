package wbs.wandcraft.generator;

import wbs.utils.util.WbsCollectionUtil;
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
