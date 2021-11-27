package me.iron.WarpSpace.Mod.HUD.client;

import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import api.DebugFile;
import api.ModPlayground;
import api.common.GameClient;
import api.listener.Listener;
import api.listener.events.world.ProceduralSkyboxColorEvent;
import api.mod.StarLoader;

import javax.vecmath.Vector4f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.10.2020
 * TIME: 15:46
 */
public class SkyboxEventHandler { //TODO figure out how to draw a skybox
    public static void CreateListener() {
        //DebugFile.log("skybox eventhandler",WarpMain.instance);
        StarLoader.registerListener(ProceduralSkyboxColorEvent.class, new Listener<ProceduralSkyboxColorEvent>() {
            @Override
            public void onEvent(ProceduralSkyboxColorEvent e) {
        //        DebugFile.log("skybox created",WarpMain.instance);
                //ModPlayground.broadcastMessage("skybox created with color: " + e.getColor1() + e.getColor2());
                if (WarpManager.IsInWarp(GameClient.getClientPlayerState().getCurrentSector())) {
                    //change skybox to cool color to make warp more obvious
                    Vector4f c1 = new Vector4f(0.033f,1,0.941f,1); //neon bright blue
                    Vector4f c2 = new Vector4f(1,0,0.485f,1); //neon bright blue
                    e.setColor1(c1);
                    e.setColor2(c2);
                }
            }
        }, WarpMain.instance);
    }
}
