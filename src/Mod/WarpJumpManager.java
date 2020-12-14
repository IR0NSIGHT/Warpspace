package Mod;

import api.DebugFile;
import api.common.GameServer;
import api.utils.StarRunnable;
import net.rudp.impl.Segment;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 15:13
 */

/**
 * handles all jumps happening and related methods like checking for interdiction etc.
 */
public class WarpJumpManager {

    public static List<SegmentController> dropQueue = new ArrayList<>();;
    public static List<SegmentController> entryQueue = new ArrayList<>();;
    /**
     * will drop the given ship out of warp after x seconds to specified sector.
     * will not check if ship is allowed to drop, only if it already has a drop queued.
     * Automatically handles effects etc.
     * @param countdown do jump in x second
     * @param ship ship to warp
     * @param sector targeted sector to drop into
     * @param isJump is a jump or an autodrop, will empty warpdrive if true
     * @param force overwrite all checks, admin
     */
    public static void invokeDrop(long countdown, final SegmentController ship, final Vector3i sector, final boolean isJump, boolean force) {
        countdown *= 25; //turn seconds into ticks
        //check if already dropping
        if (!force && dropQueue.contains(ship)) { //ship already has a drop queued, and doesnt force another one.
            ship.sendControllingPlayersServerMessage(Lng.astr("Ship is already jumping"), ServerMessage.MESSAGE_TYPE_INFO);
            //TODO abort already queued jump -> click to jump, click again to abort
            return;
        }
        if (isJump) {
            ship.sendControllingPlayersServerMessage(Lng.astr("Jumpdrive charging up"), ServerMessage.MESSAGE_TYPE_INFO);
        }
        dropQueue.add(ship);
        new StarRunnable() {
            @Override
            public void run() {
                if (GameServerState.isShutdown() || GameServerState.isFlagShutdown()) {
                    DebugFile.log("WarpCheckLoop was terminated on server shutdown",WarpMain.instance);
                    cancel();
                }
                if (dropQueue.contains(ship)) { //remove from queue
                    dropQueue.remove(ship);
                }

                if (isJump) {
                    //empty warpdrive
                    emptyWarpdrive(ship);
                }
                //queue sector switch
                doSectorSwitch(ship, sector,true);
                ship.sendControllingPlayersServerMessage(Lng.astr("Dropping out of warp"), ServerMessage.MESSAGE_TYPE_INFO);
                //TODO add visual effects
                //navigationHelper.handlePilots(ship,intoWarp);
            }
        }.runLater(WarpMain.instance,countdown);
    }

    /**
     * will make the given ship entry warp after x seconds to specified sector.
     * @param countdown in seconds
     * @param ship segmentcontroller
     * @param sector target sector to enter
     * @param force true if ignore all checks and force jump anyways
     */
    public static void invokeEntry(long countdown, final SegmentController ship, final Vector3i sector, boolean force) {
        countdown *= 25; //turn seconds into ticks

        //check if already dropping
        if (!force && entryQueue.contains(ship)) { //ship already has a jump queued, and doesnt force another one.
            ship.sendControllingPlayersServerMessage(Lng.astr("Ship is already jumping!"), ServerMessage.MESSAGE_TYPE_INFO);
            return;
        }
        entryQueue.add(ship);

        ship.sendControllingPlayersServerMessage(Lng.astr("Jumpdrive charging up"), ServerMessage.MESSAGE_TYPE_INFO);

        new StarRunnable() {
            @Override
            public void run() {
                if (GameServerState.isShutdown() || GameServerState.isFlagShutdown()) {
                    DebugFile.log("WarpCheckLoop was terminated on server shutdown",WarpMain.instance);
                    cancel();
                }
                if (entryQueue.contains(ship)) { //remove from queue
                    entryQueue.remove(ship);
                }
                //empty warpdrive
                emptyWarpdrive(ship);
                //queue sector switch
                doSectorSwitch(ship, sector,true);
                ship.sendControllingPlayersServerMessage(Lng.astr("Entering warp"), ServerMessage.MESSAGE_TYPE_INFO);
                //TODO add visual effects and navwaypoint change
                //navigationHelper.handlePilots(ship,intoWarp);
            }
        }.runLater(WarpMain.instance,countdown);
    }

