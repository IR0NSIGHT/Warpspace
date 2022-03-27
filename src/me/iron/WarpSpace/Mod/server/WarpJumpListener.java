package me.iron.WarpSpace.Mod.server;

/*
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 15:32
 */

import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import api.listener.Listener;
import api.listener.events.entity.ShipJumpEngageEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;

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
                        Vector3i posNow = event.getOriginalSectorPos();
                        //check if ship is in warp or not, check if ship is allowed to perform the jump
                        if (WarpManager.isInWarp(event.getController()) && WarpJumpManager.isAllowedDropJump(event.getController())) { //is in warpspace, get realspace pos
                            WarpJumpManager.invokeDrop(4,event.getController(),true, false);
                        } else if (!WarpManager.isInWarp(event.getController())&& WarpJumpManager.isAllowedEntry(event.getController())) { //is in realspace, get warppos
                            WarpJumpManager.invokeEntry(4,event.getController(),false); //TODO set sector on jump, not before
                        }
                    }
                }, WarpMain.instance);
    }
}
