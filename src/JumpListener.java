import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.entity.ShipJumpEngageEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.10.2020
 * TIME: 19:13
 */

/**
 *  core class that handles jump events and teleports to and from the warp
 */
    //TODO make travel speed in warp depend on FTL chamber level
    //TODO add ways to pull ships out of warpspace
    //TODO make ships drop out of warp when FTL drive is damaged
    //TODO no shields in warp?
public class JumpListener {
    public static int scale = 10; //scale warpspace distance to realspace distance
    public static int offset = 150; //offset in sectors
    public static void createListener() {
        DebugFile.log("Creating jump listener");
        StarLoader.registerListener(ShipJumpEngageEvent.class, new Listener<ShipJumpEngageEvent>() {
            @Override
            public void onEvent(ShipJumpEngageEvent event) {
                ModPlayground.broadcastMessage("ship jumping, abort");

                event.setCanceled(true); //stop jump
                Vector3i posNow = event.getOriginalSectorPos();
                DebugFile.log("jump detected at y:" + posNow.y);
                Vector3i newPos;
                String displayMessage;
                if (posNow.y >= 150) {
                    //is in warpspace, get realspace pos
                    ModPlayground.broadcastMessage("in warp");
                    displayMessage = " out of warp to position ";
                    DebugFile.log("warp");
                    newPos = GetRealSpacePos(posNow);
                } else {
                    //is in realspace, get warppos
                    ModPlayground.broadcastMessage("in realspace");
                    displayMessage = " into warp to position ";
                    DebugFile.log("realspace");
                    newPos = GetWarpSpacePos(posNow);
                }
                SegmentController ship = event.getController();
                //get jumpaddon
                JumpAddOn warpdrive;
                if(ship instanceof ManagedSegmentController<?>) {
                    warpdrive =((Ship)ship).getManagerContainer().getJumpAddOn();
                } else {
                    DebugFile.log("entity " + ship.getName() + "tried jumping but is no managed SC.");
                    return;
                }

                //TODO check for interdiction -> not possible bc vanilla method is private
                //jump

                SectorSwitch toWarp = GameServer.getServerState().getController().queueSectorSwitch(ship,newPos,SectorSwitch.TRANS_JUMP,false,true,true);
                if (toWarp != null) {
                    toWarp.delay = System.currentTimeMillis() + 4000;
                    toWarp.jumpSpawnPos = new Vector3f(event.getController().getWorldTransform().origin);
                    toWarp.executionGraphicsEffect = (byte) 2;
                    toWarp.keepJumpBasisWithJumpPos = true;
                    ship.sendControllingPlayersServerMessage(Lng.astr("Jumping " + displayMessage + " " + newPos.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
                    //empty jumpmodule after jump
                    warpdrive.removeCharge();
                    warpdrive.setCharge(0);
                    warpdrive.sendChargeUpdate();
                } else {
                    ship.sendControllingPlayersServerMessage(Lng.astr("Jump failed, warpdrive needs to cooldown."), ServerMessage.MESSAGE_TYPE_INFO);
                    DebugFile.log("jumping into warp failed");
                }
                //TODO why are jumps sometimes not executed?
            }
        });
    }

    /**
     * Calculate the Warpspace position from a realworld position
     * @param RealSpacePos sector in realspace
     * @return correlating sector in warpspace
     */
    public static Vector3i GetWarpSpacePos(Vector3i RealSpacePos) {
        Vector3i warpPos;
        Vector3f realPosF = RealSpacePos.toVector3f();
        ModPlayground.broadcastMessage("real space pos: " + realPosF.toString());
        realPosF.x = Math.round(realPosF.x / scale);
        realPosF.y = Math.round(realPosF.y / scale);
        realPosF.z = Math.round(realPosF.z / scale);
   //     realPosF.scale((1/scale)); //scale to warpspace -> 10,10,10 becomes 1,1,1
        DebugFile.log("realpos scaled down by " + scale + " : " + realPosF.toString());
        realPosF.y += offset * 2; //offset sectors to up (y axis)
        warpPos = new Vector3i(realPosF.x,realPosF.y,realPosF.z);
        DebugFile.log("warppos: " + warpPos.toString());
        ModPlayground.broadcastMessage("warppos: " + warpPos);
        return warpPos;
    }
    /**
     * Calculate the realspace position from a warpspace position
     * @param WarpSpacePos sector in warpspace
     * @return correlating sector in realspace
     */
    public static Vector3i GetRealSpacePos(Vector3i WarpSpacePos) {
        Vector3i warpPos;
        Vector3f realPosF = WarpSpacePos.toVector3f();
        ModPlayground.broadcastMessage("warp space pos: " + realPosF.toString());
        realPosF.y -= offset * 2; //offset sectors to up (y axis)
        DebugFile.log("warppos minus offset: " + realPosF.toString());
        realPosF.x = Math.round(realPosF.x * scale);
        realPosF.y = Math.round(realPosF.y * scale);
        realPosF.z = Math.round(realPosF.z * scale);
        //     realPosF.scale((1/scale)); //scale to warpspace -> 10,10,10 becomes 1,1,1
        DebugFile.log("realpos scaled down by " + scale + " : " + realPosF.toString());

        warpPos = new Vector3i(realPosF.x,realPosF.y,realPosF.z);

        ModPlayground.broadcastMessage("warppos: " + warpPos);
        return warpPos;
    }
}
