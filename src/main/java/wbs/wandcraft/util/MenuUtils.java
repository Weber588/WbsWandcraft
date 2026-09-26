package wbs.wandcraft.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.LecternView;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import wbs.utils.util.pluginhooks.hooks.PacketEventsWrapper;
import wbs.wandcraft.WbsWandcraft;

public class MenuUtils {
    public static final TextColor DESCRIPTION_COLOR = TextColor.color(0x6F47A3);
    public static final Style COST_STYLE = Style.style()
            .color(TextColor.color(0x006661))
            .decorate(TextDecoration.ITALIC)
            .build();
    public static final NamedTextColor DEFAULT_TITLE_COLOUR = NamedTextColor.GOLD;
    public static final TextColor EXTRAS_COLOUR = TextColor.color(0xe3a11c);
    public static final TextColor ACCENTS_STYLE = NamedTextColor.GOLD;

    public static final TextComponent LINE_BREAK = Component.newline()
            .append(Component.text("                            ")
                    .decorate(TextDecoration.STRIKETHROUGH)
                    .color(ACCENTS_STYLE)
            ).appendNewline();

    public static void showBook(Player player, Book book, int pageNum) {
        showBook(player, getBookItem(book), pageNum);
    }
    @SuppressWarnings("UnstableApiUsage")
    public static void showBook(Player player, ItemStack bookItem, int pageNum) {
        if (player.getGameMode() == GameMode.SURVIVAL) {
            PacketEventsWrapper.get().ifPresent(pe -> pe.sendGameModeChange(GameMode.ADVENTURE, player));
        }

        Location virtualLocation = getVirtualLecternLocation(player);

        LecternView lecternView = MenuType.LECTERN.builder()
                .checkReachable(false)
                .location(virtualLocation)
                .build(player);

        lecternView.setPage(pageNum);
        lecternView.setItem(0, bookItem);
        lecternView.open();

        removeLectern(virtualLocation, player);

        WbsWandcraft.getInstance().runAtEndOfTick(() -> {
            lecternView.setPage(pageNum);
            removeLectern(virtualLocation, player);
        });
    }

    public static @NonNull ItemStack getBookItem(Book book) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        item.setData(
                DataComponentTypes.WRITTEN_BOOK_CONTENT, toWrittenBookContent(book)
        );
        item.editPersistentDataContainer(container -> {
            container.set(ItemUtils.WANDCRAFT_ITEM_KEY, PersistentDataType.STRING, "info_book");
        });
        return item;
    }

    public static @NonNull WrittenBookContent toWrittenBookContent(Book book) {
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        return WrittenBookContent.writtenBookContent(serializer.serialize(book.title()), serializer.serialize(book.author()))
                .addPages(book.pages())
                .build();
    }

    private static void removeLectern(Location createLoc, Player player) {
        Block block = createLoc.getBlock();
        if (block.getType() == Material.LECTERN) {
            block.setType(Material.AIR);
        }
        player.sendBlockChange(createLoc, Material.AIR.createBlockData());
    }

    private static @NonNull Location getVirtualLecternLocation(Player player) {
        Location location = player.getLocation();
        location.setY(player.getWorld().getMaxHeight());

        while (location.getY() >= location.getWorld().getMinHeight() && !location.getBlock().isEmpty()) {
            location = location.add(0, -1, 0);
        }

        if (location.getY() < location.getWorld().getMinHeight()) {
            location = player.getLocation().add(0, 2, 0);
        }

        return location;
    }
}
