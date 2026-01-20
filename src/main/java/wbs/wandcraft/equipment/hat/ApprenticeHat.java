package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.events.CalculateMaxManaEvent;
import wbs.wandcraft.events.SpellCastEvent;
import wbs.wandcraft.spellbook.Spellbook;

import java.util.List;

public class ApprenticeHat extends MagicHat {
    private static final double MANA_INCREASE = 0.5;

    public ApprenticeHat() {
        super("apprentice", HatModel.APPRENTICE);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x0c8d5b);
    }

    @Override
    public List<String> getEffectsLore() {
        return List.of(
                "+50% Max Mana",
                "Learn spells you see nearby players cast!"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), CalculateMaxManaEvent.class, this::onCalculateMaxMana);
        WbsEventUtils.register(WbsWandcraft.getInstance(), SpellCastEvent.class, this::onCastSpell);
    }

    private void onCastSpell(SpellCastEvent event) {
        Player caster = event.getCaster();

        for (Entity nearbyEntity : caster.getNearbyEntities(20, 20, 20)) {
            if (nearbyEntity instanceof Player player) {
                ifEquipped(player, () -> Spellbook.teachSpell(player, event.getContext().instance().getDefinition()));
            }
        }
    }

    private void onCalculateMaxMana(CalculateMaxManaEvent event) {
        ifEquipped(event.getPlayer(), () -> {
            event.setMaxMana((int) (event.getMaxMana() * (1 + MANA_INCREASE)));
        });
    }
}
