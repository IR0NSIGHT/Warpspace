package me.iron.WarpSpace.Mod.Interdiction;

import api.DebugFile;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.HUD.client.WarpProcessController;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.network.RegisteredClientOnServer;

import java.util.ArrayList;
import java.util.List;

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
                        e.printStackTrace();
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
                    List<String> inhInfo = new ArrayList<>();

                    if (WarpManager.IsInWarp(sc)) {
                        warpPos = sc.getSector(new Vector3i());
                        rspPos = WarpManager.GetRealSpacePos(warpPos);
                    } else {
                        rspPos = sc.getSector(new Vector3i());
                        warpPos = WarpManager.GetWarpSpacePos(rspPos);
                    }
                    if (rspPos == null || warpPos == null) {
                     //   DebugFile.log("rsp or warppos is null");
                        continue;
                    }
                    Vector3i interdictingRSP = WarpJumpManager.getInterdictingSector(sc,rspPos);
                    Vector3i interdictingWARP = WarpJumpManager.getInterdictingSector(sc,warpPos);
                    Vector3i interdictingSector = null;
                    ManagedSegmentController badBoy = null;
                    if (null != interdictingRSP) { //rsp is interdicted
                        rspinterdicted = 1;
                        interdictingSector = interdictingRSP;
                    }

                    if (null != interdictingWARP) { //warp is interdicted
                        warpinterdicted = 1;
                        interdictingSector = interdictingWARP;
                    }

                    if (warpinterdicted == 1 || rspinterdicted == 1) {
                        badBoy = WarpJumpManager.getStrongestActiveInterdictorIn(interdictingSector);
                        if (badBoy == null) {
                            DebugFile.log("ship is interdicted but could not find interdictor in: " + interdictingSector.toString());
                            return;
                        }
                        inhInfo.add(badBoy.getSegmentController().getName());
                        if (badBoy.getSegmentController().getFaction() != null) {
                            inhInfo.add(badBoy.getSegmentController().getFaction().getName());
                        } else {
                            inhInfo.add("no faction");
                        }
                        inhInfo.add(interdictingSector.toString());
                    }

                    WarpJumpManager.SendPlayerWarpSituation(player, WarpProcessController.WarpProcess.WARPSECTORBLOCKED,warpinterdicted,inhInfo);
                    WarpJumpManager.SendPlayerWarpSituation(player, WarpProcessController.WarpProcess.RSPSECTORBLOCKED,rspinterdicted,inhInfo);
                }
            }
        }.runTimer(WarpMain.instance,12);

    }
}
