package me.iron.WarpSpace.Mod.server;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 15:17
 */

import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import me.iron.WarpSpace.Mod.TimedRunnable;
import me.iron.WarpSpace.Mod.WarpEntityManager;
import me.iron.WarpSpace.Mod.WarpMain;

/**
 * a loop that runs regularly and checks all loaded ships if they are in warp or not. passes the ships to the warpshipmanager
 */
public class WarpCheckLoop {
    /**
     * creates a timed runnable that checks the loaded segmentcontrollers every x seconds
     */
    public static void loop() {
        //make a timed loop
        new TimedRunnable(5000,WarpMain.instance, -1) {
            @Override
            public void onRun() {
                //check for every updatable object (astronauts, hsips, asteroids etc
                for (Sendable sc: GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
                    if (sc instanceof SimpleTransformableSendableObject) {
                        SimpleTransformableSendableObject obj = (SimpleTransformableSendableObject)sc;
                        if (!WarpEntityManager.isWarpEntity(obj)) {
                            WarpEntityManager.DeclareWarpEntity(obj);
                        }
                    }
                }
            }
        };


    }

}
