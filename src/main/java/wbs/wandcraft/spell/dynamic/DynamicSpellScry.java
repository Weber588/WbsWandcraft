package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.TileState;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.map.MinecraftFont;
import org.bukkit.util.Transformation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.attributable.RadiusAttributable;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.dynamic.scry.ScryBlocks;
import wbs.wandcraft.spell.dynamic.scry.ScryNearbyContainers;
import wbs.wandcraft.spell.dynamic.scry.ScryTranslocateItem;
import wbs.wandcraft.spell.effect.SpellEffectInstance;

import java.util.*;
import java.util.function.Predicate;

@NullMarked
public abstract class DynamicSpellScry<T> extends DynamicSpell implements CastableSpell, RadiusAttributable {
    public static final MagicDomain PRIMARY = MagicDomain.ARCANE;
    public static final SpellArchetype SCRY = new FixedDomainSpellArchetype("scry", PRIMARY,
            Component.text("Taps into the arcane, revealing information about the immediate surroundings to the caster."),
            type -> {
        // TODO: Replace this with a map?
        if (type == null || type == MagicDomain.ARCANE) {
            return new ScryNearbyContainers(MagicDomain.ARCANE);
        } else if (type == MagicDomain.NETHER) {

        } else if (type == MagicDomain.ENDER) {
            return new ScryTranslocateItem(type);
        } else if (type == MagicDomain.SCULK) {

        } else if (type == MagicDomain.VOID) {

        } else if (type == MagicDomain.NATURE) {
            return new ScryBlocks(type);
        }

        throw new NotImplementedException("Scry method not implemented for " + type.getKey().asString());
    });

    public static final int BOLD_SPACE_WIDTH = 5;
    public static final int NORMAL_SPACE_WIDTH = 4;
    private static final Set<Material> WHITELISTED_TILE_ENTITY_DISPLAYS = Set.of(
            Material.FURNACE,
            Material.SMOKER,
            Material.BLAST_FURNACE,
            Material.JUKEBOX,
            Material.DECORATED_POT,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.BEACON,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.PINK_SHULKER_BOX
    );

    public DynamicSpellScry(@Nullable MagicDomain secondary) {
        super(SCRY, PRIMARY, secondary);
    }

    @Override
    protected Multimap<MagicDomain, SpellEffectInstance<?>> typedEvents() {
        return HashMultimap.create();
    }

