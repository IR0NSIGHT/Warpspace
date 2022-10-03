package me.iron.WarpSpace.Mod;

import me.iron.WarpSpace.Mod.beacon.BeaconManager;
import me.iron.WarpSpace.Mod.client.WarpProcess;
import api.common.GameServer;
import api.mod.StarLoader;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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

    public static HashSet<SimpleTransformableSendableObject> dropQueue = new HashSet<>();
    public static HashSet<SimpleTransformableSendableObject> entryQueue = new HashSet<>();

    public static void invokeJumpdriveUsed(SimpleTransformableSendableObject object, boolean forceJump) {
        if (!forceJump && (object instanceof Ship && !canExecuteWarpdrive((Ship)object)))
            return;
        //check if ship is in warp or not, check if ship is allowed to perform the jump
        if (WarpManager.isInWarp(object) && WarpJumpManager.isAllowedDropJump(object)) { //is in warpspace, get realspace pos
            WarpJumpManager.invokeDrop((long) (1000* ConfigManager.ConfigEntry.seconds_warpjump_delay.getValue()),object,true, forceJump);
        } else if (!WarpManager.isInWarp(object)&& WarpJumpManager.isAllowedEntry(object)) { //is in realspace, get warppos
            WarpJumpManager.invokeEntry((long) (1000* ConfigManager.ConfigEntry.seconds_warpjump_delay.getValue()),object,forceJump);
        }
    }

    /**
     * will drop the given ship out of warp after x seconds to specified sector.
     * will not check if ship is allowed to drop, only if it already has a drop queued.
     * Automatically handles effects etc.
     * @param countdown do jump in millis
     * @param ship ship to warp
     * @param isJump is a jump or an autodrop, will empty warpdrive if true
     * @param force overwrite all checks, admin
     */
    public static void invokeDrop(long countdown, final SimpleTransformableSendableObject ship, final boolean isJump, boolean force) {
        //check if already dropping
        if (!force && dropQueue.contains(ship)) { //ship already has a drop queued, and doesnt force another one.
            return;
        }
        //set process values for attached players
        if (isJump) {
            WarpProcess.setProcess(ship, WarpProcess.JUMPEXIT, 1);
        } else {
            //must be a speeddrop situation is already handeled by loop.
            WarpProcess.setProcess(ship,WarpProcess.JUMPDROP,1);
            countdown = 100; //almost immediate drop
        }

        dropQueue.add(ship);
        //invoke sectorswitch
        new TimedRunnable((int) countdown, WarpMain.instance, 1) {
            private void afterRun() {
                WarpProcess.setProcess(ship, WarpProcess.JUMPEXIT, 0);
                WarpProcess.setProcess(ship,WarpProcess.JUMPDROP,0);
            }

            @Override
            public void onRun() {
                //remove from queue
                dropQueue.remove(ship);

                //create, fire event, get back params
                WarpJumpEvent.WarpJumpType type;

                type = isJump?WarpJumpEvent.WarpJumpType.EXIT:WarpJumpEvent.WarpJumpType.DROP;

                Vector3i warpPos = ship.getSector(new Vector3i());


                Vector3i targetSector = getDropPoint(warpPos);
                if (ship.getType().equals(SimpleTransformableSendableObject.EntityType.SPACE_STATION) ) {
                    Random r = new Random();
                    r.setSeed(targetSector.code());
                    int range = 25;
                    targetSector.add(r.nextInt(range),r.nextInt(range),r.nextInt(range));
                    ship.sendControllingPlayersServerMessage(Lng.astr("Unstable station drop, position shifted"),ServerMessage.MESSAGE_TYPE_WARNING);
                }

                WarpJumpEvent e = new WarpJumpEvent(ship,type,warpPos,targetSector);
                StarLoader.fireEvent(e, true);

                afterRun();

                if (isJump) {
                    emptyWarpdrive(ship);
                }

                if (!isAllowedDropJump(ship)) {
                    return;
                }



                //queue sector switch
                doSectorSwitch(ship, targetSector,true);
            }
        };
    }

    /**
     * will make the given ship entry warp after x seconds to specified sector.
     * @param countdown in millis
     * @param ship segmentcontroller
     * @param force true if ignore all checks and force jump anyways
     */
    public static void invokeEntry(long countdown, final SimpleTransformableSendableObject ship, boolean force) {
        //check if already dropping
        if (!force && entryQueue.contains(ship)) { //ship already has a jump queued, and doesnt force another one.
            ship.sendControllingPlayersServerMessage(Lng.astr("Ship is already jumping!"), ServerMessage.MESSAGE_TYPE_INFO);
            return;
        }
        entryQueue.add(ship);

        //set entry process to true/happening
        WarpProcess.setProcess(ship,WarpProcess.JUMPENTRY,1);
        new TimedRunnable((int) countdown, WarpMain.instance, 1) {
            private void afterRun() {
                WarpProcess.setProcess(ship, WarpProcess.JUMPENTRY, 0);
            }

            @Override
            public void onRun() {
                //remove from queue
                entryQueue.remove(ship);

                //create, fire event, get back params
                WarpJumpEvent.WarpJumpType  type = WarpJumpEvent.WarpJumpType.ENTRY;

                Vector3i sector = WarpManager.getWarpSpacePos(ship.getSector(new Vector3i()));
                WarpJumpEvent e = new WarpJumpEvent(ship,type,ship.getSector(new Vector3i()),sector);
                StarLoader.fireEvent(e, true);


                afterRun();
                if (!WarpJumpManager.isAllowedEntry(ship)) {
                    return;
                }
                //-- event

                //empty warpdrive
                emptyWarpdrive(ship);
                //queue sector switch
                doSectorSwitch(ship, sector,true);
                ship.sendControllingPlayersServerMessage(Lng.astr("Entered warp"), ServerMessage.MESSAGE_TYPE_INFO);
            }
        };
    }

    public static void doSectorSwitch(SimpleTransformableSendableObject ship, Vector3i newPos, boolean instant) {
        SectorSwitch sectorSwitch = GameServer.getServerState().getController().queueSectorSwitch(ship,newPos,SectorSwitch.TRANS_JUMP,false,true,true);
        if (sectorSwitch != null) {
            WarpProcess.setProcess(ship,WarpProcess.HAS_JUMPED,1);
            sectorSwitch.delay = System.currentTimeMillis();
            if (!instant) {
                sectorSwitch.delay += 4000;
            }
            sectorSwitch.jumpSpawnPos = new Vector3f(ship.getWorldTransform().origin); //position in sector (?)
            sectorSwitch.executionGraphicsEffect = (byte) 2;
            sectorSwitch.keepJumpBasisWithJumpPos = true;

        } else {
            ship.sendControllingPlayersServerMessage(Lng.astr("Jump failed, warpdrive needs to cooldown."), ServerMessage.MESSAGE_TYPE_INFO);
        }
    }

    /**
     * Check if a ship is allowed to enter the warp
     * @param ship SimpleTransformableSendableObject (Object) to check
     * @return boolean, true if allowed entry, false if interdicted or can't fire warpdrive (only for ships)
     */
    public static boolean isAllowedEntry(SimpleTransformableSendableObject ship) {
        return !isInterdicted(ship,WarpManager.getWarpSpacePos(ship.getSector(new Vector3i())));
    }

    /**
     * Check if a object is allowed to drop out of warp
     * checks interdiction
     * checks warpdrive.canExecute
     * @param object segmentcontroller object
     * @return boolean, true if not interdicted and can fire warpdrive
     */
    public static boolean isAllowedDropJump(SimpleTransformableSendableObject object) {
        return !isInterdicted(object,WarpManager.getRealSpacePos(object.getSector(new Vector3i())));
    }

    /**
     * will remove one jump charge from the FTL drive of the specified ship.
     * @param ship ship segmentcontroller
     */
    public static void emptyWarpdrive(SimpleTransformableSendableObject ship) {
        if(!(ship instanceof Ship))
            return;

        JumpAddOn warpdrive;
        warpdrive =((Ship)ship).getManagerContainer().getJumpAddOn();
        warpdrive.removeCharge();
        warpdrive.setCharge(0.0F);
        warpdrive.sendChargeUpdate();
    }

    public static boolean canExecuteWarpdrive(Ship ship) {
        //get jumpaddon
        JumpAddOn warpdrive;
        warpdrive =ship.getManagerContainer().getJumpAddOn();
        return warpdrive.canExecute();
    }

    public static boolean isDroppointShifted(Vector3i warpSector) {
        BeaconManager bm = (WarpMain.instance.beaconManagerServer!=null)?WarpMain.instance.beaconManagerServer:WarpMain.instance.beaconManagerClient;
        return bm.hasActiveBeacon(warpSector);
    }

    /**
     * check if this ship is/would be interdicted at specified position.
     * @param ship ship
     * @param position positon to check from
     * @return true if interdicted
     */
    public static boolean isInterdicted(SimpleTransformableSendableObject ship, Vector3i position) {
        //TODO PR into starmade repo, fix there.
        JumpAddOn warpdrive;
        if(ship instanceof Ship) {
            warpdrive =((Ship)ship).getManagerContainer().getJumpAddOn();
        } else {
            return false;
        }

        //use vanilla check method - check one sector in each direction for jumpaddons that interdict this one.
        assert warpdrive.isOnServer();
        Sector sector;
        boolean retVal = false;
        Vector3i neighbourSectorPos = new Vector3i();

        if (!(ship instanceof SegmentController))
            return false;

        try {
            sector = GameServer.getUniverse().getSector(position);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (sector == null) {
            return false;
        }

        int checkRange = 3; //range to check for inhibitors [sectors]
        int shipReactorLvl = 0;
        if (!(ship instanceof ManagedSegmentController)) {
            return false;
        }
        ManagedSegmentController msc = (ManagedSegmentController<?>)ship;
        if (msc.getManagerContainer() == null || msc.getManagerContainer().getPowerInterface() == null || msc.getManagerContainer().getPowerInterface().getActiveReactor() == null) {
            return false;
        };
        shipReactorLvl = msc.getManagerContainer().getPowerInterface().getActiveReactor().getLevel();

        int inhibitorStrength = 0;
        int catchesLvl = 0;
        double inhRange = 0;
        Vector3i currentSec = ship.getSector(new Vector3i());
        for (int x = -checkRange; x <= checkRange; ++x) {
            for (int y = -checkRange; y <= checkRange; ++y) {
                for (int z = -checkRange; z <= checkRange; ++z) {
                    neighbourSectorPos.set(sector.pos.x + z, sector.pos.y + y, sector.pos.z + x);
                    Sector neighbourSector;
                    if ((neighbourSector = GameServerState.instance.getUniverse().getSectorWithoutLoading(neighbourSectorPos)) == null) {
                        continue; //sector is not loaded
                    }
                    //get inhibitor level //returns [0..9]
                    inhibitorStrength = neighbourSector.getRemoteSector().getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_STRENGTH, 1);
                    catchesLvl = inhibitorStrength * 60; //will catch anything up to
                    //max inhRange of inhibitor
                    inhRange = Math.max(0, neighbourSector.getRemoteSector().getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_DISTANCE,  1));
                    int dx = Math.abs(currentSec.x - neighbourSectorPos.x);
                    int dy = Math.abs(currentSec.y - neighbourSectorPos.y);
                    int dz = Math.abs(currentSec.z - neighbourSectorPos.z);
                    double distance = Math.pow(dx * dx + dy * dy + dz * dz,0.5);
                    //TODO change config entry of inhibition power consumption
                    if (inhibitorStrength <= 1 || (distance > inhRange)) {
                        continue;
                    }
                    if (catchesLvl >= shipReactorLvl) {
                        warpdrive.getSegmentController().sendControllingPlayersServerMessage(new Object[]{" inhibitor detected in " + neighbourSectorPos.toString()}, 3);
                        retVal = true;
                        break;
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * returns drop point with applied beacon-shifting
     * @param warpSector
     * @return
     */
    public static Vector3i getDropPoint(Vector3i warpSector) {
        warpSector = new Vector3i(warpSector);
        //apply warp-beacon. inform player if beacon had effect.
        Vector3i drop = WarpManager.getRealSpacePos(warpSector);
        BeaconManager bm = (WarpMain.instance.beaconManagerServer!=null)?WarpMain.instance.beaconManagerServer:WarpMain.instance.beaconManagerClient;
        bm.updateStrongest(warpSector);
        bm.modifyDroppoint(warpSector, drop);
        return drop;
    }
}
