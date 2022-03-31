package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;

public class VoiceAnnouncer extends WarpProcessListener {
    @Override
    public void onValueChange(WarpProcess c) {
        System.out.println("warp announcer="+c);
        super.onValueChange(c);
            switch (c) {
                case JUMPEXIT:
                case JUMPDROP:
                case JUMPPULL:
                case JUMPENTRY: {
                    if (c.isTrue())
                        WarpSounds.instance.queueSound(WarpSounds.Sound.warping);
                    break;
                }
                case WARP_STABILITY:
                    if (WarpProcess.IS_IN_WARP.isTrue() && c.getPreviousValue()>=80&&c.getCurrentValue()<80)
                        WarpSounds.instance.queueSound(WarpSounds.Sound.dropping);
            }

    }
}