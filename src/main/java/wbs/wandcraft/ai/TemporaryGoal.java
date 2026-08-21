package wbs.wandcraft.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.MobGoals;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class TemporaryGoal<T extends Mob> implements Goal<T> {
    public static <T extends Mob> void replaceGoal(T mob, TemporaryGoal<T> newGoal, int priority, GoalKey<T> toReplace) {
        MobGoals mobGoals = Bukkit.getMobGoals();

        WrappedGoal nmsGoal = getWrappedGoal(mob, toReplace);
        if (nmsGoal != null) {
            newGoal.setReplaced(nmsGoal.asPaperGoal(), nmsGoal.getPriority());
        }

        mobGoals.addGoal(mob, priority, newGoal);
        mobGoals.removeGoal(mob, toReplace);
    }

    @Nullable
    private static <T extends Mob> WrappedGoal getWrappedGoal(T mob, GoalKey<T> key) {
        for (WrappedGoal wrappedGoal : ((CraftMob) mob).getHandle().goalSelector.getAvailableGoals()) {
            if (key.equals(wrappedGoal.getGoal().asPaperGoal().getKey())) {
                return wrappedGoal;
            }
        }

        return null;
    }

    protected final T mob;
    private int age = 0;
    private int maxAge = Integer.MAX_VALUE;
    @Nullable
    private Goal<T> replaced = null;
    private int replacedPriority;

    protected TemporaryGoal(T mob) {
        this.mob = mob;
    }

    @Override
    public final void tick() {
        age++;
        if (age >= maxAge) {
            MobGoals mobGoals = Bukkit.getMobGoals();
            mobGoals.removeGoal(mob, this);
            if (replaced != null) {
                mobGoals.addGoal(mob, replacedPriority, replaced);
            }
            return;
        }

        onTick();
    }

    protected void onTick() {}

    public TemporaryGoal<T> maxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public TemporaryGoal<T> setReplaced(@Nullable Goal<T> replaced, int priority) {
        this.replaced = replaced;
        this.replacedPriority = priority;
        return this;
    }
}
