package me.iron.WarpSpace.Mod.client.sounds;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.client.WarpProcessListener;

public class EngineSounds extends WarpProcessListener {
    @Override
    public void onValueChange(WarpProcess c) {
        super.onValueChange(c);
        switch (c) {
            case JUMPEXIT: //fallthrough
            case JUMPENTRY:
                if (!c.wasTrue() && c.isTrue())
                    WarpSounds.instance.playSound(WarpSounds.Sound.jump_charge);
                break;
        }

    }
}