    protected Dialog getDialog(CastContext context, Map<T, Integer> found) {
        return getDialog(context, found, _ -> true, "");
    }
    protected Dialog getDialog(CastContext context, Map<T, Integer> found, Predicate<T> predicate) {
        return getDialog(context, found, predicate, "");
    }
    protected Dialog getDialog(CastContext context, Map<T, Integer> found, Predicate<T> predicate, String searchText) {
        List<DialogBody> displays = new LinkedList<>();

        LinkedHashMap<T, Integer> orderedFound = new LinkedHashMap<>();
        found.entrySet()
                .stream()
                .filter(entry -> predicate.test(entry.getKey())
                ).sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(entry -> orderedFound.put(entry.getKey(), entry.getValue()));

        int maxWidth = 0;
        for (T t : orderedFound.keySet()) {
            String asString = this.asString(t);
            asString = asString + " x" + found.get(t);

            maxWidth = Math.max(MinecraftFont.Font.getWidth(asString), maxWidth);
        }

        int dialogWidth = maxWidth + BOLD_SPACE_WIDTH * 2;

        for (T t : orderedFound.keySet()) {
            String asString = this.asString(t);
            int amount = found.get(t);
            asString = asString + " x" + amount;

            int width = MinecraftFont.Font.getWidth(asString);
            int offset = maxWidth - width;

            int boldSpaces = offset % NORMAL_SPACE_WIDTH;
            int normalSpaces = (offset - BOLD_SPACE_WIDTH * boldSpaces) / NORMAL_SPACE_WIDTH;
            if (normalSpaces < 0) {
                boldSpaces = offset / BOLD_SPACE_WIDTH;
                normalSpaces = 0;
            }

            Component displayText = asComponent(t)
                    .append(Component.text(" x" + amount))
                    .append(
                            Component.text(" ".repeat(boldSpaces)).decorate(TextDecoration.BOLD)
                    )
                    .append(
                            Component.text(" ".repeat(normalSpaces))
                    );
            Component description = displayText.clickEvent(
                    ClickEvent.callback(audience -> {
                        if (audience instanceof Player player) {
                            this.onClick(context, t, player);
                        }
                    })
            );

            Component hoverText = getHoverText(t);
            if (hoverText != null) {
                description = description.hoverEvent(getHoverText(t));
            }

            ItemDialogBody dialogBody = getDialogBody(t).description(
                    DialogBody.plainMessage(
                            description,
                            Math.clamp(dialogWidth, 0, 1024)
                    )
            ).build();

            displays.add(dialogBody);
        }

        List<ActionButton> buttons = List.of(
                ActionButton.builder(Component.text("Search"))
                        .action(DialogAction.customClick((view, audience) -> {
                            String newSearch = view.getText("search");

                            if (newSearch != null && !Objects.equals(searchText, newSearch)) {
                                Dialog dialog = getDialog(context, found, t -> searchAgainst(t, newSearch), newSearch);

                                audience.showDialog(dialog);
                            }
                        }, ClickCallback.Options.builder().uses(1).build()))
                        .build()
        );

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(displayName())
                        .body(displays)
                        .inputs(
                                List.of(
                                        DialogInput.text("search", Component.text("Search"))
                                                .initial(searchText)
                                                .build()
                                )
                        )
                        .build()
                )
                .type(
                        DialogType.multiAction(buttons)
                                .columns(2)
                                .build()
                )
        );
    }

    protected abstract @Nullable Component getHoverText(T t);

    protected abstract ItemDialogBody.Builder getDialogBody(T t);

    protected void highlightBlocks(CastContext context, Player player, Set<Block> toHighlight) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        Set<BlockDisplay> displays = new HashSet<>();

        toHighlight.forEach(block -> {
            context.location().getWorld().spawn(block.getLocation(), BlockDisplay.class, display -> {
                // Most blocks can be set to themselves, but some (like chests) break when trying -- don't show tile
                // entities, unless we know they work and are in the whitelist
                if (block.getState() instanceof TileState && !WHITELISTED_TILE_ENTITY_DISPLAYS.contains(block.getType())) {
                    display.setBlock(Material.GRAY_STAINED_GLASS.createBlockData());
                } else {
                    display.setBlock(block.getBlockData());
                }

                Transformation transformation = display.getTransformation();
                float scale = 1.0001f;
                transformation.getScale().set(scale);
                transformation.getTranslation().set(-scale / 2 + 0.5);
                display.setTransformation(transformation);
                display.setBrightness(new Display.Brightness(15, 15));

                displays.add(display);

                display.setPersistent(false);
                display.setGlowing(true);

                Color color = getPrimaryDomain().color();
                MagicDomain secondaryMagicDomain = this.getSecondaryDomain();
                if (secondaryMagicDomain != null) {
                    color = secondaryMagicDomain.color();
                }
                display.setGlowColorOverride(color);

                display.setVisibleByDefault(false);
                player.showEntity(plugin, display);
            });
        });

        plugin.runLater(() -> displays.forEach(Entity::remove), 60L);
    }

    protected String asString(T t) {
        return PlainTextComponentSerializer.plainText().serialize(asComponent(t));
    }

    protected abstract Component asComponent(T t);

    public abstract void onClick(CastContext context, T clicked, Player whoClicked);

    protected boolean searchAgainst(T t, String search) {
        String modifiedSearch;

        if (!search.startsWith("#")) {
            modifiedSearch = search;
        } else {
            modifiedSearch = search.substring(1);

            Material material = null;
            if (t instanceof ItemStack itemStack) {
                material = itemStack.getType();
            } else if (t instanceof Block block) {
                material = block.getType();
            } else if (t instanceof Material tMat) {
                material = tMat;
            }

            if (material != null && searchMaterialTags(material, search)) {
                return true;
            }
        }

        String toSearch = asSearchableString(t);

        return Strings.isBlank(modifiedSearch) ||
                toSearch.matches(modifiedSearch.toLowerCase());
    }

    @Override
    protected String rawDescription() {
        return "Scry in a radius, gaining information about your surroundings, depending on the used spell types.";
    }

    protected boolean searchMaterialTags(Material material, String search) {
        TagKey<ItemType> asItemTag = null;
        TagKey<BlockType> asBlockTag = null;
        String modifiedSearch;

        if (!search.startsWith("#")) {
            modifiedSearch = search;
        } else {
            modifiedSearch = search.substring(1);

            asItemTag = getTagKey(search, RegistryKey.ITEM);
            asBlockTag = getTagKey(search, RegistryKey.BLOCK);
        }

        boolean inTag = false;
        if (asItemTag != null) {
            inTag |= RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)
                    .getTagValues(asItemTag)
                    .contains(material.asItemType());
        }
        if (asBlockTag != null) {
            inTag |= RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK)
                    .getTagValues(asBlockTag)
                    .contains(material.asBlockType());
        }

        return inTag;
    }

    private static <T extends Keyed> @Nullable TagKey<T> getTagKey(String search, RegistryKey<T> registryKey) {
        TagKey<T> tagSearch = null;

        Registry<T> registry = RegistryAccess.registryAccess().getRegistry(registryKey);

        NamespacedKey namespacedKey = NamespacedKey.fromString(search);
        if (namespacedKey != null) {
            tagSearch = TagKey.create(registryKey, namespacedKey);

            if (!registry.hasTag(tagSearch)) {
                return null;
            }
        }
        return tagSearch;
    }

    protected abstract String asSearchableString(T t);

}
