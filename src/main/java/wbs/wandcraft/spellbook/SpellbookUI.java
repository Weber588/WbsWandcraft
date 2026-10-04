package wbs.wandcraft.spellbook;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.Positive;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.commands.CommandInfoSpell;
import wbs.wandcraft.commands.CommandInfoSpellAspect;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.dynamic.SpellAspect;
import wbs.wandcraft.util.MenuUtils;
import wbs.wandcraft.wand.types.WandType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@NullMarked
public class SpellbookUI {
    private static final Map<String, Integer> CHAPTERS = new HashMap<>();
    private static final Map<String, Integer> CHAPTER_LENGTHS = new HashMap<>();
    public static final String CHAPTER_CONTENTS = "Contents";
    public static final String CHAPTER_WANDS = "Wands";
    public static final String CHAPTER_SPELL_ASPECTS = "Spell Aspects";
    public static final String CHAPTER_CANONICAL_SPELLS = "Canonical Spells";

    public static Map<String, Integer> getChapters() {
        if (CHAPTERS.isEmpty()) {
            int page = 0;

            CHAPTERS.put(CHAPTER_CONTENTS, page);
            page += 1;
            CHAPTER_LENGTHS.put("Contents", 0);

            int count = addChapter(CHAPTER_WANDS, page, WandcraftRegistries.WAND_TYPES);
            page += count + 1;

            count = addChapter(CHAPTER_SPELL_ASPECTS, page, WandcraftRegistries.SPELL_ASPECTS);
            page += count + 1;

            count = addChapter(CHAPTER_CANONICAL_SPELLS, page, WandcraftRegistries.SPELLS);
            page += count + 1;
        }

        return CHAPTERS;
    }

    private static int addChapter(String chapterName, int page, WbsRegistry<?> registered) {
        CHAPTERS.put(chapterName, page);
        int count = registered.values().size();
        CHAPTER_LENGTHS.put(chapterName, count);
        return count;
    }

    public static int getBookLength() {
        return CHAPTER_LENGTHS.values().stream().mapToInt(i -> i).sum() + CHAPTERS.size();
    }

    public static Book getBook(Player player) {
        List<WandType<?>> wandDefinitions = WandcraftRegistries.WAND_TYPES.ordered();
        List<SpellAspect> aspects = WandcraftRegistries.SPELL_ASPECTS.ordered();
        List<SpellDefinition> spellDefinitions = WandcraftRegistries.SPELLS.ordered();

        Map<String, Integer> chapters = getChapters();

        Component contentsPage = Component.empty();

        contentsPage = contentsPage.append(Component.text("Contents").decorate(TextDecoration.BOLD));
        contentsPage = contentsPage.append(MenuUtils.LINE_BREAK);

        contentsPage = contentsPage.append(getChapterLink(chapters.get("Contents"), "Contents"));
        contentsPage = contentsPage.append(getChapterLink(chapters.get("Wands"), "Wands"));
        contentsPage = contentsPage.append(getChapterLink(chapters.get("Spell Aspects"), "Spell Aspects"));
        contentsPage = contentsPage.append(getChapterLink(chapters.get("Canonical Spells"), "Canonical Spells"));

        Component wandsChapterPage = buildChapterPage(
                chapters,
                "Wands",
                Component.text("Types of wands that can be crafted at an artificing table")
        );
        Component spellAspectsChapterPage = buildChapterPage(
                chapters,
                "Spell Aspects",
                Component.text("Types/forms of craftable spells")
        );
        Component canonicalSpellsChapterPage = buildChapterPage(
                chapters,
                "Canonical Spells",
                Component.text("Pre-made spells with custom effects")
        );

        // TODO: Add chapter start pages? And chapter skip buttons at top of each page???
        List<Component> wandPages = getWandPages(wandDefinitions, chapters.get(CHAPTER_WANDS));
        List<Component> aspectPages = getAspectPages(player, aspects, chapters.get(CHAPTER_SPELL_ASPECTS));
        List<Component> spellPages = getSpellPages(player, spellDefinitions, chapters.get(CHAPTER_CANONICAL_SPELLS));

        List<Component> pages = new LinkedList<>();
        pages.add(contentsPage);
        pages.add(wandsChapterPage);
        pages.addAll(wandPages);
        pages.add(spellAspectsChapterPage);
        pages.addAll(aspectPages);
        pages.add(canonicalSpellsChapterPage);
        pages.addAll(spellPages);

        return Book.book(Component.text("Spellbook"), player.name(), pages);
    }

    private static Component buildChapterPage(Map<String, Integer> chapters, String chapterName, Component description) {
        // TODO: Center chapter name
        Component page = Component.empty().append(
                Component.text(chapterName).style(MenuUtils.DEFAULT_TITLE_STYLE.decorate(TextDecoration.BOLD))
        );

        page = page.appendNewline()
                .append(
                        //    Component.text("============================")
                        Component.text("╔════ ▣◎▣ ════╗").style(MenuUtils.EXTRAS_STYLE)
                )
                .appendNewline()
                .append(description.style(MenuUtils.DESCRIPTION_STYLE))
                .appendNewline()
                .append(Component.text("╚════ ▣◎▣ ════╝").style(MenuUtils.EXTRAS_STYLE));

        return buildHeader(chapters.get(chapterName)).appendNewline().append(page);
    }

