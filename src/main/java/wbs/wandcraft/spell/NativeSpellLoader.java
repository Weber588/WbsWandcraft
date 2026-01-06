package wbs.wandcraft.spell;

import wbs.wandcraft.spell.definitions.*;

import java.util.Set;

public class NativeSpellLoader extends SpellLoader {
    public Set<Loader<?>> getEntries() {
        return Set.of(
                new Loader<>(FireballSpell.class, FireballSpell::new),
                new Loader<>(LeapSpell.class, LeapSpell::new),
                new Loader<>(BlinkSpell.class, BlinkSpell::new),
                new Loader<>(PrismaticRaySpell.class, PrismaticRaySpell::new),
                new Loader<>(EldritchBlastSpell.class, EldritchBlastSpell::new),
                new Loader<>(WarpSpell.class, WarpSpell::new),
                new Loader<>(RecallSpell.class, RecallSpell::new),
                new Loader<>(AntiMagicShellSpell.class, AntiMagicShellSpell::new),
                new Loader<>(ArcaneSurgeSpell.class, ArcaneSurgeSpell::new),
                new Loader<>(ConflagrationSpell.class, ConflagrationSpell::new),
                new Loader<>(WindWalkSpell.class, WindWalkSpell::new),
                new Loader<>(CrowsCallSpell.class, CrowsCallSpell::new),
                new Loader<>(StunSpell.class, StunSpell::new),
                new Loader<>(FireBreathSpell.class, FireBreathSpell::new),
                new Loader<>(ArcaneSparkSpell.class, ArcaneSparkSpell::new),
                new Loader<>(PlanarBindingSpell.class, PlanarBindingSpell::new),
                new Loader<>(DiscoverItemSpell.class, DiscoverItemSpell::new),
                new Loader<>(ChainLightningSpell.class, ChainLightningSpell::new),
                new Loader<>(CharmSpell.class, CharmSpell::new),
                new Loader<>(DisplaceSpell.class, DisplaceSpell::new),
                new Loader<>(ArcaneBurstSpell.class, ArcaneBurstSpell::new),
                new Loader<>(ShieldSpell.class, ShieldSpell::new),
                new Loader<>(CarveSpell.class, CarveSpell::new),
                new Loader<>(PolymorphSpell.class, PolymorphSpell::new),
                new Loader<>(HoldSpell.class, HoldSpell::new)
        );
    }
}
