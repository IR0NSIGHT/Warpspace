package me.iron.WarpSpace.Mod.server;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.WarpEntityManager;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import api.DebugFile;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.GameServerState;


/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.10.2020
 * TIME: 01:00
 */
public class InWarpLoop {
    //TODO add a central method that handles an entity being put into warp so it can be called by event or cheeseloop
    public static void startLoop(final SegmentController ship) {
        new StarRunnable() {
            long lastRun;
            final float timeOut = 0.1f;
            final int countdownMax = (int) ConfigManager.ConfigEntry.seconds_until_speeddrop.getValue();
            float countdown = countdownMax; //initial start value

            @Override
            public void run() {
                if (GameServerState.isFlagShutdown() || GameServerState.isShutdown() || ship == null || !ship.existsInState()) {
                    cancel();
                }

                //precise timer
                if (System.currentTimeMillis()<lastRun + timeOut*1000)
                    return;
                lastRun = System.currentTimeMillis();
                try {
                    if (ship == null)
                        return;

                    if (!WarpEntityManager.isWarpEntity(ship) || GameServerState.isShutdown()) {
                        //left warp
                    ;
                        cancel();
                    }
                    //update value for synching
                    WarpProcess.setProcess(ship,WarpProcess.WARP_STABILITY,(int)((100*countdown)/countdownMax));

                    if (ship.getSpeedCurrent() < WarpManager.minimumSpeed) {
                        //ship is to slow, dropping out of warp!
                        countdown -= timeOut; //runs once a second
                    } else {
                        if (countdown < countdownMax) {
                            countdown += 2*timeOut;
                        }
                    }

                    if (countdown > countdownMax) { //essentially caps the countdown to max_val, while allowing a start buffer of extra seconds
                        countdown -= timeOut;
                    }
                    if (countdown <= 0) {
                        //drop entity out of warp.
                        WarpJumpManager.invokeDrop(0,ship,false, false);
                        cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    DebugFile.err("Warpspace failed to run in-warp-loop on entity " + ship.getName() +" UID=" + ship.getUniqueIdentifier() + "err:" + e.toString());
                }
            }
        }.runTimer(WarpMain.instance, 1);
    }
}
