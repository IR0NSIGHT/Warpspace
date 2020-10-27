import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.utils.StarRunnable;
import api.utils.sound.AudioUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import javax.naming.LimitExceededException;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.10.2020
 * TIME: 01:00
 */
public class warpLoop {
    public static List<SegmentController> warpEntities = new ArrayList<>();
    //TODO add a central method that handles an entity being put into warp so it can be called by event or cheeseloop

    public static void startLoop(final SegmentController ship) {
        if (warpEntities.contains(ship)) {
            DebugFile.log("ship already is registered with warploop");
            return;
        } else {
            DebugFile.log("added ship to warpentities: " + ship.getName());
            warpEntities.add(ship);
        }
        new StarRunnable() {
            int countdown = 15;
            long lastWarning = 11;
            @Override
            public void run() {
                if (GameServerState.isFlagShutdown() || GameServerState.isShutdown() || ship == null || !ship.existsInState()) {
                    cancel();
                }
                try {
                //    ModPlayground.broadcastMessage("loop running");
                //    ModPlayground.broadcastMessage("ship speed: " + ship.getSpeedCurrent());
                //    ModPlayground.broadcastMessage("countdown: " + countdown);
                //     ModPlayground.broadcastMessage("lastWarning: " + lastWarning);
                //    WarpThrustManager.DebugThrust((Ship)ship);
                    if (ship.getSector(new Vector3i()).y < 150) {
                        //left warp
                    //    ModPlayground.broadcastMessage("ship left warp.");
                        cancel();
                    }
                    if (ship.getSpeedCurrent() < 50) {
                        //ship is to slow, dropping out of warp!
                        if (countdown < lastWarning) {
                            DebugFile.log("warning player for countdown " + countdown + " and lastwarning " + lastWarning);
                            ship.sendControllingPlayersServerMessage(Lng.astr("you are to slow! dropping out of warp in " + countdown), ServerMessage.MESSAGE_TYPE_WARNING);
                        //    ModPlayground.broadcastMessage("you are to slow! dropping out of warp in " + countdown);
                            //send warning sound to players in ship
                            if (ship.isConrolledByActivePlayer()) {
                                for (PlayerState player: ((PlayerControllable)ship).getAttachedPlayers()) {
                                    AudioUtils.serverPlaySound("0022_gameplay - low fuel warning constant beeps (loop)", 1F,1F,player);
                                }
                            //    ModPlayground.broadcastMessage("ship is controlled by player, ding dong");
                            }


                            lastWarning = countdown;
                        }
                        countdown --; //runs once a second
                    } else {
                        if (countdown < 10) {
                            countdown ++;
                            lastWarning = countdown;
                        }

                    }
                //    WarpThrustManager.LimitShipSpeed((Ship) ship,50); //TODO make thrust manipulation work
                    if (countdown > 10) { //essentially caps the countdown to 10, while allowing a start buffer of extra seconds
                        countdown --;

                    }
                    if (countdown <= 0) {
                        //drop entity out of warp.
                    //    ModPlayground.broadcastMessage("dropping, to slow for to long");
                        JumpListener.dropOutOfWarp(ship);
                        cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    DebugFile.log(e.toString());
                }

            }

            @Override
            public void cancel() {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                    DebugFile.log("could not remove entity from warpList: " + e.toString());
                }

                super.cancel();
            }
        }.runTimer(main.instance, 25);
    }
}
