package wbs.wandcraft.resourcepack;

import org.bukkit.Keyed;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.*;

public interface ItemModelProvider extends Keyed {
    default String namespace() {
        return key().namespace();
    }
    default String value() {
        return key().value();
    }

    Model buildBaseModel();
}
