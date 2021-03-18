package Mod.server;

import Mod.WarpEntityManager;
import Mod.WarpJumpManager;
import Mod.WarpMain;
import Mod.WarpManager;
import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.entity.ShipJumpEngageEvent;
import api.listener.events.systems.ThrustCalculateEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.data.ManagedSegmentController;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.12.2020
 * TIME: 00:33
 * intercept the thrust calculation event provided by vanilla code and overwrite it with a different value for warpships
 */
public class ThrustEventhandler {
    public static void createListener() {
        DebugFile.log("Creating thrust calculation listener");
        StarLoader.registerListener(ThrustCalculateEvent.class, new Listener<ThrustCalculateEvent>() {
            @Override
            public void onEvent(ThrustCalculateEvent e) {
                float vanillaT = e.getCalculatedThrust();
                SegmentController ship = e.getThrusterElementManager().getContainer().getSegmentController();
                boolean inWarp = WarpEntityManager.isWarpEntity(ship);
                //ModPlayground.broadcastMessage("calcualted thrust: " + vanillaT);
                //ModPlayground.broadcastMessage("is warp entity: " + inWarp);
                e.getThrusterElementManager().getContainer();
                e.setThrust(5);


                //get jumpaddon
                JumpAddOn warpdrive;
                if(ship instanceof ManagedSegmentController<?>) {
                    warpdrive =((Ship)ship).getManagerContainer().getJumpAddOn();
                    warpdrive.getDistance();
                } else {
                    DebugFile.log("entity " + ship.getName() + "tried jumping but is no managed SC.");
                }
            }
        }, WarpMain.instance);
    }
}
