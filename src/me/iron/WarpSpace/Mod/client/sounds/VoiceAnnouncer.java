package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;

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

        //inhibition
        WarpProcess.WARPSECTORBLOCKED.addListener(this);
        WarpProcess.RSPSECTORBLOCKED.addListener(this);

        //waypoint
        WarpProcess.DISTANCE_TO_WP.addListener(this);
    }

    public static String queueID = "VoiceAnnouncer";
    @Override
    public void onValueChange(WarpProcess c) {
        super.onValueChange(c);
            switch (c) {
                case JUMPEXIT: //fallthrough
                case JUMPENTRY:
                    if (c.isTrue()) {
                        announce(WarpSounds.SoundEntry.voice_engage);
                        announce(WarpSounds.SoundEntry.voice_warpdrive);
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
                case RSPSECTORBLOCKED:
                case WARPSECTORBLOCKED:
                    inhibitionEvent(); break;
                case DISTANCE_TO_WP:
                    waypointEvent(c); break;

            }
    }

    private void waypointEvent(WarpProcess wp) {
        if (WarpProcess.IS_IN_WARP.isTrue() && wp.getPreviousValue()>0 && wp.getCurrentValue()==0) {
            //reached waypoint in warp
            announce(WarpSounds.SoundEntry.voice_droppoint);
            announce(WarpSounds.SoundEntry.voice_reached);
        }
    }

    private void inhibitionEvent() {
        if ((!WarpProcess.WARPSECTORBLOCKED.wasTrue() && WarpProcess.WARPSECTORBLOCKED.isTrue()) ||
            (!WarpProcess.RSPSECTORBLOCKED.wasTrue() && WarpProcess.RSPSECTORBLOCKED.isTrue())) {
            //RSP or Warp inhibited.
            announce(WarpSounds.SoundEntry.voice_inhibitor);
            announce(WarpSounds.SoundEntry.voice_detected);
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
        if (!ConfigManager.ConfigEntry.sfx_voice_enable.isTrue())
            return;

        WarpSounds.instance.queueSound(new WarpSounds.SoundInstance(e, ConfigManager.ConfigEntry.sfx_voice_add_db.getValue(), 1),queueID);

    }
}
