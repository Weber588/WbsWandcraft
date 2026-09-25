package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.util.DistanceScaler;

@NullMarked
public interface ISpellDefinition extends Keyed, AttributeHolder, DistanceScaler, Comparable<Keyed> {

    default String name() {
        return WbsStrings.capitalizeAll(key().value().replace("_", " "));
    }

    Component displayName();

    default void debug(String message) {
        String channel = "spell_%s";

        String id;
        if (key().namespace().equals(WbsWandcraft.getInstance().namespace())) {
            id = key().value();
        } else {
            id = key().asString();
        }

        WbsWandcraft.getInstance().debug(channel.formatted(id), message);
    }

    @Override
    default int compareTo(Keyed other) {
        return getKey().compareTo(other.getKey());
    }
}
