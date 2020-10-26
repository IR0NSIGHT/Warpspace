/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 21.10.2020
 * TIME: 20:13
 */

import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

import java.util.Map;

/**
 * a loop that runs every couple seconds checking for unregistered segmentcontrollers in the warp, that somehow avoided getting caught by the eventhandler
 */
public class CheeseCatchLoop {
    public static void createLoop() {
        DebugFile.log("starting anti cheesy loop");
        new StarRunnable() {
            @Override
            public void run() {
                //get every segmentcontoller of the gameserver
                Map<String, SegmentController> scList = GameServer.getServerState().getSegmentControllersByName();
                for (SegmentController sc: scList.values()) {
                //    DebugFile.log("checking: " + sc.getName() + " at y = " + + sc.getSector(new Vector3i()).y);
                    if (sc.getSector(new Vector3i()).y >= 150 && !warpLoop.warpEntities.contains(sc)) { //entity is in warp but not registered by the system to be in warp
                        //register entity as in warp
                        DebugFile.log(" cheeser found: " + sc.getName());
                        DebugFile.log("------------ logging warpEn. list:");
                        for (SegmentController s: warpLoop.warpEntities) {
                            DebugFile.log(s.getName());
                        }
                        DebugFile.log("---------done");

                        warpLoop.startLoop(sc);
                        navigationHelper.handlePilots(sc,true); //TODO make navHelper autodetect if into or from warp
                 //       ModPlayground.broadcastMessage("caught cheesy ship in warp");
                    } else {
                //        DebugFile.log("not in warp or registered:");
                    }
                    if (sc.getSector(new Vector3i()).y < 150 && warpLoop.warpEntities.contains(sc)) {
                        //is in warpentity list but not in warp
                        //remove from list
                        warpLoop.warpEntities.remove(sc);
                        DebugFile.log("removed " + sc.getName() + "from list bc not anymore in warp: " + sc.getSector(new Vector3i()).toString());
                    }
                }
                //check y position
                //check for all warp sc if they have a warploop
            }
        }.runTimer(25);
    }
}
