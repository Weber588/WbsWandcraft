package wbs.wandcraft.spell.event;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.commands.brigadier.KeyedSuggestionProvider;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

@NullMarked
public class CastSpellEffect extends SpellEffectDefinition<Location> {
    private static final SpellAttribute<SpellInstance> SPELL = new SpellAttribute<>(
            "spell",
            AttributeDataType.SPELL,
            new SpellInstanceType(),
            new SpellInstance(WandcraftRegistries.SPELLS.stream().findAny().orElseThrow()),
            string -> {
                SpellDefinition definition = WandcraftRegistries.SPELLS.get(NamespacedKey.fromString(string, WbsWandcraft.getInstance()));
                if (definition == null) {
                    //noinspection DataFlowIssue
                    return null;
                }
                return new SpellInstance(definition);
            }).addRawSuggestions(WandcraftRegistries.SPELLS.stream().map(SpellDefinition::key).map(Key::asString).toList());

    public CastSpellEffect() {
        super(Location.class, "cast_spell");

        setAttribute(SPELL.defaultInstance());

        supportedEvents.add(SupportedEvent.LOCATION_RAYTRACE);
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<Location> instance, Location event) {
        SpellInstance triggeredCast = instance.getAttribute(SPELL);

        CastContext updatedContext = new CastContext(context.player(), triggeredCast, context.wand(), context.slot(), event, context, null);
        triggeredCast.cast(updatedContext);
    }

    @Override
    public Component toComponent(SpellEffectInstance<Location> instance) {
        return Component.text("Cast ").append(instance.getAttribute(SPELL).getDefinition().displayName());
    }

    private static class SpellInstanceType implements CustomArgumentType<SpellInstance, NamespacedKey>, KeyedSuggestionProvider<SpellDefinition> {

        @Override
        public SpellInstance parse(StringReader reader) throws CommandSyntaxException {
            NamespacedKey key = ArgumentTypes.namespacedKey().parse(reader);

            SpellDefinition def = WandcraftRegistries.SPELLS.get(key);

            if (def == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                        .create("Invalid spell key \"" + key.asString() + "\"");
            }

            return new SpellInstance(def);
        }

        @Override
        public ArgumentType<NamespacedKey> getNativeType() {
            return ArgumentTypes.namespacedKey();
        }

        @Override
        public Iterable<SpellDefinition> getSuggestions(CommandContext<CommandSourceStack> commandContext) {
            return WandcraftRegistries.SPELLS.values();
        }
    }
}
