package Mod.server;

import Mod.WarpJumpEvent;
import Mod.WarpJumpManager;
import Mod.WarpMain;
import Mod.WarpManager;
import api.DebugFile;
import api.listener.Listener;
import api.listener.events.entity.ShipJumpEngageEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.12.2020
 * TIME: 13:50
 * this class contains the eventhandler for handling of mod-warpjumps.
 * this includes the switching of the navigation marker.
 */
public class WarpJumpEventHandler {
    public static void createServerListener() {
        DebugFile.log("Creating WarpJump listener on client");
        StarLoader.registerListener(WarpJumpEvent.class,new Listener<WarpJumpEvent>() {
            @Override
            public void onEvent(WarpJumpEvent event) {
                //FIXME remove debug message
                DebugFile.log("handling warp event for: " + event.getShip().getName());

                //change players nav tool
                boolean toWarp = (event.getType().equals(WarpJumpEvent.WarpJumpType.ENTRY));
                NavHelper.handlePilots(event.getShip(), toWarp);
            }
        }, WarpMain.instance);
    }
}
