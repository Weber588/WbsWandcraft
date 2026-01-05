package wbs.wandcraft.learning;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsMath;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.wandcraft.WbsWandcraft;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@NullMarked
public class TradingMethod extends RegistrableLearningMethod {
    @Nullable
    private Integer villagerLevel = null;
    @Nullable
    private final Villager.Profession villagerProfession;
    // TODO: Make this be a generator for a wand, spell, or modifier
    private ItemStack result;
    private int emeraldCostMin = 1;
    private int emeraldCostMax = 64;
    @Nullable
    private ItemStack itemCost;
    @Nullable
    private ItemStack replaceItem = null;
    private boolean replaceExisting = true;
    private double chance = 100;

    public TradingMethod(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section == null) {
            throw new InvalidConfigurationException("Must be a section.", directory);
        }

        if (section.isInt("villager-level")) {
            villagerLevel = section.getInt("villager-level");
            if (villagerLevel < 1 || villagerLevel > 5) {
                villagerLevel = null;
                WbsWandcraft.getInstance().getSettings().logError("villager-level must be between 1 and 5 inclusive.", directory + "/villager-level");
            }
        }

        String replaceItemString = section.getString("replace");
        if (replaceItemString != null) {
            try {
                replaceItem = Bukkit.getItemFactory().createItemStack(replaceItemString);
            } catch (IllegalArgumentException ex) {
                WbsWandcraft.getInstance().getSettings().logError("Invalid item string: " + replaceItemString, directory + "/replace");
            }
        }

        villagerProfession = WbsConfigReader.getRegistryEntry(section, "villager-profession", RegistryKey.VILLAGER_PROFESSION, null);

        String resultItemString = section.getString("result");
        if (resultItemString != null) {
            try {
                result = Bukkit.getItemFactory().createItemStack(resultItemString);
            } catch (IllegalArgumentException ex) {
                WbsWandcraft.getInstance().getSettings().logError("Invalid item string: " + resultItemString, directory + "/result");
            }
        }

        ConfigurationSection emeraldCostSection = section.getConfigurationSection("emerald-cost");
        if (emeraldCostSection != null) {
            emeraldCostMin = emeraldCostSection.getInt("min", emeraldCostMin);
            emeraldCostMax = emeraldCostSection.getInt("max", emeraldCostMax);
        } else if (section.isInt("emerald-cost")) {
            emeraldCostMin = section.getInt("emerald-cost", emeraldCostMin);
            emeraldCostMax = section.getInt("emerald-cost", emeraldCostMax);
        }

        if (emeraldCostMin > emeraldCostMax) {
            int max = emeraldCostMin;
            emeraldCostMin = emeraldCostMax;
            emeraldCostMax = max;
        }

        String itemCostString = section.getString("item-cost");
        if (itemCostString != null) {
            try {
                itemCost = Bukkit.getItemFactory().createItemStack(itemCostString);
            } catch (IllegalArgumentException ex) {
                WbsWandcraft.getInstance().getSettings().logError("Invalid item string: " + itemCostString, directory + "/item-cost");
            }
        }

        replaceExisting = section.getBoolean("replace-existing");
        chance = section.getDouble("chance", chance);
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), VillagerAcquireTradeEvent.class, this::onTradePrep);
    }

    private void onTradePrep(VillagerAcquireTradeEvent event) {
        AbstractVillager abstractVillager = event.getEntity();
        
        if (!WbsMath.chance(chance)) {
            return;
        }

        if (!(abstractVillager instanceof Villager villager)) {
            return;
        }

        // nms Villager#increaseMerchantCareer increases level BEFORE acquiring trades -- reliable
        if (villagerLevel != null && villager.getVillagerLevel() != villagerLevel) {
            return;
        }

        if (villagerProfession != null && villager.getProfession() != villagerProfession) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();
        if (replaceItem != null && !result.isSimilar(replaceItem)) {
            return;
        }

        MerchantRecipe recipe = buildRecipe(event);
        if (replaceExisting) {
            event.setRecipe(recipe);
        } else {
            WbsWandcraft.getInstance().runAtEndOfTick(() -> {
                Villager updatedVillager = (Villager) Bukkit.getEntity(villager.getUniqueId());
                if (updatedVillager == null) {
                    return;
                }

                List<MerchantRecipe> recipes = new LinkedList<>(updatedVillager.getRecipes());
                recipes.add(recipe);
                updatedVillager.setRecipes(recipes);
            });

        }
    }

    private @NotNull MerchantRecipe buildRecipe(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = new MerchantRecipe(result, 1);

        MerchantRecipe replace = event.getRecipe();

        // TODO: Add more configurability over recipe details
        recipe.setDemand(replace.getDemand());
        recipe.setExperienceReward(replace.hasExperienceReward());
        recipe.setIgnoreDiscounts(replace.shouldIgnoreDiscounts());
        recipe.setVillagerExperience(replace.getVillagerExperience());
        recipe.setSpecialPrice(replace.getSpecialPrice());
        recipe.setPriceMultiplier(replace.getPriceMultiplier());

        if (emeraldCostMin > 0) {
            recipe.addIngredient(ItemStack.of(Material.EMERALD, new Random().nextInt(emeraldCostMin, emeraldCostMax)));
        }
        if (itemCost != null) {
            recipe.addIngredient(itemCost);
        }

        return recipe;
    }

    @Override
    public Component describe(Component indent, boolean shorten) {
        return null;
    }
}
