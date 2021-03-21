package me.iron.WarpSpace.Mod.server;

import me.iron.WarpSpace.Mod.WarpJumpEvent;
import me.iron.WarpSpace.Mod.WarpMain;
import api.listener.Listener;
import api.mod.StarLoader;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.12.2020
 * TIME: 13:50
 * this class contains the eventhandler for handling of mod-warpjumps.
 * this includes the switching of the navigation marker.
 */
public class WarpJumpEventHandler { //TODO is this obsolete?
    public static void createServerListener() {
        StarLoader.registerListener(WarpJumpEvent.class,new Listener<WarpJumpEvent>() {
            @Override
            public void onEvent(WarpJumpEvent event) {
                //change players nav tool
                boolean toWarp = (event.getType().equals(WarpJumpEvent.WarpJumpType.ENTRY));
                NavHelper.handlePilots(event.getShip(), toWarp);
            }
        }, WarpMain.instance);
    }
}