    /**
     * get a random sector in a 500 x 100 x 500 radius
     * @return
     */
    private static Vector3i getRandomSector() {
        Vector3i sector = new Vector3i();
        sector.x = (int) Math.round(Math.random() * 1000 - 500);
        sector.y = (int) Math.round(Math.random() * 200 - 100);
        sector.z = (int) Math.round(Math.random() * 1000 - 500);
        DebugFile.log("chose random sector: " + sector.toString());
        return sector;
    }
    private static void handleStation(SegmentController station) {

    }

    public static void doSectorSwitch(SegmentController ship, Vector3i newPos, boolean instant) {
        //TODO refactor method, make instant?
        SectorSwitch sectorSwitch = GameServer.getServerState().getController().queueSectorSwitch(ship,newPos,SectorSwitch.TRANS_JUMP,false,true,true);
        if (sectorSwitch != null) {
            sectorSwitch.delay = System.currentTimeMillis();
            if (!instant) {
                sectorSwitch.delay += 4000;
            }
            sectorSwitch.jumpSpawnPos = new Vector3f(ship.getWorldTransform().origin); //position in sector (?)
            sectorSwitch.executionGraphicsEffect = (byte) 2;
            sectorSwitch.keepJumpBasisWithJumpPos = true;

        } else {
            ship.sendControllingPlayersServerMessage(Lng.astr("Jump failed, warpdrive needs to cooldown."), ServerMessage.MESSAGE_TYPE_INFO);
            DebugFile.log("jumping into warp failed");
        }
    }
    /**
     * Check if a ship is allowed to enter the warp
     * @param ship segmentcontroller to check
     * @return boolean, true if allowed entry, false if interdicted or can fire warpdrive
     */
    public static boolean isAllowedEntry(SegmentController ship) {
        if (isInterdicted(ship) || !canExecuteWarpdrive(ship)) {
            return false;
        }
    //    DebugFile.log("isAllowedEntry is an empty check");
        return true;
    }

    /**
     * Check if a ship is allowed to drop out of warp
     * checks interdiction
     * checks warpdrive.canExecute
     * @param ship segmentcontroller ship
     * return boolean, true if not interdicted and can fire warpdrive
     */
    public static boolean isAllowedDropJump(SegmentController ship) {
        if (isInterdicted(ship) || !canExecuteWarpdrive(ship)) {
            return false;
        }
    //    DebugFile.log("isAllowedDrop is an empty check");
        return true;
    }

    /**
     * will remove one jump charge from the FTL drive of the specified ship.
     * @param ship ship segmentcontroller
     */
    public static void emptyWarpdrive(SegmentController ship) {
        //get jumpaddon
        JumpAddOn warpdrive;
        if(ship instanceof ManagedSegmentController<?>) {
            warpdrive =((Ship)ship).getManagerContainer().getJumpAddOn();
        } else {
            DebugFile.log("entity " + ship.getName() + "tried jumping but is no managed SC.");
            return;
        }
        warpdrive.removeCharge();
        warpdrive.setCharge(0.0F);
        warpdrive.sendChargeUpdate();
    }
    public static boolean canExecuteWarpdrive(SegmentController ship) {
        //get jumpaddon
        JumpAddOn warpdrive;
        if(ship instanceof ManagedSegmentController<?>) {
            warpdrive =((Ship)ship).getManagerContainer().getJumpAddOn();
        } else {
            DebugFile.log("entity " + ship.getName() + "tried jumping but is no managed SC.");
            return false;
        }
        if (!warpdrive.canExecute()) {
            return false;
        }
        return true;
    }
    public static boolean isInterdicted(SegmentController ship) {
        //TODO add interdiction check
        return false;
    }
}
