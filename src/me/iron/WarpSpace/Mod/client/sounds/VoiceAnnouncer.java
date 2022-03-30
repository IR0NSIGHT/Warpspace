package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcessController;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;

public class VoiceAnnouncer extends WarpProcessListener {
    @Override
    public void onValueChange(WarpProcessController.WarpProcess c) {
        super.onValueChange(c);
        switch (c) {
            case JUMPEXIT:
            case JUMPDROP:
            case JUMPPULL:
            case JUMPENTRY:
                WarpSounds.instance.queueSound(WarpSounds.Sound.warping);
        }
    }
}
