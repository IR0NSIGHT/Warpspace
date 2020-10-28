package Mod;

import api.DebugFile;
import api.common.GameServer;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
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



    /**
     * will drop the given ship out of warp after x seconds to specified sector.
     * will not check if ship is allowed to drop
     * Automatically handles effects etc.
     * @param countdown
     * @param ship
     * @param sector
     */
    public static void invokeDrop(long countdown, SegmentController ship, Vector3i sector, boolean force) {
    //TODO write drop method

        new StarRunnable() {
            @Override
            public void run() {

            }
        }.runLater(WarpMain.instance,countdown);
    }

    /**
     * will make the given ship entry warp after x seconds to specified sector.
     * @param countdown
     * @param ship
     * @param sector
     */
    public static void invokeEntry(long countdown, SegmentController ship, Vector3i sector, boolean force) {
    //TODO write entry method
    //    ship.sendControllingPlayersServerMessage(Lng.astr("Jumping " + displayMessage + " " + newPos.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
        //empty jumpmodule after jump
    //    warpdrive.removeCharge();
    //    warpdrive.setCharge(0);
    //    warpdrive.sendChargeUpdate();
     //   navigationHelper.handlePilots(ship,intoWarp);
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

    public static void doSectorSwitch(SegmentController ship, Vector3i newPos) {
        //TODO refactor method, make instant?
        SectorSwitch sectorSwitch = GameServer.getServerState().getController().queueSectorSwitch(ship,newPos,SectorSwitch.TRANS_JUMP,false,true,true);
        if (sectorSwitch != null) {
            sectorSwitch.delay = System.currentTimeMillis() + 4000;
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
     * @param ship
     */
    public static boolean isAllowedEntry(SegmentController ship) {
        if (isInterdicted(ship) || !canExecuteWarpdrive(ship)) {
            return false;
        }
        DebugFile.log("isAllowedEntry is an empty check");
        return true;
    }

    /**
     * Check if a ship is allowed to drop out of warp
     * @param ship
     */
    public static boolean isAllowedDrop(SegmentController ship) {
        if (isInterdicted(ship) || !canExecuteWarpdrive(ship)) {
            return false;
        }
        DebugFile.log("isAllowedDrop is an empty check");
        return true;
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
        //TODO check for interdiction -> not possible bc vanilla method is private
        //jump
        return true;
    }
    public static boolean isInterdicted(SegmentController ship) {
        //TODO add interdiction check
        return false;
    }
}
