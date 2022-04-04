package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;

public class VoiceAnnouncer extends WarpProcessListener {
    public VoiceAnnouncer() {
        WarpProcess.JUMPDROP.addListener(this);
        WarpProcess.JUMPENTRY.addListener(this);
        WarpProcess.JUMPEXIT.addListener(this);
        WarpProcess.JUMPPULL.addListener(this);
        WarpProcess.WARP_STABILITY.addListener(this);

        //beacon stuff
        WarpProcess.DROPPOINTSHIFTED.addListener(this);
        WarpProcess.IS_IN_WARP.addListener(this);
    }

    public static String queueID = "VoiceAnnouncer";
    @Override
    public void onValueChange(WarpProcess c) {
        //System.out.println("warp announcer="+c);
        super.onValueChange(c);
            switch (c) {
                case JUMPEXIT: //fallthrough
                case JUMPENTRY:
                    if (c.isTrue()) {
                        System.out.println("player warp sound with process"+ c);
                        announce(WarpSounds.SoundEntry.voice_warpdrive);
                        announce(WarpSounds.SoundEntry.voice_engaged);
                    }
                    break;

                case WARP_STABILITY:
                    if (!WarpProcess.JUMPEXIT.isTrue() && WarpProcess.IS_IN_WARP.isTrue() && c.getPreviousValue()>=50&&c.getCurrentValue()<50) {
                        announce(WarpSounds.SoundEntry.voice_warp);
                        announce(WarpSounds.SoundEntry.voice_stability);
                        announce(WarpSounds.SoundEntry.voice_critical);
                    }
                    break;
                case IS_IN_WARP: //fallthrough
                case DROPPOINTSHIFTED:
                    beaconEvent(WarpProcess.IS_IN_WARP,WarpProcess.DROPPOINTSHIFTED);
                    break;
            }
    }

    private void beaconEvent(WarpProcess inWarp, WarpProcess droppointShifted) {
        if ((inWarp.isTrue() && !inWarp.wasTrue() && droppointShifted.isTrue())||
            (inWarp.isTrue() && !droppointShifted.wasTrue() && droppointShifted.isTrue())) {
            //entered warp into shifted point OR beacon was activated in that system
            //no shift -> shift
            announce(WarpSounds.SoundEntry.voice_detected);
            announce(WarpSounds.SoundEntry.voice_activated); //use "active" instead of "activated"
            announce(WarpSounds.SoundEntry.voice_beacon);
        }
    }

    private void announce(WarpSounds.SoundEntry e) {
        WarpSounds.instance.queueSound(e ,VoiceAnnouncer.queueID);

    }
}
