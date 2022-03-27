package me.iron.WarpSpace.Mod.server;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 15:17
 */

import me.iron.WarpSpace.Mod.WarpEntityManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import api.common.GameServer;
import api.utils.StarRunnable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.GameServerState;

import java.util.Map;

/**
 * a loop that runs regularly and checks all loaded ships if they are in warp or not. passes the ships to the warpshipmanager
 */
public class WarpCheckLoop {
    /**
     * loop that creates a starrunnable that checks the loaded segmentcontrollers every x seconds
     * @param frequency amount of ticks between iterations. 25 ticks = 1 second
     */
    public static void loop(long frequency) {
        //make a timed loop
        new StarRunnable() {
            @Override
            public void run() {
                //kill loop if server is shut down.
                if (GameServerState.isShutdown() || GameServerState.isFlagShutdown()) {
                    cancel();
                }

                //check for every segmentcontroller:
                Map<String, SegmentController> scList = GameServer.getServerState().getSegmentControllersByName();
                for (SegmentController sc: scList.values()) {
                    if (WarpManager.isInWarp(sc) && !WarpEntityManager.isWarpEntity(sc)) {
                        //is in warp and not registered
                        WarpEntityManager.DeclareWarpEntity(sc);
                    }
                    if (!WarpManager.isInWarp(sc) && WarpEntityManager.isWarpEntity(sc)) {
                        //is not in warp but registered
                        WarpEntityManager.RemoveWarpEntity(sc);
                    }
                }
            }
        }.runTimer(WarpMain.instance,frequency);


    }

}


