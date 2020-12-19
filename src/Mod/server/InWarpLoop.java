package Mod.server;

import Mod.HUD.client.HUD_core;
import Mod.WarpEntityManager;
import Mod.WarpJumpManager;
import Mod.WarpMain;
import Mod.WarpManager;
import api.DebugFile;
import api.utils.StarRunnable;
import api.utils.sound.AudioUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.util.ArrayList;
import java.util.List;

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
            int countdown = 15;
            long lastWarning = 11;
            @Override
            public void run() {
                if (GameServerState.isFlagShutdown() || GameServerState.isShutdown() || ship == null || !ship.existsInState()) {
                    cancel();
                }
                try {
                    if (!WarpEntityManager.isWarpEntity(ship) || GameServerState.isShutdown()) {
                        //left warp
                        DebugFile.log("InWarpLoop was terminated bc server is shutdown or ship no longer in warp.");
                        cancel();
                    }
                    if (ship.getSpeedCurrent() < WarpManager.minimumSpeed) {
                        //TODO get better way of turning on and off drop warning
                        //WarpJumpManager.SendPlayerWarpSituation(ship, HUD_core.WarpSituation.JUMPDROP);
                        //ship is to slow, dropping out of warp!
                        if (countdown < lastWarning) {
                            DebugFile.log("warning player for countdown " + countdown + " and lastwarning " + lastWarning);
                            ship.sendControllingPlayersServerMessage(Lng.astr("you are to slow! dropping out of warp in " + countdown), ServerMessage.MESSAGE_TYPE_WARNING);
                            //send warning sound to players in ship
                            if (ship.isConrolledByActivePlayer()) {
                                for (PlayerState player: ((PlayerControllable)ship).getAttachedPlayers()) {
                                    AudioUtils.serverPlaySound("0022_gameplay - low fuel warning constant beeps (loop)", 1F,1F,player);
                                }
                            }
                            lastWarning = countdown;
                        }
                        countdown --; //runs once a second
                    } else {
                        //WarpJumpManager.SendPlayerWarpSituation(ship, HUD_core.WarpSituation.TRAVEL);

                        if (countdown < 10) {
                            countdown ++;
                            lastWarning = countdown;
                        }
                    }
                    if (countdown > 10) { //essentially caps the countdown to 10, while allowing a start buffer of extra seconds
                        countdown --;
                    }
                    if (countdown <= 0) {
                        //drop entity out of warp.
                        WarpJumpManager.invokeDrop(1,ship,WarpManager.GetRealSpacePos(ship.getSector(new Vector3i())),false, false);
                        cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    DebugFile.log(e.toString());
                }
            }
        }.runTimer(WarpMain.instance, 25);
    }
    private void handleCountdown() {
    }
}
