package wbs.wandcraft;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import org.bukkit.block.BlockType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@SuppressWarnings("unused")
public class WbsWandcraftBootstrap implements PluginBootstrap {
    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new WbsWandcraft();
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.BLOCK).newHandler(event -> {
            event.registrar().addToTag(BlockTypeTagKeys.SCULK_REPLACEABLE, Set.of(TypedKey.create(RegistryKey.BLOCK, BlockType.AMETHYST_BLOCK.key())));
        }));
    }
}
