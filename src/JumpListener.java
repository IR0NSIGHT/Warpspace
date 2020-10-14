import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.entity.ShipJumpEngageEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.controller.SectorSwitch;

import javax.vecmath.Vector3f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.10.2020
 * TIME: 19:13
 */
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
                if (posNow.y >= 150) {
                    //is in warpspace, get realspace pos
                    ModPlayground.broadcastMessage("in warp");
                    DebugFile.log("warp");
                    newPos = GetRealSpacePos(posNow);
                } else {
                    //is in realspace, get warppos
                    ModPlayground.broadcastMessage("in realspace");
                    DebugFile.log("realspace");
                    newPos = GetWarpSpacePos(posNow);
                }
                SectorSwitch toWarp = GameServer.getServerState().getController().queueSectorSwitch(event.getController(),newPos,SectorSwitch.TRANS_JUMP,false);
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
