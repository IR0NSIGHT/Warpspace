import Mod.HUD.client.navigationHelper;
import Mod.WarpMain;
import api.DebugFile;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.entity.ShipJumpEngageEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
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

    /**
     * creates the event handler for ShipJumpEngageEvent.
     * It will detect and cancel any jump a ship attempts.
     * instead another jump is queue to or from warp, depending on the ships position.
     */
    public static void createListener() {
        DebugFile.log("Creating jump listener");
        StarLoader.registerListener(ShipJumpEngageEvent.class,
                new Listener<ShipJumpEngageEvent>() {
            @Override
            public void onEvent(ShipJumpEngageEvent event) {
                //ModPlayground.broadcastMessage("ship jumping, abort");

                event.setCanceled(true); //stop jump
                Vector3i posNow = event.getOriginalSectorPos();
                DebugFile.log("jump detected at y:" + posNow.y);
                Vector3i newPos;
                String displayMessage;
                boolean intoWarp;
                if (posNow.y >= 150) {
                    //is in warpspace, get realspace pos
                    intoWarp = false;
                    //ModPlayground.broadcastMessage("in warp");
                    displayMessage = " out of warp to position ";
                    DebugFile.log("warp");
                 //   newPos = GetRealSpacePos(posNow);
                } else {
                    intoWarp = true;
                    //is in realspace, get warppos
                    //ModPlayground.broadcastMessage("in realspace");
                    displayMessage = " into warp to position ";
                    DebugFile.log("realspace");
                 //   newPos = GetWarpSpacePos(posNow);
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
                if (!warpdrive.canExecute()) {
                    return;
                }
                //TODO check for interdiction -> not possible bc vanilla method is private
                //jump

                SectorSwitch sectorSwitch = GameServer.getServerState().getController().queueSectorSwitch(ship,newPos,SectorSwitch.TRANS_JUMP,false,true,true);
                if (sectorSwitch != null) {
                    sectorSwitch.delay = System.currentTimeMillis() + 4000;
                    sectorSwitch.jumpSpawnPos = new Vector3f(event.getController().getWorldTransform().origin);
                    sectorSwitch.executionGraphicsEffect = (byte) 2;
                    sectorSwitch.keepJumpBasisWithJumpPos = true;
                    ship.sendControllingPlayersServerMessage(Lng.astr("Jumping " + displayMessage + " " + newPos.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
                    //empty jumpmodule after jump
                    warpdrive.removeCharge();
                    warpdrive.setCharge(0);
                    warpdrive.sendChargeUpdate();
                    if (intoWarp) { //ship has (will) successfully changed into warp
                    //    ModPlayground.broadcastMessage("starting checkloop");
                        final SegmentController s = ship;
                        new StarRunnable() {
                            @Override
                            public void run() {
                         //       warpLoop.startLoop(s); //start loop that will drop the ship back out if its to slow
                            }
                        }.runLater(WarpMain.instance, 25*5);
                    //Does not work atm    WarpThrustManager.OverwriteThrust((Ship)ship,true);
                    } else {
                        //ship has successfully dropped out of warp
                    //Does not work atm   WarpThrustManager.OverwriteThrust((Ship)ship,false);
                    };
                } else {
                    ship.sendControllingPlayersServerMessage(Lng.astr("Jump failed, warpdrive needs to cooldown."), ServerMessage.MESSAGE_TYPE_INFO);
                    DebugFile.log("jumping into warp failed");
                }
                navigationHelper.handlePilots(ship,intoWarp);

            }
        }, WarpMain.instance);
    }



    public static void dropOutOfWarp(SegmentController ship) {
        Vector3i posNow = ship.getSector(new Vector3i());
    //    Vector3i newPos = GetRealSpacePos(posNow);
        if (ship.getType() == SimpleTransformableSendableObject.EntityType.SPACE_STATION) {
            newPos = getRandomSector();
        }
        SectorSwitch sectorSwitch = GameServer.getServerState().getController().queueSectorSwitch(ship,newPos,SectorSwitch.TRANS_JUMP,false,true,true);
        if (sectorSwitch != null) {
            sectorSwitch.delay = System.currentTimeMillis();
            sectorSwitch.executionGraphicsEffect = (byte) 2;
            ship.sendControllingPlayersServerMessage(Lng.astr("Dropping out of warp at " + newPos.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
            //empty jumpmodule after jump
        } else {
            ship.sendControllingPlayersServerMessage(Lng.astr("Jump failed, warpdrive needs to cooldown."), ServerMessage.MESSAGE_TYPE_INFO);
            DebugFile.log("jumping into warp failed");
        }
        navigationHelper.handlePilots(ship,false);

        //remove from tracking list
        if (warpLoop.warpEntities.contains(ship)) {
            warpLoop.warpEntities.remove(ship);
        }
    //    WarpThrustManager.OverwriteThrust((Ship)ship,false);
    }
    private static Vector3i getRandomSector() {
        Vector3i sector = new Vector3i();
        sector.x = (int) Math.round(Math.random() * 1000 - 500);
        sector.y = (int) Math.round(Math.random() * 200 - 100);
        sector.z = (int) Math.round(Math.random() * 1000 - 500);
        DebugFile.log("chose random sector: " + sector.toString());
        return sector;
    }
}
