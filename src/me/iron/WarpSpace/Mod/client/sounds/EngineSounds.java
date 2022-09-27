package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;
import me.iron.WarpSpace.Mod.server.ConfigManager;

public class EngineSounds extends WarpProcessListener {
    public static String queueId = "Engine";
    @Override
    public void onValueChange(WarpProcess c) {
        super.onValueChange(c);
        switch (c) {
            case JUMPDROP: //immediate
                if (!c.wasTrue() && c.isTrue()) { //TODO doenst always catch event
                    queue(WarpSounds.SoundEntry.warp_boom); //assert immediate
                }
                break;
            case JUMPEXIT: //fallthrough
            case JUMPENTRY:
                if (!c.wasTrue() && c.isTrue()) {
                    //TODO clear queue
                    queue(WarpSounds.SoundEntry.drive_charge_up);
                    queue(WarpSounds.SoundEntry.warp_boom);
                }
                break;
        }

    }
    public void queue(WarpSounds.SoundEntry e) {
        if (!ConfigManager.ConfigEntry.sfx_effects_enable.isTrue())
            return;

        WarpSounds.instance.queueSound(new WarpSounds.SoundInstance(e, ConfigManager.ConfigEntry.sfx_effects_add_db.getValue(), 1),queueId);
    }
}
