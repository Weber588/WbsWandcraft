package wbs.wandcraft.spellbook;

import io.papermc.paper.advancement.AdvancementDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.LecternView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.utils.util.pluginhooks.PacketEventsWrapper;
import wbs.wandcraft.ItemDecorator;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.ResourcePackObjects.*;
import wbs.wandcraft.resourcepack.TextureLayer;
import wbs.wandcraft.resourcepack.TextureProvider;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.util.persistent.PersistentSpellbookType;

import java.util.*;

@NullMarked
public class Spellbook implements ItemDecorator, TextureProvider {
    private static final NamespacedKey SPELL_BOOK = WbsWandcraft.getKey("spellbook");

    public static List<SpellDefinition> getKnownSpells(PersistentDataViewHolder holder) {
        return getKnownSpells(holder.getPersistentDataContainer());
    }
    public static List<SpellDefinition> getKnownSpells(PersistentDataContainerView container) {
        List<NamespacedKey> knownSpellKeys = getKnownSpellKeys(container);
        List<SpellDefinition> knownSpells = new LinkedList<>();

        for (NamespacedKey spellKey : knownSpellKeys) {
            SpellDefinition spellDefinition = WandcraftRegistries.SPELLS.get(spellKey);

            if (spellDefinition != null) {
                knownSpells.add(spellDefinition);
            }
        }

        return knownSpells;
    }
    private static List<NamespacedKey> getKnownSpellKeys(PersistentDataViewHolder holder) {
        return getKnownSpellKeys(holder.getPersistentDataContainer());
    }
    private static List<NamespacedKey> getKnownSpellKeys(PersistentDataContainerView container) {
        List<NamespacedKey> knownKeys = container.get(PersistentSpellbookType.KNOWN_SPELLS, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.NAMESPACED_KEY));

