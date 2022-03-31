package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;

public class VoiceAnnouncer extends WarpProcessListener {
    @Override
    public void onValueChange(WarpProcess c) {
        super.onValueChange(c);
        if (c.getCurrentValue()>0)
            switch (c) {
                case JUMPEXIT:
                case JUMPDROP:
                case JUMPPULL:
                case JUMPENTRY:
                    WarpSounds.instance.queueSound(WarpSounds.Sound.warping);
            }
    }
}
