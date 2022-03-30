package me.iron.WarpSpace.Mod;

import me.iron.WarpSpace.Mod.beacon.BeaconManager;
import me.iron.WarpSpace.Mod.client.WarpProcessController;
import api.common.GameServer;
import api.mod.StarLoader;
import api.network.packets.PacketUtil;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.network.PacketHUDUpdate;
import org.lwjgl.Sys;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

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
     * @param isJump is a jump or an autodrop, will empty warpdrive if true
     * @param force overwrite all checks, admin
     */
    public static void invokeDrop(long countdown, final SegmentController ship, final boolean isJump, boolean force) {
        //check if already dropping
        if (!force && dropQueue.contains(ship)) { //ship already has a drop queued, and doesnt force another one.
        //    ship.sendControllingPlayersServerMessage(Lng.astr("Ship is already jumping"), ServerMessage.MESSAGE_TYPE_INFO);
            //TODO abort already queued jump -> click to jump, click again to abort
            return;
        }
        final boolean isRandom;
        if (ship.getType().equals(SimpleTransformableSendableObject.EntityType.SPACE_STATION) )
        {
            //its a spacestation. drop to a random sector.
            // reason: could drop second station into realspace sector by spawning in warp otherwise.
            // also: could drop battlestation from warp into sector, maybe even homebase
            isRandom = true;
        }
        //--------------before action is taken
        //--------------after action is taken
        if (isJump) {
            SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPEXIT, 1, new ArrayList<String>()); //set exiting process to true
        } else {
            //must be a speeddrop situation is already handeled by loop.
            //SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPDROP,1);
        }

        final  long dropTime = System.currentTimeMillis()+(countdown*1000);
        dropQueue.add(ship);
        new StarRunnable() {
            @Override
            public void run() {
                if (GameServerState.isShutdown() || GameServerState.isFlagShutdown()) {
                    cancel();
                }
                if (System.currentTimeMillis()<dropTime) {
                    return;
                }
                //remove from queue
                dropQueue.remove(ship);

                //create, fire event, get back params
                WarpJumpEvent.WarpJumpType type;

                type = isJump?WarpJumpEvent.WarpJumpType.EXIT:WarpJumpEvent.WarpJumpType.DROP;

                Vector3i warpPos = ship.getSector(new Vector3i());
                Vector3i targetSector = getDropPoint(warpPos);

                WarpJumpEvent e = new WarpJumpEvent(ship,type,warpPos,targetSector);
                StarLoader.fireEvent(e, true);

                if (isJump) {
                    SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPEXIT, 0, new ArrayList<String>());
                } else {
                    //is a speeddrop (drop bc to slow)
                    SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPDROP,0, new ArrayList<String>());
                }


                if (e.isCanceled() || !isAllowedDropJump(ship)) {
                    cancel();
                    return;
                }
                //-- event

                if (isJump) {
                    //empty warpdrive
                    emptyWarpdrive(ship);
                }

                //queue sector switch
                doSectorSwitch(ship, targetSector,true);
                cancel();
            }
        }.runTimer(WarpMain.instance,countdown);
    }

    /**
     * will make the given ship entry warp after x seconds to specified sector.
     * @param countdown in seconds
     * @param ship segmentcontroller
     * @param force true if ignore all checks and force jump anyways
     */
    public static void invokeEntry(long countdown, final SegmentController ship, boolean force) {
        //check if already dropping
        if (!force && entryQueue.contains(ship)) { //ship already has a jump queued, and doesnt force another one.
            ship.sendControllingPlayersServerMessage(Lng.astr("Ship is already jumping!"), ServerMessage.MESSAGE_TYPE_INFO);
            return;
        }
        entryQueue.add(ship);

        //set entry process to true/happening
        SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPENTRY,1, new ArrayList<String>());
        //ship.sendControllingPlayersServerMessage(Lng.astr("Jumpdrive charging up"), ServerMessage.MESSAGE_TYPE_INFO);
        final long dropTime = System.currentTimeMillis() + countdown*1000; //10 seconds from now
        new StarRunnable() {
            @Override
            public void run() {
                if (GameServerState.isShutdown() || GameServerState.isFlagShutdown()) {
                    cancel();
                }
                if (System.currentTimeMillis()<dropTime) {
                    return;
                }
                if (entryQueue.contains(ship)) { //remove from queue
                    entryQueue.remove(ship);
                }

                //create, fire event, get back params
                WarpJumpEvent.WarpJumpType  type = WarpJumpEvent.WarpJumpType.ENTRY;

                Vector3i sector = WarpManager.getWarpSpacePos(ship.getSector(new Vector3i()));
                WarpJumpEvent e = new WarpJumpEvent(ship,type,ship.getSector(new Vector3i()),sector);
                StarLoader.fireEvent(e, true);

                //for all attached players send travel update, bc drop is over
                SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPENTRY,0, new ArrayList<String>());

                if (e.isCanceled() || !WarpJumpManager.isAllowedEntry(ship)) {
                    cancel();
                    return;
                }
                //-- event

                //empty warpdrive
                emptyWarpdrive(ship);
                //queue sector switch
                doSectorSwitch(ship, sector,true);
                ship.sendControllingPlayersServerMessage(Lng.astr("Entering warp"), ServerMessage.MESSAGE_TYPE_INFO);
                //TODO add visual effects and navwaypoint change
                cancel();
            }
        }.runTimer(WarpMain.instance,1);
    }

    /**
     * get a random sector in a 500 x 100 x 500 radius
     * @return random pos Vector3i
     */
    private static Vector3i getRandomSector() {
        Vector3i sector = new Vector3i();
        sector.x = (int) Math.round(Math.random() * 1000 - 500);
        sector.y = (int) Math.round(Math.random() * 200 - 100);
        sector.z = (int) Math.round(Math.random() * 1000 - 500);
        return sector;
    }

    public static void doSectorSwitch(SegmentController ship, Vector3i newPos, boolean instant) {
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
        }
    }

    /**
     * Check if a ship is allowed to enter the warp
     * @param ship segmentcontroller to check
     * @return boolean, true if allowed entry, false if interdicted or can fire warpdrive
     */
    public static boolean isAllowedEntry(SegmentController ship) {
        if (isInterdicted(ship,WarpManager.getWarpSpacePos(ship.getSector(new Vector3i()))) || !canExecuteWarpdrive(ship)) {
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
     * @return boolean, true if not interdicted and can fire warpdrive
     */
    public static boolean isAllowedDropJump(SegmentController ship) {
        if (isInterdicted(ship,WarpManager.getRealSpacePos(ship.getSector(new Vector3i()))) || !canExecuteWarpdrive(ship)) {
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
            return false;
        }
        if (!warpdrive.canExecute()) {
            return false;
        }
        return true;
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
    public static boolean isInterdicted(SegmentController ship, Vector3i position) {
        //TODO add interdiction check for target sector
        JumpAddOn warpdrive;
        if(ship instanceof Ship) {
            warpdrive =((Ship)ship).getManagerContainer().getJumpAddOn();
        } else {
            return false;
        }

        //use vanilla check method - check one sector in each direction for jumpaddons that interdict this one.
        assert warpdrive.isOnServer();
        GameServerState gameServerState;
        Sector sector;
        boolean retVal = false;
        Vector3i neighbourSectorPos = new Vector3i();

        //debug jumpdrive level
        if(ship.hasActiveReactors()){
        //    DebugFile.log ("warpdrive of "+ ship.getName() + " has level: " + ((ManagedSegmentController<?>)ship).getManagerContainer().getPowerInterface().getActiveReactor().getLevel());
        }

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
        Vector3i currentSec = ship.getSector(new Vector3i()); //TODO refactor me and make me pretty
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
                    //DebugFile.log("inhibitor has strength: " + inhibitorStrength + " catches rkt level: " + catchesLvl + " power cons: " + inhibitorStrength * 60000 + " range: " + inhRange + " vs: " + distance);
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
     * check if ship is interdicted at its current position
     * @param ship ship to check for
     * @return true if is interdicted
     */
    public static boolean isInterdicted(SegmentController ship) {
        return isInterdicted(ship,ship.getSector(new Vector3i()));
    }

    /**
     * send hud update to specific player client computer
     * @param p playerstate player
     * @param s process thats happening
     * @param v value of process
     * @param processArray extra info, not used atm
     */
    public static void SendPlayerWarpSituation(PlayerState p, WarpProcessController.WarpProcess s, Integer v, List<String> processArray) {
        //make packet with new wp, send it to players client
        PacketHUDUpdate packet = new PacketHUDUpdate(s, v, processArray);
        PacketUtil.sendPacket(p, packet);
    }

    /**
     * send HUD update to all clients of attached players of this segmentcontroller
     * @param sc ship
     * @param process warpprocess
     * @param processValue value of warpprocess (0 = off, 1 = on)
     * @param processArray extra info, not used atm
     */
    public static void SendPlayerWarpSituation(SegmentController sc, WarpProcessController.WarpProcess process, Integer processValue, List<String> processArray) {
            if ((sc instanceof PlayerControllable && !((PlayerControllable)sc).getAttachedPlayers().isEmpty()))
            {
                for (PlayerState p: ((PlayerControllable)sc).getAttachedPlayers()) {
                    SendPlayerWarpSituation(p,process,processValue, processArray);
                }
            }
    }

    public static Vector3i getDropPoint(Vector3i warpSector) {
        //apply warp-beacon. inform player if beacon had effect.
        Vector3i drop = WarpManager.getRealSpacePos(warpSector);
        BeaconManager bm = (WarpMain.instance.beaconManagerServer!=null)?WarpMain.instance.beaconManagerServer:WarpMain.instance.beaconManagerClient;
        bm.updateStrongest(warpSector);
        bm.modifyDroppoint(warpSector, drop);
        return drop;
    }
}
