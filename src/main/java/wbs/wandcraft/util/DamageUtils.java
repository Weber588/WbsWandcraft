package wbs.wandcraft.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import org.bukkit.damage.DamageType;

import java.util.HashSet;
import java.util.Set;

public class DamageUtils {
    private static final Set<DamageType> MAGIC_DAMAGE_TYPES = new HashSet<>();

    static {
        MAGIC_DAMAGE_TYPES.addAll(
                RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.DAMAGE_TYPE)
                        .getTagValues(DamageTypeTagKeys.WITCH_RESISTANT_TO)
        );
    }

    public static boolean isMagicDamage(DamageType type) {
        return MAGIC_DAMAGE_TYPES.contains(type);
    }
}
