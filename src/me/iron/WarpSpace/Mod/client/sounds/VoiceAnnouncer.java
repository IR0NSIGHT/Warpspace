package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;

public class VoiceAnnouncer extends WarpProcessListener {
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
                    if (WarpProcess.IS_IN_WARP.isTrue() && c.getPreviousValue()>=10&&c.getCurrentValue()<10) {
                        announce(WarpSounds.SoundEntry.voice_disengage);
                        announce(WarpSounds.SoundEntry.voice_warp);
                    }
            }

    }

    private void announce(WarpSounds.SoundEntry e) {
        WarpSounds.instance.queueSound(e ,VoiceAnnouncer.queueID);

    }
}
