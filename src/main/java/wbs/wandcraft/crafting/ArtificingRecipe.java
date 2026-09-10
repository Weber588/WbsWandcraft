package wbs.wandcraft.crafting;

import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.util.ItemUtils;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

@NullMarked
public record ArtificingRecipe(
        NamespacedKey key,
        RecipeChoice baseItem,
        int shardCost,
        @Nullable RecipeChoice ingredient,
        int ingredientAmount,
        ItemStack result
) implements Keyed {
    public ArtificingRecipe {
        WandcraftRegistries.ARTIFICING_RECIPES.register(this);
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key + ": " +
                ", base=" + getRecipeChoiceString(baseItem) +
                ", shardCost=" + shardCost +
                (ingredient == null ? "" :
                        ", ingredient=" + getRecipeChoiceString(ingredient) +
                        ", ingredientAmount=" + ingredientAmount
                )
                ;
    }

    @SuppressWarnings("UnstableApiUsage")
    private String getRecipeChoiceString(RecipeChoice choice) {
        if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            return "Exact match: " + exactChoice.getChoices().stream()
                    .map(ArtificingRecipe::getItemString)
                    .collect(Collectors.joining(" | "))
                    ;
        } else if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            return "Material match: " + materialChoice.getChoices().stream()
                    .map(type -> type.getKey().asMinimalString())
                    .collect(Collectors.joining(" | "))
                    ;
        } else if (choice instanceof RecipeChoice.ItemTypeChoice itemChoice) {
            RegistryKeySet<ItemType> keySet = itemChoice.itemTypes();

            return switch (keySet) {
                case Tag<ItemType> tag -> tag.tagKey().key().asMinimalString();
                default -> keySet.values().stream()
                        .map(TypedKey::key)
                        .map(Key::asMinimalString)
                        .collect(Collectors.joining(" | "));
            };
        } else if (choice instanceof RecipeChoice.PredicateChoice) {
            return "Predicate (tell jane to improve this lol)";
        }
        return choice.toString();
    }

    private static String getItemString(ItemStack item) {
        String wandcraftName = item.getPersistentDataContainer().get(ItemUtils.WANDCRAFT_ITEM_NAME, PersistentDataType.STRING);
        if (wandcraftName != null) {
            return wandcraftName;
        }

        return item.getType().getKey().asMinimalString() + item.getItemMeta().getAsComponentString();
    }
}
