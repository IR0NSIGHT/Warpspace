package me.iron.WarpSpace.Mod.server;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 15:17
 */

import me.iron.WarpSpace.Mod.TimedRunnable;
import me.iron.WarpSpace.Mod.WarpEntityManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

/**
 * a loop that runs regularly and checks all loaded ships if they are in warp or not. passes the ships to the warpshipmanager
 */
public class WarpCheckLoop {
    /**
     * creates a timed runnable that checks the loaded segmentcontrollers every x seconds
     */
    public static void loop() {
        //killswitch active? dont run loop.
        if (ConfigManager.ConfigEntry.killswitch_speedDrop.isTrue())
            return;
        //make a timed loop
        new TimedRunnable(5000, WarpMain.instance, -1) {
            @Override
            public void onRun() {
                //check for every updatable object (astronauts, hsips, asteroids etc
                for (Sendable sc : GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
                    if (sc instanceof SimpleTransformableSendableObject) {
                        SimpleTransformableSendableObject obj = (SimpleTransformableSendableObject) sc;

                        //buggy feature with killswitch. disables astronauts autodropping
                        if (ConfigManager.ConfigEntry.killswitch_astronautDrop.isTrue() && sc instanceof AbstractCharacter)
                            continue;

                        if (!WarpEntityManager.isWarpEntity(obj)) {
                            WarpEntityManager.DeclareWarpEntity(obj);
                        }
                    }
                }
            }
        };


    }

}
