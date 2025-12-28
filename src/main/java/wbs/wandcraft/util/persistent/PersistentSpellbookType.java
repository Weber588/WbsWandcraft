package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spellbook.Spellbook;

import java.util.List;

public class PersistentSpellbookType implements PersistentDataType<PersistentDataContainer, Spellbook> {
    public static final @NotNull NamespacedKey KNOWN_SPELLS = WbsWandcraft.getKey("known_spells");
    private static final @NotNull NamespacedKey CURRENT_PAGE = WbsWandcraft.getKey("current_page");

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<Spellbook> getComplexType() {
        return Spellbook.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull Spellbook spellbook, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();

        container.set(KNOWN_SPELLS, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.NAMESPACED_KEY), spellbook.knownSpells().stream().map(SpellDefinition::getKey).toList());
        container.set(CURRENT_PAGE, PersistentDataType.INTEGER, spellbook.currentPage());

        return container;
    }

    @Override
    public @NotNull Spellbook fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
        Integer currentPage = container.get(CURRENT_PAGE, PersistentDataType.INTEGER);

        List<SpellDefinition> knownSpells = Spellbook.getKnownSpells(container);

        return new Spellbook(knownSpells, currentPage == null ? 0 : currentPage);
    }
}
