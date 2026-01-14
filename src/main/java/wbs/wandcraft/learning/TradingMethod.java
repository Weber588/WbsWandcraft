package wbs.wandcraft.learning;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsKeyed;
import wbs.utils.util.WbsMath;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.wandcraft.WbsWandcraft;

import java.text.DecimalFormat;
import java.util.*;

@NullMarked
public class TradingMethod extends RegistrableLearningMethod {
    private final String name;
    @Nullable
    private Integer villagerLevel = null;
    @Nullable
    private final Villager.Profession villagerProfession;
    private final boolean requireWanderingTrader;
    private int paymentMin = 1;
    private int paymentMax = 64;
    private ItemType paymentMaterial = ItemType.EMERALD;
    @Nullable
    private ItemStack itemCost;
    @Nullable
    private ItemStack replaceItem = null;
    private final boolean replaceExisting;
    private final double chance;
    private final boolean allowMultiple;

    public TradingMethod(ConfigurationSection parentSection, String key, String directory) throws InvalidConfigurationException {
        super(parentSection, key, directory);

        this.name = key;

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

        String villagerProfessionString = section.getString("villager-profession");
        if (villagerProfessionString != null && villagerProfessionString.toLowerCase().matches("wandering.?trader")) {
            villagerProfession = null;
            requireWanderingTrader = true;
        } else {
            villagerProfession = WbsConfigReader.getRegistryEntry(section, "villager-profession", RegistryKey.VILLAGER_PROFESSION, null);
            requireWanderingTrader = false;
        }

        ConfigurationSection paymentSection = section.getConfigurationSection("payment");
        if (paymentSection != null) {
            paymentMin = paymentSection.getInt("min", paymentMin);
            paymentMax = paymentSection.getInt("max", paymentMax);
            paymentMaterial = WbsConfigReader.getRegistryEntry(paymentSection, "material", RegistryKey.ITEM, ItemType.EMERALD);
        } else if (section.isInt("payment")) {
            paymentMin = section.getInt("payment", paymentMin);
            paymentMax = section.getInt("payment", paymentMax);
        }

        if (paymentMin > paymentMax) {
            int max = paymentMin;
            paymentMin = paymentMax;
            paymentMax = max;
        }

        String itemCostString = section.getString("item-cost");
        if (itemCostString != null) {
            try {
                itemCost = Bukkit.getItemFactory().createItemStack(itemCostString);
            } catch (IllegalArgumentException ex) {
                WbsWandcraft.getInstance().getSettings().logError("Invalid item string: " + itemCostString, directory + "/item-cost");
            }
        }

        replaceExisting = section.getBoolean("replace-existing", true);
        allowMultiple = section.getBoolean("allow-multiple", false);
        chance = section.getDouble("chance", 100);
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

        boolean isWanderingTrader = abstractVillager instanceof WanderingTrader;

        if (isWanderingTrader && (villagerLevel != null || villagerProfession != null)) {
            return;
        }

        if (abstractVillager instanceof Villager villager) {
            // nms Villager#increaseMerchantCareer increases level BEFORE acquiring trades -- reliable
            if (villagerLevel != null && villager.getVillagerLevel() != villagerLevel) {
                return;
            }

            if (villagerProfession != null && villager.getProfession() != villagerProfession) {
                return;
            }
        } else if (requireWanderingTrader && !isWanderingTrader) {
            return;
        }

        NamespacedKey tradeKey = WbsWandcraft.getKey(name);
        if (!allowMultiple) {
            if (abstractVillager.getPersistentDataContainer().has(tradeKey)) {
                return;
            }
        }

        ItemStack result = event.getRecipe().getResult();
        if (replaceItem != null && !result.isSimilar(replaceItem)) {
            return;
        }

        abstractVillager.getPersistentDataContainer().set(tradeKey, PersistentDataType.BOOLEAN, true);

        MerchantRecipe recipe = buildRecipe(event);
        if (replaceExisting) {
            event.setRecipe(recipe);
        } else {
            List<MerchantRecipe> recipes = new LinkedList<>(abstractVillager.getRecipes());
            recipes.add(recipe);
            abstractVillager.setRecipes(recipes);
        }
    }

    private @NotNull MerchantRecipe buildRecipe(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = new MerchantRecipe(resultGenerator.generateItem(), 1);

        MerchantRecipe replace = event.getRecipe();

        // TODO: Add more configurability over recipe details
        recipe.setDemand(replace.getDemand());
        recipe.setExperienceReward(replace.hasExperienceReward());
        recipe.setIgnoreDiscounts(replace.shouldIgnoreDiscounts());
        recipe.setVillagerExperience(replace.getVillagerExperience());
        recipe.setSpecialPrice(replace.getSpecialPrice());
        recipe.setPriceMultiplier(replace.getPriceMultiplier());

        if (paymentMin > 0) {
            //noinspection deprecation
            recipe.addIngredient(ItemStack.of(Objects.requireNonNull(paymentMaterial.asMaterial()), new Random().nextInt(paymentMin, paymentMax)));
        }
        if (itemCost != null) {
            recipe.addIngredient(itemCost);
        }

        return recipe;
    }

    private static final Map<Integer, String> LEVEL_NAMES = Map.of(
            1, "Novice",
            2, "Apprentice",
            3, "Journeyman",
            4, "Expert",
            5, "Master"
    );

    @Override
    public Component describe(Component indent, boolean shorten) {
        TextComponent description;

        if (replaceItem == null) {
            description = Component.text("On");
        } else {
            description = Component.text("Replacing ")
                    .append(replaceItem.effectiveName().style(Style.empty()))
                    .append(Component.text(" on"));
        }

        if (villagerLevel != null) {
            description = description.append(Component.text(" " + LEVEL_NAMES.get(villagerLevel)));
        }

        if (villagerProfession != null) {
            description = description.append(Component.text(" " + WbsKeyed.toPrettyString(villagerProfession)));
        }

        String type = "villager";
        if (requireWanderingTrader) {
            type = "wandering trader";
        }

        description = description.append(
                Component.text(" " + type + " trades: ")
                        .append(Component.text(new DecimalFormat("#.##").format(chance) + "%")
                                .color(NamedTextColor.AQUA)
                        )
        );

        return description;
    }
}