    private static Component buildHeader(int currentPage) {
        int prevChapter = getStartOfChapter(currentPage);

        if (prevChapter == currentPage) {
            prevChapter = getPrevChapterStart(currentPage);
        }

        String prevChapterName = getChapterName(prevChapter);
        Component header = Component.empty().append(Component.text("⮜ ")
                .clickEvent(ClickEvent.changePage(prevChapter + 1))
                .hoverEvent(HoverEvent.showText(Component.text(prevChapterName).style(MenuUtils.DEFAULT_TITLE_STYLE)))
        );

        header = header.append(
                Component.text("                      ")
                        .decorate(TextDecoration.STRIKETHROUGH)
        );

        int nextChapter = getNextChapter(currentPage);

        if (nextChapter < Integer.MAX_VALUE) {
            String nextChapterName = getChapterName(nextChapter);
            header = header.append(Component.text(" ⮞")
                    .clickEvent(ClickEvent.changePage(nextChapter + 1))
                    .hoverEvent(HoverEvent.showText(Component.text(nextChapterName).style(MenuUtils.DEFAULT_TITLE_STYLE)))
            );
        }

        return header.style(MenuUtils.EXTRAS_STYLE);
    }

    public static int getPageInChapter(int atPage) {
        return Math.max(0, atPage - getStartOfChapter(atPage));
    }

    public static int getStartOfChapter(int atPage) {
        for (Map.Entry<String, Integer> entry : getChapters().entrySet()) {
            Integer page = entry.getValue();
            if (page == atPage) {
                return atPage;
            }
        }

        return getPrevChapterStart(atPage);
    }

    public static String getChapterName(int atPage) {
        int currentChapter = getStartOfChapter(atPage);

        for (Map.Entry<String, Integer> entry : getChapters().entrySet()) {
            String name = entry.getKey();
            Integer page = entry.getValue();
            if (page == currentChapter) {
                return name;
            }
        }

        throw new IllegalStateException("getChapterName(getCurrentChapter(%d)) returned null!".formatted(atPage));
    }

    public static int getNextChapter(int currentPage) {
        int nextChapter = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> chapter : getChapters().entrySet().stream().sorted(Map.Entry.comparingByValue()).toList()) {
            int toPage = currentPage - chapter.getValue();
            if (toPage < 0) {
                // chapter page is after this one
                if (Math.abs(toPage) < nextChapter - currentPage) {
                    nextChapter = chapter.getValue();
                }
            }
        }
        return nextChapter;
    }

    public static int getPrevChapterStart(int thisPage) {
        int prevChapter = 0;
        for (Map.Entry<String, Integer> chapter : getChapters().entrySet().stream().sorted(Map.Entry.comparingByValue()).toList()) {
            int toPage = thisPage - chapter.getValue();
            if (toPage > 0) {
                // chapter page is before this one
                // Pick the closest chapter page that comes before this one
                if (toPage < thisPage - prevChapter) {
                    prevChapter = chapter.getValue();
                }
            }
        }
        return prevChapter;
    }

    private static List<Component> getWandPages(List<WandType<?>> wandDefinitions, int chapterStart) {
        List<Component> wandPages = new LinkedList<>();
        int i = 0;
        for (WandType<?> type : wandDefinitions) {
            i++;
            Component page = Component.empty()
                    .append(buildHeader(chapterStart + i))
                    .appendNewline()
                    .append(type.getItemName().style(MenuUtils.DEFAULT_TITLE_STYLE))
                    .append(Component.text(" (" + type.getEchoShardCost() + ")").style(MenuUtils.COST_STYLE))
                    .append(MenuUtils.LINE_BREAK)
                    .append(type.getDescription().style(MenuUtils.DESCRIPTION_STYLE).decorate(TextDecoration.ITALIC));

            wandPages.add(page);
        }
        return wandPages;
    }

    private static List<Component> getAspectPages(Player player, List<SpellAspect> allAspects, int chapterStart) {
        List<Component> spellPages = new LinkedList<>();
        int i = 0;
        for (SpellAspect aspect : allAspects) {
            i++;
            // TODO: Add aspect learning???
            // boolean isKnown = knowsSpell(player, aspect);
            spellPages.add(buildHeader(chapterStart + i).appendNewline().append(CommandInfoSpellAspect.getSpellAspectPage(aspect)));
        }
        return spellPages;
    }

    private static List<Component> getSpellPages(Player player, List<SpellDefinition> allDefinitions, int chapterStart) {
        List<Component> spellPages = new LinkedList<>();
        int i = 0;
        for (SpellDefinition definition : allDefinitions) {
            i++;
            boolean isKnown = Spellbook.knowsSpell(player, definition);
            spellPages.add(buildHeader(chapterStart + i).appendNewline().append(CommandInfoSpell.getSpellPage(definition, isKnown, true)));
        }
        return spellPages;
    }

    private static TextComponent getChapterLink(@Positive int page, String title) {
        return Component.text((page + 1) + ". " + title).clickEvent(ClickEvent.changePage(page + 1)).appendNewline();
    }
}
