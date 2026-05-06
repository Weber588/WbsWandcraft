package wbs.wandcraft.spell;

import wbs.wandcraft.spell.definitions.*;

import java.util.List;

public class NativeSpellLoader extends SpellLoader {
    public static final List<Loader> LOADERS = List.of(
            new ConcreteLoader<>(FireballSpell::new),
            new ConcreteLoader<>(LeapSpell::new),
            new ConcreteLoader<>(BlinkSpell::new),
            new ConcreteLoader<>(PrismaticRaySpell::new),
            new ConcreteLoader<>(EldritchBlastSpell::new),
            new ConcreteLoader<>(WarpSpell::new),
            new ConcreteLoader<>(RecallSpell::new),
            new ConcreteLoader<>(AntiMagicShellSpell::new),
            new ConcreteLoader<>(ArcaneSurgeSpell::new),
            new ConcreteLoader<>(ConflagrationSpell::new),
            new ConcreteLoader<>(WindWalkSpell::new),
            new ConcreteLoader<>(CrowsCallSpell::new),
            new ConcreteLoader<>(StunSpell::new),
            new ConcreteLoader<>(FireBreathSpell::new),
            new ConcreteLoader<>(ArcaneSparkSpell::new),
            new ConcreteLoader<>(PlanarBindingSpell::new),
            new ConcreteLoader<>(DiscoverItemSpell::new),
            new ConcreteLoader<>(ChainLightningSpell::new),
            new ConcreteLoader<>(CharmSpell::new),
            new ConcreteLoader<>(DisplaceSpell::new),
            new ConcreteLoader<>(ArcaneBurstSpell::new),
            new ConcreteLoader<>(ShieldSpell::new),
            new ConcreteLoader<>(CarveSpell::new),
            new PluginDependentLoader("wbs.wandcraft.spell.definitions.PolymorphSpell", "LibsDisguises"),
            new ConcreteLoader<>(HoldSpell::new),
            new PluginDependentLoader("wbs.wandcraft.spell.definitions.MassIllusionSpell", "LibsDisguises"),
            new ConcreteLoader<>(HeatRaySpell::new),
            new ConcreteLoader<>(InterruptSpell::new),
            new PluginDependentLoader("wbs.wandcraft.spell.definitions.HallucinationSpell", "LibsDisguises"),
            new ConcreteLoader<>(NegateMagicSpell::new),
            new ConcreteLoader<>(TurnUndeadSpell::new),
            new ConcreteLoader<>(GrowSpell::new),
            new ConcreteLoader<>(HealSpell::new),
            new ConcreteLoader<>(HealingCircleSpell::new),
            new ConcreteLoader<>(EmergencyTeleportSpell::new),
            new ConcreteLoader<>(MageLightSpell::new),
            new ConcreteLoader<>(AmorphousEarthSpell::new),
            new ConcreteLoader<>(BlackHoleSpell::new),
            new ConcreteLoader<>(AcidBombSpell::new),
            new ConcreteLoader<>(VoidStepSpell::new),
            new ConcreteLoader<>(DeathWalkSpell::new)
    );

    public List<Loader> getEntries() {
        return LOADERS;
    }
}
