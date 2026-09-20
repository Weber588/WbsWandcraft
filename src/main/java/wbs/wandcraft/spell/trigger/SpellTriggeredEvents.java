package wbs.wandcraft.spell.trigger;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.objects.generics.MagicObject;

public class SpellTriggeredEvents {

    /**
     * When a spell "hits" something physically, such as a block or entity.
     * May or may not involve a magic object.
     */
    public static final SpellTriggeredEvent<RayTraceResult> ON_HIT_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("hit"), RayTraceResult.class);
    /**
     * Runs every ticket on the location a magic object ticks.
     */
    public static final SpellTriggeredEvent<MagicObject> OBJECT_TICK_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("tick"), MagicObject.class);
    /**
     * Runs when a magic object expires, but before any other final onRemove actions occur.
     */
    public static final SpellTriggeredEvent<MagicObject> OBJECT_EXPIRE_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("expire"), MagicObject.class);
    /**
     * Runs after a spell explicitly targets its caster.
     */
    public static final SpellTriggeredEvent<Entity> TARGET_SELF_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("target_self"), Entity.class);
    /**
     * Runs after a spell affects an explicitly targeted entity. Explicitly in this case
     * means that the spell would have failed had it not had any targets.<br/>
     * Does not include entities hit in {@link #ON_HIT_TRIGGER}
     */
    public static final SpellTriggeredEvent<Entity> DIRECT_TARGET_ENTITY_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("direct_target_entity"), Entity.class);
    /**
     * Runs after a spell affects an indirectly targeted entity. Indirectly in this case
     * means that the spell did not explicitly depend on having an entity involved, and it
     * targeted the entity as a side or secondary effect that did not form a core part of the spell.<br/>
     * Does not include entities hit in {@link #ON_HIT_TRIGGER} unless an additional targeting takes place.
     */
    public static final SpellTriggeredEvent<Entity> INDIRECT_TARGET_ENTITY_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("indirect_target_entity"), Entity.class);
    public static final SpellTriggeredEvent<Entity> DAMAGE_ENTITY_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("damage_entity"), Entity.class);
    public static final SpellTriggeredEvent<Entity> IGNITE_ENTITY_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("ignite_entity"), Entity.class);
    public static final SpellTriggeredEvent<Block> IGNITE_BLOCK_TRIGGER
            = new SpellTriggeredEvent<>(WbsWandcraft.getKey("ignite_block"), Block.class);
}
