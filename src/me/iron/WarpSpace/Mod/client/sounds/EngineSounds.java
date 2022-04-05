package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;

public class EngineSounds extends WarpProcessListener {
    public static String queueId = "Engine";
    @Override
    public void onValueChange(WarpProcess c) {
        super.onValueChange(c);
        switch (c) {
            case JUMPDROP: //immediate
                if (!c.wasTrue() && c.isTrue()) { //TODO doenst always catch event
                    WarpSounds.instance.playSound(WarpSounds.SoundEntry.warp_boom);
                }
                break;
            case JUMPEXIT: //fallthrough
            case JUMPENTRY:
                if (!c.wasTrue() && c.isTrue()) {
                    //TODO clear queue
                    WarpSounds.instance.queueSound(WarpSounds.SoundEntry.drive_charge_up,queueId);
                    WarpSounds.instance.queueSound(WarpSounds.SoundEntry.warp_boom,queueId);
                }
                break;
        }

    }
}
