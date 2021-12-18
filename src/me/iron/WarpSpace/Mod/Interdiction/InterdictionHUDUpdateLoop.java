package me.iron.WarpSpace.Mod.Interdiction;

import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.client.WarpProcessController;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.network.RegisteredClientOnServer;

import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.02.2021
 * TIME: 17:39
 */
public class InterdictionHUDUpdateLoop {
    /**
     * loop that checks interdiction for all connected clients and sends updates to them for HUD.
     * will check if rsp is interdicted and if warp is interdicted. always sends both info. runs once a second for every player.
     */
    public static void CreateServerLoop() {
        //DebugFile.log("starting loop that updates clients on interdiction",WarpMain.instance);
        new StarRunnable() {
            @Override
            public void run() {
                //get all clients
                for ( RegisteredClientOnServer client: GameServerState.instance.getClients().values() ) {
                    PlayerState player = null;
                    try {
                        player = GameServerState.instance.getPlayerFromName(client.getPlayerName());
                    } catch (PlayerNotFountException e) {
                    //    e.printStackTrace();
                    //    DebugFile.logError(e, WarpMain.instance);
                        continue;
                    }

                    //player in ship? false for astronaut
                    SimpleTransformableSendableObject ship = player.getFirstControlledTransformableWOExc();
                    if (!(ship instanceof SegmentController)) { //ship null or not Segmentcontroller (= astronaut)
                        continue;
                    }
                    SegmentController sc = (SegmentController) ship;
                    int rspinterdicted = 0;
                    int warpinterdicted = 0;

                    //get relevant positions to check
                    Vector3i rspPos = null;
                    Vector3i warpPos = null;
                    if (WarpManager.IsInWarp(sc)) {
                        warpPos = sc.getSector(new Vector3i());
                        rspPos = WarpManager.getRealSpacePos(warpPos);
                    } else {
                        rspPos = sc.getSector(new Vector3i());
                        warpPos = WarpManager.getWarpSpacePos(rspPos);
                    }
                    if (rspPos == null || warpPos == null) {
                     //   DebugFile.log("rsp or warppos is null");
                        continue;
                    }
                    if (WarpJumpManager.isInterdicted(sc,rspPos)) {
                        rspinterdicted = 1;
                    }

                    if (WarpJumpManager.isInterdicted(sc, warpPos)) {
                        warpinterdicted = 1;
                    }
                   // DebugFile.log("updating player on inhibition: " + player.getName() + warpPos.toString() + "warp " + warpinterdicted +rspPos.toString() + " rsp: " + rspinterdicted);
                    WarpJumpManager.SendPlayerWarpSituation(player, WarpProcessController.WarpProcess.WARPSECTORBLOCKED,warpinterdicted, new ArrayList<String>());
                    WarpJumpManager.SendPlayerWarpSituation(player, WarpProcessController.WarpProcess.RSPSECTORBLOCKED,rspinterdicted, new ArrayList<String>());
                }
            }
        }.runTimer(WarpMain.instance,12);

    }
}
