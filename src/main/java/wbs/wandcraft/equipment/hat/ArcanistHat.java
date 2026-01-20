package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.events.CalculateMaxManaEvent;

import java.util.List;

public class ArcanistHat extends MagicHat {
    private static final double MANA_INCREASE = 1.5;

    public ArcanistHat() {
        super("arcanist", HatModel.ARCANIST);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x62315B);
    }

    @Override
    public List<String> getEffectsLore() {
        return List.of(
                "+150% Max Mana"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), CalculateMaxManaEvent.class, this::onCalculateMaxMana);
    }

    private void onCalculateMaxMana(CalculateMaxManaEvent event) {
        ifEquipped(event.getPlayer(), () -> {
                event.setMaxMana((int) (event.getMaxMana() * (1 + MANA_INCREASE)));
        });
    }
}