        if (knownKeys == null) {
            knownKeys = new LinkedList<>();
        }
        return knownKeys;
    }
    public static void setKnownSpells(PersistentDataHolder holder, List<SpellDefinition> knownSpells) {
        setKnownSpells(holder.getPersistentDataContainer(), knownSpells);
    }
    public static void setKnownSpells(PersistentDataContainer container, List<SpellDefinition> knownSpells) {
        container.set(PersistentSpellbookType.KNOWN_SPELLS, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.NAMESPACED_KEY), knownSpells.stream().map(SpellDefinition::getKey).toList());
    }


    private static final TextComponent LINE_BREAK = Component.newline().append(Component.text("                            ").decorate(TextDecoration.STRIKETHROUGH)).appendNewline();

    @Nullable
    public static Spellbook fromItem(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }

        return item.getPersistentDataContainer().get(SPELL_BOOK, CustomPersistentDataTypes.SPELLBOOK_TYPE);
    }

    public static boolean isSpellbook(PersistentDataViewHolder holder) {
        return isSpellbook(holder.getPersistentDataContainer());
    }

    public static boolean isSpellbook(PersistentDataContainerView container) {
        return container.has(SPELL_BOOK);
    }

    private final Set<SpellDefinition> knownSpells = new HashSet<>();
    private int currentPage;

    public Spellbook(Collection<SpellDefinition> knownSpells, int currentPage) {
        this.currentPage = currentPage;
        this.knownSpells.addAll(knownSpells);
    }
    public Spellbook() {
        currentPage = 0;
    }

    public static void teachSpell(PersistentDataHolder holder, SpellDefinition spell) {
        List<SpellDefinition> knownSpells = Spellbook.getKnownSpells(holder);

        if (!knownSpells.contains(spell)) {
            knownSpells.add(spell);

            if (holder instanceof Player player) {
                Component message = Component.text("Learnt ").append(spell.displayName().color(NamedTextColor.AQUA)).append(Component.text("!"));
                boolean sentToast = PacketEventsWrapper.sendToast(ItemUtils.buildSpell(spell), message, AdvancementDisplay.Frame.GOAL, player);

                if (!sentToast) {
                    WbsWandcraft.getInstance().buildMessage(message).send(player);
                }
            }

            Spellbook.setKnownSpells(holder, knownSpells);
        }
    }

    public Set<SpellDefinition> knownSpells() {
        return Collections.unmodifiableSet(knownSpells);
    }
    public int currentPage() {
        return currentPage;
    }

    public Spellbook learnSpell(SpellDefinition definition) {
        knownSpells.add(definition);

        return this;
    }

    public Spellbook learnSpells(List<SpellDefinition> definitions) {
        knownSpells.addAll(definitions);

        return this;
    }
    public Spellbook learnSpells(Player fromPlayer) {
        List<SpellDefinition> knownSpells = getKnownSpells(fromPlayer);
        this.knownSpells.addAll(knownSpells);

        return this;
    }

    public Spellbook teachPlayer(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        List<NamespacedKey> knownKeys = new LinkedList<>(getKnownSpellKeys(container));

        knownSpells.stream().map(SpellDefinition::getKey).forEach(knownKeys::add);

        container.set(PersistentSpellbookType.KNOWN_SPELLS, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.NAMESPACED_KEY), knownKeys);

        return this;
    }

    public Spellbook openBook(Player player) {
        learnSpells(player);
        teachPlayer(player);

        Component chapterPage = Component.empty();

        chapterPage = chapterPage.append(Component.text("Contents").decorate(TextDecoration.BOLD));
        chapterPage = chapterPage.append(LINE_BREAK.color(NamedTextColor.GOLD));

        chapterPage = chapterPage.append(getChapterLink(1, "Contents"));
        chapterPage = chapterPage.append(getChapterLink(2, "Spell Descriptions"));

        List<SpellDefinition> allDefinitions = getOrderedSpells();

        List<Component> spellPages = new LinkedList<>();
        for (SpellDefinition definition : allDefinitions) {
            Component page = Component.empty();

            TextColor textColor = TextColor.color(0x6F47A3);
            Component displayName = definition.displayName().color(textColor);
            boolean isKnown = knownSpells.contains(definition);
            if (!isKnown) {
                displayName = displayName.font(Key.key("illageralt"));
            }

            page = page.append(displayName);

            // TODO: Add spell schools

            page = page.append(LINE_BREAK.color(NamedTextColor.GOLD));

            Component description = definition.description().color(textColor).decorate(TextDecoration.ITALIC);
            if (!isKnown) {
                description = description.font(Key.key("alt"));
            }
            page = page.append(description);

            spellPages.add(page);
        }

        WrittenBookContent.Builder book = WrittenBookContent.writtenBookContent("Spellbook", player.getName());

        book.addPage(chapterPage);
        spellPages.forEach(book::addPage);


        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        toItem(item);
        item.setData(DataComponentTypes.WRITTEN_BOOK_CONTENT, book);

        Location checkLoc = player.getLocation();
        checkLoc.setY(player.getWorld().getMaxHeight());
        while (checkLoc.getY() >= checkLoc.getWorld().getMinHeight() && !checkLoc.getBlock().isEmpty()) {
            checkLoc = checkLoc.add(0, -1, 0);
        }

        if (checkLoc.getY() < checkLoc.getWorld().getMinHeight()) {
            checkLoc = player.getLocation().add(0, 2, 0);
        }

        Location createLoc = checkLoc;
        LecternView lecternView = MenuType.LECTERN.builder().checkReachable(false).location(createLoc).build(player);
        lecternView.setPage(currentPage);
        lecternView.setItem(0, item);
        lecternView.open();

        createLoc.getBlock().setType(Material.AIR);

        WbsWandcraft.getInstance().runAtEndOfTick(() -> {
            lecternView.setPage(currentPage);
            createLoc.getBlock().setType(Material.AIR);
        });

        return this;
    }

    private static @NotNull List<SpellDefinition> getOrderedSpells() {
        return WandcraftRegistries.SPELLS.stream()
                .sorted(Comparator.comparing(SpellDefinition::getKey))
                .toList();
    }

    @Override
    public Model buildBaseModel() {
        return new ConditionModel(
                "using_item",
                new StaticModel(namespace() + ":item/" + textureName() + "_active", getTints()),
                TextureProvider.super.buildBaseModel()
        );
    }

    @Override
    public Map<String, ModelDefinition> getModelDefinitions() {
        Map<String, ModelDefinition> namedModelDefinitions = new HashMap<>();

        ModelDefinition baseModelDefinition = new ModelDefinition(
                "minecraft:item/generated",
                namespace() + ":item/" + textureName()
        );

        namedModelDefinitions.put(textureName(), baseModelDefinition);

        ModelDefinition activeModelDefinition = new ModelDefinition(
                "minecraft:item/generated",
                namespace() + ":item/" + textureName() + "_active"
        );

        DisplayTransform rightTransform = new DisplayTransform()
                .translation(1.13, 0.2, 3.13)
                .rotation(80, -90, 0)
                .scale(0.7, 0.7, 0.7);

        DisplayTransform leftTransform = new DisplayTransform()
                .translation(1.13, 0.2, 3.13)
                .rotation(80, -90, 0)
                .scale(0.7, 0.7, 0.7);

        DisplayTransform right3rdTransform = new DisplayTransform()
                .translation(0, 4, 1)
                .scale(0.75, 0.75, 0.75);

        DisplayTransform left3rdTransform = new DisplayTransform()
                .translation(1.13, 4.2, 1.13)
                .scale(0.75, 0.75, 0.75);

        activeModelDefinition.addDisplay(ItemDisplayTransform.FIRSTPERSON_RIGHTHAND, rightTransform);
        activeModelDefinition.addDisplay(ItemDisplayTransform.FIRSTPERSON_LEFTHAND, leftTransform);
        activeModelDefinition.addDisplay(ItemDisplayTransform.THIRDPERSON_RIGHTHAND, right3rdTransform);
        activeModelDefinition.addDisplay(ItemDisplayTransform.THIRDPERSON_LEFTHAND, left3rdTransform);

        namedModelDefinitions.put(textureName() + "_active", activeModelDefinition);

        return namedModelDefinitions;
    }

    private static @NotNull TextComponent getChapterLink(@Positive int page, String title) {
        return Component.text(page + ". " + title).clickEvent(ClickEvent.changePage(page)).appendNewline();
    }

    public void toItem(ItemStack item) {
        item.editMeta(meta -> {
            PersistentDataContainer container = meta.getPersistentDataContainer();

            container.set(SPELL_BOOK, CustomPersistentDataTypes.SPELLBOOK_TYPE, this);
            ItemDecorator.decorate(this, meta);
        });
    }

    @Override
    public @Nullable Component getItemName() {
        return Component.text("Spellbook");
    }

    @Override
    public @NotNull List<Component> getLore() {
        return List.of();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("wbswandcraft", "spellbook");
    }

    @Override
    public @NotNull List<TextureLayer> getTextures() {
        return List.of(
                new TextureLayer("spellbook"),
                new TextureLayer("spellbook_active")
        );
    }

    public void tryCasting(Player player, ItemStack item) {
        List<SpellDefinition> allDefinitions = getOrderedSpells();

        if (currentPage >= 1) {
            SpellDefinition definition = allDefinitions.get(currentPage - 1);

            if (!getKnownSpells(player).contains(definition)) {
                Component errorMessage = definition.displayName().font(Key.key("illageralt")).color(NamedTextColor.DARK_PURPLE);

                errorMessage = errorMessage.append(Component.text("???").color(NamedTextColor.DARK_RED).font(Key.key("default")));

                player.sendActionBar(errorMessage);

                player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1, 2);
                return;
            }

            new SpellInstance(definition).cast(player, () -> {});
            player.setCooldown(item, 5 * definition.getAttribute(CastableSpell.COOLDOWN, Ticks.TICKS_PER_SECOND));
        }
    }

    public void currentPage(int newPage) {
        this.currentPage = Math.max(0, newPage);
    }
}
