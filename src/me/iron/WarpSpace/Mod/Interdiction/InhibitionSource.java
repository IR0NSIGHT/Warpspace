package me.iron.WarpSpace.Mod.Interdiction;

/**
 * wrapper class that gives info about the object causing inhibition.
 */
public class InhibitionSource {
    /**
     * the available types of inhibition sources
     */
    public static enum Sourcetype {
        STAR,
        BLACKHOLE,
        BOMB,
        STORM,
        MACHINE
    }

    /**
     * the effect the inhibiton has
     */
    public static enum Warpeffect {
        noentry,
        noleave,
        nodrop,
        pull
    }

    public Sourcetype getType() {
        return type;
    }

    public void setType(Sourcetype type) {
        this.type = type;
    }

    public Warpeffect getEffect() {
        return effect;
    }

    public void setEffect(Warpeffect effect) {
        this.effect = effect;
    }

    private Sourcetype type;
    private Warpeffect effect;
    public InhibitionSource(Sourcetype type, Warpeffect effect) {
        this.type = type;
        this.effect = effect;
        //add itself to managers list
        //make itself a persisten object
    }
}
