package wbs.wandcraft.events.objects;

import wbs.wandcraft.events.SpellEvent;
import wbs.wandcraft.objects.generics.MagicObject;

public abstract class MagicObjectEvent extends SpellEvent {
    protected final MagicObject magicObject;

    public MagicObjectEvent(MagicObject magicObject) {
        super(magicObject.caster, magicObject.castContext);
        this.magicObject = magicObject;
    }

    public MagicObject getMagicObject() {
        return magicObject;
    }
}
