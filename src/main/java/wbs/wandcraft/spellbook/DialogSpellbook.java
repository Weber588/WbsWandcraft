package wbs.wandcraft.spellbook;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.LinkedList;
import java.util.List;

public class DialogSpellbook {

    public static Dialog getDialog(PersistentDataContainer container) {
        List<SpellDefinition> knownSpells = Spellbook.getKnownSpells(container);

        List<ActionButton> buttons = new LinkedList<>();
        for (SpellDefinition spell : knownSpells) {
            DialogAction.CustomClickAction callback = DialogAction.customClick(
                    (view, audience) -> {
                        if (audience instanceof Player player) {
                            new SpellInstance(spell).cast(player, null, null, () -> {
                                WbsWandcraft.getInstance()
                                        .buildMessage(Component.text("Cast ").append(spell.displayName()))
                                        .send(player);
                            });
                        }
                    },
                    ClickCallback.Options.builder()
                            .uses(1) // Set the number of uses for this callback. Defaults to 1
                            .lifetime(ClickCallback.DEFAULT_LIFETIME) // Set the lifetime of the callback. Defaults to 12 hours
                            .build()
            );

            buttons.add(
                    ActionButton.create(
                            spell.displayName(),
                            Component.text("Click to cast ").append(spell.displayName()),
                            100,
                            callback
                    ));
        }

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Cast a spell!"))
                        .build()
                )
                .type(DialogType.multiAction(buttons, null, 2))
        );
    }

}
