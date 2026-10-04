package wbs.wandcraft.spellbook;

import io.papermc.paper.advancement.AdvancementDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.utils.util.pluginhooks.hooks.PacketEventsWrapper;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.dynamic.SpellAspect;
import wbs.wandcraft.util.ItemDecorator;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.util.MenuUtils;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.types.WandType;

import java.util.*;

@NullMarked
public class Spellbook implements ItemDecorator {
    private static final NamespacedKey SPELL_BOOK = WbsWandcraft.getKey("spellbook");
    public static final NamespacedKey KNOWN_SPELLS = WbsWandcraft.getKey("known_spells");

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
                PacketEventsWrapper.get().ifPresentOrElse(
                        pe -> pe.sendToast(icon, message, AdvancementDisplay.Frame.CHALLENGE, player),
                        () -> WbsWandcraft.getInstance().buildMessage(message).send(player)
                );
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
        PacketEventsWrapper.get().ifPresentOrElse(
                pe -> pe.sendToast(icon, message, AdvancementDisplay.Frame.GOAL, player),
                () -> WbsWandcraft.getInstance().buildMessage(message).send(player)
        );
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
        Book book = SpellbookUI.getBook(player);

        ItemStack item = MenuUtils.getBookItem(book);
        toItem(item);

        MenuUtils.showBook(player, item, currentPage);

        return this;
    }

    @Override
    public void toItem(ItemStack item) {
        double consumeTicks = 2 * Ticks.TICKS_PER_SECOND;
        SpellDefinition currentSpell = getCurrentSpell();

        CustomModelData.Builder modelData = CustomModelData.customModelData()
                .addString(WbsWandcraft.getKey("spellbook").asString());
        if (currentSpell != null) {
            consumeTicks = currentSpell.getAttribute(CastableSpell.COOLDOWN);

            List<SpellType> types = currentSpell.getTypes();

            modelData.addColor(currentSpell.getPrimarySpellType().color());

            if (types.size() > 1) {
                modelData.addColor(types.get(1).color());
            } else {
                modelData.addColor(currentSpell.getPrimarySpellType().color());
            }
        }
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, modelData);

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
            container.set(ItemUtils.WANDCRAFT_ITEM_NAME, PersistentDataType.STRING, "Spellbook");
            container.set(ItemUtils.WANDCRAFT_ITEM_KEY, WbsPersistentDataType.NAMESPACED_KEY, SPELL_BOOK);
            ItemDecorator.decorate(this, meta);
        });
    }

    @Override
    public @Nullable Component getItemName() {
        return Component.text("Spellbook");
    }

    @Override
    public List<Component> getLore() {
        List<Component> components = new LinkedList<>(
                WbsStrings.wrapText("Sneak + hold Right Click to Cast", 140).stream()
                        .map(Component::text)
                        .map(component -> component.color(NamedTextColor.AQUA))
                        .toList()
        );

        components.add(Component.empty());

        Component pageName = getCurrentPageName();
        components.add(Component.text("Page " + (currentPage + 1) + ": ").style(MenuUtils.DESCRIPTION_STYLE).append(pageName));

        return components;
    }

    private Component getCurrentPageName() {
        String currentChapter = getCurrentChapter();

        int index = SpellbookUI.getPageInChapter(currentPage) - 1;
        switch (currentChapter) {
            case SpellbookUI.CHAPTER_CONTENTS -> {
                return Component.text("Contents");
            }
            case SpellbookUI.CHAPTER_WANDS -> {
                WandType<?> currentWandType = getCurrentWandType();
                if (currentWandType != null) {
                    return currentWandType.getItemName();
                }
                return Component.text("Wands");
            }
            case SpellbookUI.CHAPTER_SPELL_ASPECTS -> {
                if (index >= 0) {
                    SpellAspect spellAspect = WandcraftRegistries.SPELL_ASPECTS.ordered().get(index);

                    return spellAspect.displayName();
                }

                return Component.text("Spell Aspects");
            }
            case SpellbookUI.CHAPTER_CANONICAL_SPELLS -> {
                SpellDefinition currentSpell = getCurrentSpell();
                if (currentSpell != null) {
                    return currentSpell.displayName();
                }
                return Component.text("Spells");
            }
        }

        throw new IllegalStateException("Page name not found! Page %d, Chapter %s, Chapter Page %d"
                .formatted(
                        currentPage,
                        currentChapter,
                        index
                )
        );
    }

    @Nullable
    public SpellDefinition getCurrentSpell() {
        int index = SpellbookUI.getPageInChapter(currentPage) - 1;
        if (index >= 0) {
            return WandcraftRegistries.SPELLS.ordered().get(index);
        }
        return null;
    }

    @Nullable
    public WandType<?> getCurrentWandType() {
        int index = SpellbookUI.getPageInChapter(currentPage) - 1;
        if (index >= 0) {
            return WandcraftRegistries.WAND_TYPES.ordered().get(index);
        }
        return null;
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

                Particle.FLASH.builder()
                        .location(player.getEyeLocation().add(WbsEntityUtil.getFacingVector(player, 0.5)))
                        .data(definition.getPrimarySpellType().color())
                        .spawn();
                player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1, 2);
                return;
            }

            CastingQueue castingQueue = new CastingQueue(definition.newInstance(), null);
            castingQueue.startCasting(player);
            player.setCooldown(item, Ticks.TICKS_PER_SECOND);
        }
    }

    public static Component getErrorMessage(SpellDefinition definition) {
        Component errorMessage = definition.displayName().font(Key.key("illageralt")).color(NamedTextColor.DARK_PURPLE);

        errorMessage = errorMessage.append(Component.text("???").color(NamedTextColor.DARK_RED).font(Key.key("default")));
        return errorMessage;
    }

    public void currentPage(int newPage) {
        this.currentPage = Math.clamp(newPage, 0, SpellbookUI.getBookLength());
    }

    public String getCurrentChapter() {
        return SpellbookUI.getChapterName(currentPage);
    }
}
