package me.iron.WarpSpace.Mod.server;

/*
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 15:32
 */

import api.listener.Listener;
import api.listener.events.entity.ShipJumpEngageEvent;
import api.mod.StarLoader;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;

/**
 * eventhandler class that will detect vanilla FTL jumps, abort them and instead issue a warp entry or warpdrop.
 */
public class WarpJumpListener {
    /**
     * creates the event handler for ShipJumpEngageEvent.
     * It will detect and cancel any jump a ship attempts.
     * instead another jump is queue to or from warp, depending on the ships position.
     */
    public static void createListener() {
        StarLoader.registerListener(ShipJumpEngageEvent.class,
                new Listener<ShipJumpEngageEvent>() {
                    @Override
                    public void onEvent(ShipJumpEngageEvent event) {
                        event.setCanceled(true); //stop jump
                        WarpJumpManager.invokeJumpdriveUsed(event.getController(), false);
                    }
                }, WarpMain.instance);
    }
}
