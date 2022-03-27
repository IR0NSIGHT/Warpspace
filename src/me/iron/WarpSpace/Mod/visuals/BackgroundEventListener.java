package me.iron.WarpSpace.Mod.visuals;

import api.listener.Listener;
import api.listener.events.world.ProceduralSkyboxColorEvent;
import api.mod.StarLoader;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import org.schema.game.client.data.GameClientState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.03.2021
 * TIME: 13:55
 */
public class BackgroundEventListener {
    public static void AddListener() {
        StarLoader.registerListener(ProceduralSkyboxColorEvent.class, new Listener<ProceduralSkyboxColorEvent>() {
            @Override
            public void onEvent(ProceduralSkyboxColorEvent event) {
                if (event.isServer()) {
                    return;
                }
                if (WarpManager.isInWarp(GameClientState.instance.getPlayer().getCurrentSector())) {
                    event.setColor1(1,0,1,1.5f);
                    event.setColor2(4,4,0,4);   //idk why but starmade allows for numbers >1 as color input. vanilla is usually <2 tho.
                }
            }
        }, WarpMain.instance);

    }
}
