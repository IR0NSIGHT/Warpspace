package Mod.HUD.client; /**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.10.2020
 * TIME: 17:36
 */

import Mod.PacketSCUpdateWarp;
import Mod.WarpManager;
import api.DebugFile;
import api.network.packets.PacketUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.objects.remote.RemoteVector3i;

import java.util.Iterator;
/**
 * adds elements that make it easier to tell what warp coord relates to what realspace coord.
 */
public class navigationHelper {
    /**
     * get the players waypoint to its equivalent in warpspace / realspace, so that navigating in warp is easier.
     * @param waypoint name of the player whos navigation waypoint should be changed
     * @param toWarp switch navigation waypoint towarp, false for to realspace
     * @return new waypoint in other dimension
     */
    public static Vector3i switchWaypoint(Vector3i waypoint, boolean toWarp) {
        //check if fromWarp or toWarp
        Vector3i currentWP = waypoint;
        Vector3i newWP;
        if (toWarp) {
            //calculate warp position from realworld pos
            newWP = WarpManager.GetWarpSpacePos(currentWP);
        } else {
            //calculate realspace position from warp pos
            newWP = WarpManager.GetRealSpacePos(currentWP);
        }
        //set new waypoint
        return newWP;
    }

    /**
     * change the players waypoint on their machine so that waypoints point to the correct position when chaning into warp.
     * @param ship segmentcontroller of players
     * @param toWarp boolean, true for going into warp, false for dropping out
     */
    public static void handlePilots(SegmentController ship, boolean toWarp) {
        try {
            //DebugFile.log("trying to handle pilots");
            if (ship.getType() != SimpleTransformableSendableObject.EntityType.SHIP) {
                //DebugFile.log("not handling pilots for type: " + ship.getType());
                return;
            }
            //get all players in ship
            Iterator i = ((PlayerControllable)ship).getAttachedPlayers().iterator();
            //foreach pilot do
            do {
                PlayerState player = (PlayerState)i.next();
                //DebugFile.log("changing waypoint for player " + player.getName());
                RemoteVector3i vec = player.getNetworkObject().waypoint;
                if (vec.equals(PlayerState.NO_WAYPOINT)) {
                    continue;
                }
                Vector3i newVec = switchWaypoint(vec.getVector(),toWarp);

                //DebugFile.log("old wp: " + vec.getVector().toString() + " new wp: " + newVec);
                //make packet with new wp, send it to players client
                PacketSCUpdateWarp packet = new PacketSCUpdateWarp(newVec);
                PacketUtil.sendPacket(player, packet);
            } while (i.hasNext());
            //DebugFile.log("handled all pilots");
        }  catch (Exception e) {
            e.printStackTrace();
            DebugFile.log(e.toString());
       //     ModPlayground.broadcastMessage("handlePilots failed");
        }
    }
}