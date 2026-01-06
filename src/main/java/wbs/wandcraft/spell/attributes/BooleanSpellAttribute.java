package wbs.wandcraft.spell.attributes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.RegisteredPersistentDataType;

public class BooleanSpellAttribute extends SpellAttribute<Boolean> {
    public BooleanSpellAttribute(String key, boolean defaultValue) {
        super(key, RegisteredPersistentDataType.BOOLEAN, defaultValue, Boolean::parseBoolean);

        setSuggestions(true, false);
        // TODO: Make this translatable, if minecraft has something for this in vanilla pack
        setFormatter(value -> value ? "true" : "false");
        polarity(Polarity.NEUTRAL);
    }

    @Override
    public @Nullable Polarity getPolarity(@NotNull Boolean value) {
        if (value) {
            return polarity();
        } else {
            return polarity().invert();
        }
    }
}
