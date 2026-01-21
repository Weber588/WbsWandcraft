package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsMath;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.util.ItemUtils;

import java.util.LinkedList;
import java.util.List;

public abstract class MagicHat implements MagicEquipmentType {
    private final String hatName;
    private final NamespacedKey key;
    private final HatModel model;

    public MagicHat(String hatName, HatModel model) {
        key = WbsWandcraft.getKey("hat_" + hatName);
        this.hatName = hatName;
        this.model = model;
    }

    @Override
    public final Component getItemName() {
        return Component.text(getName()).color(getNameColour());
    }

    protected @NotNull String getName() {
        return WbsStrings.capitalizeAll(hatName) + "'s Hat";
    }

    @Override
    public void registerEvents() {
        MagicEquipmentType.super.registerEvents();

        WbsEventUtils.register(WbsWandcraft.getInstance(), CreatureSpawnEvent.class, this::onMobSpawn);
    }

    // TODO: Remove testing method and properly implement equipment generation system
    private void onMobSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();
        if (spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        if (this instanceof WitchHat) {
            if (event.getEntity() instanceof Witch witch) {
                EntityEquipment equipment = witch.getEquipment();

                equipment.setHelmet(ItemUtils.buildEquipment(this));
                equipment.setHelmetDropChance(100);
            }
        } else if (event.getEntity() instanceof Evoker evoker) {
            if (WbsMath.chance(10)) {
                EntityEquipment equipment = evoker.getEquipment();

                equipment.setHelmet(ItemUtils.buildEquipment(this));

                switch (spawnReason) {
                    case RAID -> {
                        equipment.setHelmetDropChance(20);
                    }
                    case PATROL, NATURAL -> {
                        equipment.setHelmetDropChance(100);
                    }
                    default -> {
                        equipment.setHelmetDropChance(50);
                    }
                }
            }
        }
    }

    protected abstract @NotNull TextColor getNameColour();

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public HatModel getModel() {
        return model;
    }

    @Override
    public final @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>();

        getEffectsLore().stream()
                .map(effectLine -> WbsStrings.wrapText(effectLine, 140))
                .forEach(lines -> {
                    for (String line : lines) {
                        lore.add(Component.text(line).color(TextColor.color(0x487ba3)));
                    }
                });

        String credit = getCredit();

        if (credit != null) {
            lore.add(Component.empty());
            lore.add(Component.text("Artist: ").color(Spellbook.DESCRIPTION_COLOR).append(Component.text(credit).decorate(TextDecoration.ITALIC)));
        }

        return lore;
    }

    protected @Nullable String getCredit() {
        return model.credit();
    }

    public abstract List<String> getEffectsLore();

    // TODO: Add passive particle effects
}
