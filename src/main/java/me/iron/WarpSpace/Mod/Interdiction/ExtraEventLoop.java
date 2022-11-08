package me.iron.WarpSpace.Mod.Interdiction;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.client.WarpProcess;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.02.2021
 * TIME: 17:39
 */
public class ExtraEventLoop {


    public static void updatePlayer(PlayerState player) {
        //player in ship? false for astronaut
        SimpleTransformableSendableObject ship = player.getFirstControlledTransformableWOExc();
        if (!(ship instanceof Ship)) { //ship null or not Segmentcontroller (= astronaut)
            return;
        }
        SegmentController sc = (SegmentController) ship;

        //get relevant positions to check
        Vector3i playerPos =  sc.getSector(new Vector3i()),
                rspPos = WarpManager.getInstance().getRealSpaceBySector(playerPos),
                warpPos = WarpManager.getInstance().getWarpSpaceSector(playerPos);

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
