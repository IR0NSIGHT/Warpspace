package me.iron.WarpSpace.Mod.server;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.WarpEntityManager;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import api.DebugFile;
import api.utils.StarRunnable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.GameServerState;

import java.util.ArrayList;

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
            long timeOut = 200;
            int countdown = 15;
            final int countdownMax = 30;
            @Override
            public void run() {
                if (GameServerState.isFlagShutdown() || GameServerState.isShutdown() || ship == null || !ship.existsInState()) {
                    cancel();
                }

                //precise timer
                if (System.currentTimeMillis()<lastRun + timeOut)
                    return;
                lastRun = System.currentTimeMillis();

                try {
                    if (ship == null)
                        return;

                    if (!WarpEntityManager.isWarpEntity(ship) || GameServerState.isShutdown()) {
                        //left warp
                    //    DebugFile.log("InWarpLoop was terminated bc server is shutdown or ship no longer in warp.");
                        cancel();
                    }
                    //update value for synching
                    WarpProcess.setProcess(ship,WarpProcess.WARP_STABILITY,(int)(100*((float)countdown/countdownMax)));

                    if (ship.getSpeedCurrent() < WarpManager.minimumSpeed) {
                        //ship is to slow, dropping out of warp!
                        countdown --; //runs once a second
                    } else {
                        //WarpProcess.setProcess(ship,WarpProcess.JUMPDROP,0);
                        if (countdown < countdownMax) {
                            countdown +=2;
                        }
                    }

                    if (countdown > countdownMax) { //essentially caps the countdown to max_val, while allowing a start buffer of extra seconds
                        countdown --;
                    }
                    if (countdown <= 0) {
                        //drop entity out of warp.
                        WarpJumpManager.invokeDrop(0,ship,false, false);
                        cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    DebugFile.log(e.toString());
                }
            }
        }.runTimer(WarpMain.instance, 1);
    }
}
