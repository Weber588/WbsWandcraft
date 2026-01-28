package wbs.wandcraft.util.persistent;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.wand.types.BroomstickWand;

public class PersistentBroomstickWandType
        extends AbstractPersistentWandType<BroomstickWand> {
    @Override
    public @NotNull Class<BroomstickWand> getComplexType() {
        return BroomstickWand.class;
    }

    @Override
    protected void writeTo(PersistentDataContainer container, BroomstickWand wand, @NotNull PersistentDataAdapterContext context) {
    }

    @Override
    protected @NotNull BroomstickWand getWand(@NotNull PersistentDataContainer container, @NotNull String uuid) {
        return new BroomstickWand(uuid);
    }

    @Override
    protected void populateWand(BroomstickWand wand, @NotNull PersistentDataContainer container) {
    }
}