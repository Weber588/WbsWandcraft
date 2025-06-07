package wbs.wandcraft;

import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;

public interface TextureProvider extends Keyed {
    @NotNull
    String getTexture();
}
