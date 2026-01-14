package wbs.wandcraft.spellbook;

import com.google.common.collect.Multimap;
import io.papermc.paper.advancement.AdvancementDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
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
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.generation.SpellInstanceGenerator;
import wbs.wandcraft.learning.LearningMethod;
import wbs.wandcraft.learning.RegistrableLearningMethod;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.util.ItemDecorator;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.types.WandType;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@NullMarked
public class Spellbook implements ItemDecorator {
    private static final NamespacedKey SPELL_BOOK = WbsWandcraft.getKey("spellbook");
    public static final @NotNull NamespacedKey KNOWN_SPELLS = WbsWandcraft.getKey("known_spells");
    public static final @NotNull TextColor DESCRIPTION_COLOR = TextColor.color(0x6F47A3);

    private static final TextComponent LINE_BREAK = Component.newline().append(Component.text("                            ").decorate(TextDecoration.STRIKETHROUGH)).appendNewline();

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
        List<NamespacedKey> knownKeys = container.get(
                KNOWN_SPELLS,
                PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.NAMESPACED_KEY)
        );

        if (knownKeys == null) {
            knownKeys = new LinkedList<>();
        }
        return knownKeys;
    }
    public static void setKnownSpells(PersistentDataHolder holder, List<SpellDefinition> knownSpells) {
        setKnownSpells(holder.getPersistentDataContainer(), knownSpells);
    }
    public static void setKnownSpells(PersistentDataContainer container, List<SpellDefinition> knownSpells) {
        container.set(KNOWN_SPELLS,
                PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.NAMESPACED_KEY),
                knownSpells.stream()
                        .map(SpellDefinition::getKey)
                        .distinct()
                        .toList()
        );
    }
    public static boolean knowsSpell(PersistentDataViewHolder holder, SpellDefinition definition) {
        return getKnownSpells(holder).contains(definition);
    }

    public static void teachSpell(PersistentDataHolder holder, SpellDefinition spell) {
        teachSpells(holder, List.of(spell));
    }
    public static void teachSpells(PersistentDataHolder holder, List<SpellDefinition> spells) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        List<SpellDefinition> knownSpells = new LinkedList<>(getKnownSpells(container));

        List<SpellDefinition> toAdd = spells.stream().filter(spell -> !knownSpells.contains(spell)).toList();

        if (toAdd.isEmpty()) {
            return;
        }

        knownSpells.addAll(toAdd);

        if (holder instanceof Player player) {
            showLearntToast(player, toAdd);

            if (WandcraftRegistries.SPELLS.stream().count() == knownSpells.size()) {
                Component message = Component.text("Learnt all spells!");
                ItemStack icon = ItemUtils.buildSpellbook();
                boolean sentToast = PacketEventsWrapper.sendToast(icon, message, AdvancementDisplay.Frame.CHALLENGE, player);

                if (!sentToast) {
                    WbsWandcraft.getInstance().buildMessage(message).send(player);
                }
            }
        }

        setKnownSpells(container, knownSpells);
    }

    public static void forgetSpell(PersistentDataHolder holder, SpellDefinition forgetSpell) {
        forgetSpells(holder, List.of(forgetSpell));
    }
    public static void forgetSpells(PersistentDataHolder holder, List<SpellDefinition> forgetSpells) {
        List<SpellDefinition> knownSpells = Spellbook.getKnownSpells(holder);

        List<SpellDefinition> toForget = new LinkedList<>();

        for (SpellDefinition forgetSpell : forgetSpells) {
            if (knownSpells.contains(forgetSpell)) {
                toForget.add(forgetSpell);
            }
        }

        if (!toForget.isEmpty()) {
            knownSpells.removeAll(toForget);
            if (holder instanceof Player player) {
                Component message;
                if (toForget.size() == 1) {
                    message = Component.text("Forgot ").append(toForget.getFirst().displayName().color(NamedTextColor.AQUA)).append(Component.text("!"));
                } else {
                    message = Component.text("Forgot ").append(Component.text(toForget.size()).color(NamedTextColor.AQUA)).append(Component.text(" spells!"));
                }

                WbsWandcraft.getInstance().buildMessage(message).send(player);
            }

            Spellbook.setKnownSpells(holder, knownSpells);
        }
    }

    private static void showLearntToast(Player player, List<SpellDefinition> toLearn) {
        Component message;
        ItemStack icon;
        if (toLearn.size() == 1) {
            SpellDefinition spell = toLearn.getFirst();
            message = Component.text("Learnt ").append(spell.displayName().color(NamedTextColor.AQUA)).append(Component.text("!"));
            icon = ItemUtils.buildSpell(spell);
        } else {
            message = Component.text("Learnt ").append(Component.text(toLearn.size()).color(NamedTextColor.AQUA)).append(Component.text(" spells!"));
            icon = ItemUtils.buildSpellbook();
        }
        boolean sentToast = PacketEventsWrapper.sendToast(icon, message, AdvancementDisplay.Frame.GOAL, player);

        if (!sentToast) {
            WbsWandcraft.getInstance().buildMessage(message).send(player);
        }
    }

    @Nullable
    public static Spellbook fromItem(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }

        return item.getPersistentDataContainer().get(SPELL_BOOK, CustomPersistentDataTypes.SPELLBOOK_TYPE);
    }

    public static boolean isSpellbook(@Nullable PersistentDataViewHolder holder) {
        if (holder == null) {
            return false;
        }
        return isSpellbook(holder.getPersistentDataContainer());
    }

    public static boolean isSpellbook(PersistentDataContainerView container) {
        return container.has(SPELL_BOOK);
    }

    private int currentPage;

    public Spellbook(int currentPage) {
        this.currentPage = currentPage;
    }
    public Spellbook() {
        currentPage = 0;
    }

    public int currentPage() {
        return currentPage;
    }

    public Spellbook openBook(Player player) {
        if (player.getGameMode() == GameMode.SURVIVAL) {
            PacketEventsWrapper.sendGameModeChange(player, GameMode.ADVENTURE);
        }

        List<WandType<?>> wandDefinitions = getOrderedWands();
        List<SpellDefinition> spellDefinitions = getOrderedSpells();

        Component chapterPage = Component.empty();

        chapterPage = chapterPage.append(Component.text("Contents").decorate(TextDecoration.BOLD));
        chapterPage = chapterPage.append(LINE_BREAK.color(NamedTextColor.GOLD));

        chapterPage = chapterPage.append(getChapterLink(1, "Contents"));
        chapterPage = chapterPage.append(getChapterLink(2, "Wands"));
        chapterPage = chapterPage.append(getChapterLink(2 + wandDefinitions.size(), "Spell Descriptions"));

        List<Component> wandPages = getWandPages(wandDefinitions);

        List<Component> spellPages = getSpellPages(player, spellDefinitions);

        WrittenBookContent.Builder book = WrittenBookContent.writtenBookContent("Spellbook", player.getName());

        book.addPage(chapterPage);
        wandPages.forEach(book::addPage);
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

    private static @NotNull List<Component> getWandPages(List<WandType<?>> wandDefinitions) {
        List<Component> wandPages = new LinkedList<>();
        for (WandType<?> type : wandDefinitions) {
            Component page = Component.empty().append(type.getItemName().color(NamedTextColor.GOLD))
                    .append(LINE_BREAK)
                    .append(type.getDescription().color(DESCRIPTION_COLOR).decorate(TextDecoration.ITALIC));

            wandPages.add(page);
        }
        return wandPages;
    }

    private static @NotNull List<Component> getSpellPages(Player player, List<SpellDefinition> allDefinitions) {
        List<Component> spellPages = new LinkedList<>();
        for (SpellDefinition definition : allDefinitions) {
            Component page = Component.empty();

            Component displayName = definition.displayName();
            boolean isKnown = knowsSpell(player, definition);

            page = page.append(displayName);
            page = page.appendNewline().append(definition.getTypesDisplay());

            page = page.append(LINE_BREAK.color(NamedTextColor.GOLD));

            Component description = definition.description().color(DESCRIPTION_COLOR).decorate(TextDecoration.ITALIC);
            if (!isKnown) {
                description = description.font(Key.key("illageralt"));
            }
            page = page.append(description);

            if (!isKnown) {
                boolean isObtainable = false;
                Component hoverText = Component.empty();

                Multimap<SpellDefinition, LearningMethod> learningMap = WbsWandcraft.getInstance().getSettings().getLearningMap();

                Collection<LearningMethod> methodList = learningMap.get(definition);
                Component indent = Component.text("  ");
                if (!methodList.isEmpty()) {
                    isObtainable = true;
                    hoverText = hoverText.append(Component.join(
                            JoinConfiguration.builder()
                                    .separator(Component.newline())
                                    .build(),
                            methodList.stream().map(method ->
                                            method.describe(indent, false).color(NamedTextColor.GOLD)
                                    ).toList()
                            ));
                }

                List<RegistrableLearningMethod> generationMethods = WbsWandcraft.getInstance().getSettings().getGenerationMethods();

                if (!generationMethods.isEmpty()) {
                    List<Component> registrableMethods = new LinkedList<>();
                    for (RegistrableLearningMethod method : generationMethods) {
                        if (method.getResultGenerator() instanceof SpellInstanceGenerator generator) {
                            if (generator.getSpells().contains(definition)) {
                                registrableMethods.add(method.describe(indent).color(NamedTextColor.GOLD));
                            }
                        }
                    }

                    if (!registrableMethods.isEmpty()) {
                        if (isObtainable) {
                            hoverText = hoverText.appendNewline();
                        }

                        isObtainable = true;

                        hoverText = hoverText.append(
                                Component.join(
                                        JoinConfiguration.builder().separator(Component.newline()).build(),
                                        registrableMethods
                                )
                        );
                    }
                }

                if (!isObtainable) {
                    hoverText = Component.text("Unknown...").decorate(TextDecoration.ITALIC).color(DESCRIPTION_COLOR);
                }
                page = page.hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.runCommand("wbswandcraft:wbswandcraft spell info " + definition.key().asString()));
            }
            spellPages.add(page);
        }
        return spellPages;
    }

    private List<WandType<?>> getOrderedWands() {
        return WandcraftRegistries.WAND_TYPES.stream()
                .sorted(Comparator.comparing(WandType::getKey))
                .toList();
    }

    private static @NotNull List<SpellDefinition> getOrderedSpells() {
        return WandcraftRegistries.SPELLS.stream()
                .sorted(Comparator.comparing(SpellDefinition::getKey))
                .toList();
    }

    private static @NotNull TextComponent getChapterLink(@Positive int page, String title) {
        return Component.text(page + ". " + title).clickEvent(ClickEvent.changePage(page)).appendNewline();
    }

    public void toItem(ItemStack item) {
        double consumeTicks = 2 * Ticks.TICKS_PER_SECOND;
        SpellDefinition currentSpell = getCurrentSpell();
        if (currentSpell != null) {
            consumeTicks = currentSpell.getAttribute(CastableSpell.COOLDOWN);
        }

        consumeTicks = Math.min(consumeTicks, 5 * Ticks.TICKS_PER_SECOND);

        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                .animation(ItemUseAnimation.BLOCK)
                .hasConsumeParticles(false)
                .consumeSeconds((float) (consumeTicks / Ticks.TICKS_PER_SECOND))
                .sound(Key.key("not.a.real.sound"))
        );

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

        return new LinkedList<>(
                WbsStrings.wrapText("Sneak + hold Right Click to Cast", 140).stream()
                        .map(Component::text)
                        .map(component -> component.color(NamedTextColor.AQUA))
                        .toList()
        );
    }

    public void tryCasting(Player player, ItemStack item) {
        if (CastingManager.isCasting(player)) {
            WbsWandcraft.getInstance().sendActionBar("Already casting!", player);
            return;
        }

        SpellDefinition definition = getCurrentSpell();
        if (definition != null) {
            if (!knowsSpell(player, definition)) {
                Component errorMessage = getErrorMessage(definition);

                player.sendActionBar(errorMessage);

                player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1, 2);
                return;
            }

            CastingQueue castingQueue = new CastingQueue(new SpellInstance(definition), null);
            castingQueue.startCasting(player);
            player.setCooldown(item, Ticks.TICKS_PER_SECOND);
        }
    }

    public static @NotNull Component getErrorMessage(SpellDefinition definition) {
        Component errorMessage = definition.displayName().font(Key.key("illageralt")).color(NamedTextColor.DARK_PURPLE);

        errorMessage = errorMessage.append(Component.text("???").color(NamedTextColor.DARK_RED).font(Key.key("default")));
        return errorMessage;
    }

    public void currentPage(int newPage) {
        this.currentPage = Math.clamp(newPage, 0, 1 + getOrderedSpells().size() + getOrderedWands().size());
    }

    @Nullable
    public SpellDefinition getCurrentSpell() {
        List<SpellDefinition> allDefinitions = getOrderedSpells();
        int wandPages = WandcraftRegistries.WAND_TYPES.keys().size();
        if (currentPage >= wandPages + 1) {
            return allDefinitions.get(currentPage - wandPages - 1);
        }

        return null;
    }

    @Nullable
    public WandType<?> getCurrentWandType() {
        List<WandType<?>> allWands = getOrderedWands();
        if (currentPage >= 1 && currentPage < allWands.size() + 1) {
            return allWands.get(currentPage - 1);
        }

        return null;
    }
}
