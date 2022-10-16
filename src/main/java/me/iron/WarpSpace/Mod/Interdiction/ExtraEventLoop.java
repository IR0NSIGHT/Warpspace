package me.iron.WarpSpace.Mod.Interdiction;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.network.RegisteredClientOnServer;

import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.client.WarpProcess;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.02.2021
 * TIME: 17:39
 */
public class ExtraEventLoop {
    /**
     * loop that creates events by bruteforce checking.
     * handles: interdiction, beacons
     * am aware that such stuff should be handeled on the client but am to lazy to change current event system thats focuessed on server -> client
     */
    public static void CreateServerLoop() { //TODO fixme
        new StarRunnable() {
            @Override
            public void run() {
                //get all clients
                for ( RegisteredClientOnServer client: GameServerState.instance.getClients().values() ) {
                    PlayerState player;
                    try {
                        player = GameServerState.instance.getPlayerFromName(client.getPlayerName());
                    } catch (PlayerNotFountException e) {
                        continue;
                    }


                }
            }
        }.runTimer(WarpMain.instance,12);
    }

    public static void updatePlayer(PlayerState player) {
        //player in ship? false for astronaut
        SimpleTransformableSendableObject ship = player.getFirstControlledTransformableWOExc();
        if (!(ship instanceof Ship)) { //ship null or not Segmentcontroller (= astronaut)
            return;
        }
        SegmentController sc = (SegmentController) ship;

        //get relevant positions to check
        Vector3i playerPos =  sc.getSector(new Vector3i()),
                rspPos = WarpManager.getRealSpacePos(playerPos),
                warpPos = WarpManager.getWarpSpacePos(playerPos);

        //interdiction
        updateInterdiction(warpPos,rspPos,player,sc);

        //beacons
        updateBeacons(warpPos, player);
    }

    private static void updateBeacons(Vector3i warpPos, PlayerState p) {
        WarpProcess.setProcess(p,WarpProcess.DROPPOINTSHIFTED,WarpJumpManager.isDroppointShifted(warpPos)?1:0);
    }

    private static void updateInterdiction(Vector3i warpPos, Vector3i rspPos, PlayerState playerState, SegmentController ship) {
        boolean warpInterdicted = WarpJumpManager.isInterdicted(ship,warpPos);
        boolean rspInterdicted = WarpJumpManager.isInterdicted(ship,rspPos);
        WarpProcess.setProcess(playerState,WarpProcess.WARPSECTORBLOCKED,warpInterdicted?1:0);
        WarpProcess.setProcess(playerState,WarpProcess.RSPSECTORBLOCKED,rspInterdicted?1:0);
    }
}
