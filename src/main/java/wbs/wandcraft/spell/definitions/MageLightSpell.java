package wbs.wandcraft.spell.definitions;

import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkHolderManager;
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.util.Ticks;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Light;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.utils.util.persistent.BlockChunkStorageUtil;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.*;

public class MageLightSpell extends SpellDefinition implements CustomProjectileSpell {
    private static final WbsParticleGroup EFFECT = new WbsParticleGroup();

    static {
        NormalParticleEffect effect = new NormalParticleEffect();
        effect.setAmount(0);
        effect.setY(1);
        effect.setSpeed(1);
        effect.setData(new Particle.DustTransition(Color.WHITE, Color.fromRGB(0xf599ff), 1.3f));

        EFFECT.addEffect(effect, Particle.DUST_COLOR_TRANSITION);
    }

    public MageLightSpell() {
        super("mage_light");

        addSpellType(SpellType.ARCANE);
        addSpellType(SpellType.NETHER);

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, (int) (1.5 * Ticks.TICKS_PER_SECOND));

        setAttribute(SPEED, 2.5d);
        setAttribute(RANGE, 50.0);
        setAttribute(IMPRECISION, 0d);
        setAttribute(GRAVITY, 0d);
    }

    @Override
    public String rawDescription() {
        return "Fires an orb of light that lingers wherever it hits, acting as a magic torch.";
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        projectile.setParticle(EFFECT);

        projectile.setHitEntities(false);


        SpellTriggeredEvents.ON_HIT_TRIGGER.registerAnonymous(context.instance(), (result) -> {
            Block hitBlock = result.getHitBlock();
            BlockFace hitBlockFace = result.getHitBlockFace();
            if (hitBlock == null || hitBlockFace == null) {
                return;
            }

            Block relative = hitBlock.getRelative(hitBlockFace);
            if (relative.isReplaceable() && relative.getType() != Material.LIGHT) {
                boolean isWater = false;
                if (relative.getBlockData() instanceof Waterlogged waterlogged) {
                    isWater = waterlogged.isWaterlogged();
                }

                relative.setType(Material.LIGHT);

                if (relative.getBlockData() instanceof Light light) {
                    light.setLevel(15);
                    light.setWaterlogged(isWater);
                    relative.setBlockData(light);
                }

                BlockChunkStorageUtil.modifyContainer(relative, container -> {
                    container.set(getKey(), WbsPersistentDataType.UUID, context.player().getUniqueId());
                });

                startLightParticles(relative, true);
            }
        });
    }

    private static final Multimap<Chunk, Block> MAGIC_LIGHT_BLOCKS = HashMultimap.create();
    private static final Map<Chunk, @NotNull Integer> MAGIC_LIGHT_TIMERS = new HashMap<>();

    private void startLightParticles(Block block, boolean updateLighting) {
        Chunk chunk = block.getChunk();
        MAGIC_LIGHT_BLOCKS.put(chunk, block);
        if (updateLighting) {
            WbsWandcraft.getInstance().runLater(() -> sendLightingUpdates(chunk), 1);
        }

        Integer timerId = MAGIC_LIGHT_TIMERS.get(chunk);

        if (timerId == null) {
            timerId = WbsWandcraft.getInstance().runTimer(runnable -> playParticles(runnable, chunk), 0, 1);

            MAGIC_LIGHT_TIMERS.put(chunk, timerId);
        }
    }

    private void playParticles(BukkitRunnable runnable, Chunk chunk) {
        Collection<Block> blocks = MAGIC_LIGHT_BLOCKS.get(chunk);

        List<Block> toRemove = new LinkedList<>();
        for (Block block : blocks) {
            if (block.getType() != Material.LIGHT) {
                toRemove.add(block);
                BlockChunkStorageUtil.modifyContainer(block, container -> container.remove(getKey()));
            }
        }

        toRemove.forEach(block -> MAGIC_LIGHT_BLOCKS.remove(chunk, block));

        if (!chunk.isLoaded() || blocks.isEmpty()) {
            runnable.cancel();
            MAGIC_LIGHT_TIMERS.remove(chunk);
            sendLightingUpdates(chunk);
        } else {
            Collection<Player> players = chunk.getPlayersSeeingChunk();
            if (!players.isEmpty()) {
                if (Bukkit.getCurrentTick() % Ticks.TICKS_PER_SECOND == 0) {
                    sendLightingUpdates(chunk);
                }

                for (Block block : blocks) {
                    EFFECT.play(block.getLocation().toCenterLocation());

                    for (Player player : players) {
                        player.sendBlockChange(block.getLocation(), Material.STRUCTURE_VOID.createBlockData());

                    }
                }
            }
        }
    }

    private static void sendLightingUpdates(Chunk chunk) {
        ServerLevel serverLevel = ((CraftWorld) chunk.getWorld()).getHandle();
        Set<ChunkPos> chunksToUpdate = new HashSet<>();

        for (int x = chunk.getX() - 1; x <= chunk.getX() + 1; x++) {
            for (int z = chunk.getZ() - 1; z <= chunk.getZ() + 1; z++) {
                chunksToUpdate.add(new ChunkPos(x, z));
            }
        }

        ChunkHolderManager chunkHolderManager = serverLevel.moonrise$getChunkTaskScheduler().chunkHolderManager;
        ThreadedLevelLightEngine lightEngine = serverLevel
                .getChunkSource()
                .getLightEngine();

        List<ServerGamePacketListenerImpl> connections = chunk.getPlayersSeeingChunk().stream()
                .map(player -> ((CraftPlayer) player).getHandle().connection)
                .toList();

        for (ChunkPos chunkPos : chunksToUpdate) {
            NewChunkHolder chunkHolder = chunkHolderManager.getChunkHolder(chunkPos.x, chunkPos.z);
            if (chunkHolder != null) {
                Packet<?> relightPacket = new ClientboundLightUpdatePacket(chunkPos, lightEngine, null, null);

                connections.forEach(connection -> connection.send(relightPacket));
            }
        }
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        WbsEventUtils.register(WbsWandcraft.getInstance(), ChunkLoadEvent.class, this::onChunkLoad);
        WbsEventUtils.register(WbsWandcraft.getInstance(), ChunkUnloadEvent.class, this::onChunkUnload);
        WbsEventUtils.register(WbsWandcraft.getInstance(), PlayerInteractEvent.class, this::onPlayerBreakLight, EventPriority.HIGHEST, true);
    }

    private void onChunkUnload(ChunkUnloadEvent event) {
        MAGIC_LIGHT_BLOCKS.removeAll(event.getChunk());
        MAGIC_LIGHT_TIMERS.remove(event.getChunk());
    }

    // TODO: Universalize this for magic spawned blocks
    private void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {
        Map<Block, PersistentDataContainer> containers = BlockChunkStorageUtil.getBlockContainerMap(chunkLoadEvent.getChunk());

        for (Block block : containers.keySet()) {
            PersistentDataContainer container = containers.get(block);
            if (container.has(getKey())) {
                startLightParticles(block, false);
            }
        }
    }

    // Allow players in survival to break light blocks
    private void onPlayerBreakLight(PlayerInteractEvent event) {
        if (!event.getAction().isLeftClick()) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (clickedBlock.getType() != Material.LIGHT) {
            return;
        }

        if (!BlockChunkStorageUtil.getContainer(clickedBlock).has(getKey())) {
            return;
        }

        if (event.useInteractedBlock() != Event.Result.DENY) {
            event.setUseInteractedBlock(Event.Result.ALLOW);

            clickedBlock.setType(Material.AIR);
        }
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.DUST_COLOR_TRANSITION;
    }
}
