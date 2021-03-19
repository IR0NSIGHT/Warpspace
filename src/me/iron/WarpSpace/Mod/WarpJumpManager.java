package me.iron.WarpSpace.Mod;

import me.iron.WarpSpace.Mod.HUD.client.WarpProcessController;
import api.DebugFile;
import api.common.GameServer;
import api.mod.StarLoader;
import api.network.packets.PacketUtil;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.network.PacketHUDUpdate;
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
    public static void invokeDrop(long countdown, final SegmentController ship, Vector3i sector, final boolean isJump, boolean force) {
        countdown *= 25; //turn seconds into ticks
        //check if already dropping
        if (!force && dropQueue.contains(ship)) { //ship already has a drop queued, and doesnt force another one.
            ship.sendControllingPlayersServerMessage(Lng.astr("Ship is already jumping"), ServerMessage.MESSAGE_TYPE_INFO);
            //TODO abort already queued jump -> click to jump, click again to abort
            return;
        }
        if (ship.getType().equals(SimpleTransformableSendableObject.EntityType.SPACE_STATION) )
        {
            //its a spacestation. drop to a random sector.
            // reason: could drop second station into realspace sector by spawning in warp otherwise.
            // also: could drop battlestation from warp into sector, maybe even homebase
            sector = WarpJumpManager.getRandomSector();
        }
        //--------------before action is taken
        //--------------after action is taken
        if (isJump) {
            ship.sendControllingPlayersServerMessage(Lng.astr("Jumpdrive charging up"), ServerMessage.MESSAGE_TYPE_INFO);
            SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPEXIT, 1, new ArrayList<String>()); //set exiting process to true
        } else {
            //must be a speeddrop situation is already handeled by loop.
            //SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPDROP,1);
        }


        final Vector3i sectorF = sector;
        dropQueue.add(ship);
        new StarRunnable() {
            @Override
            public void run() {
                if (GameServerState.isShutdown() || GameServerState.isFlagShutdown()) {
                    cancel();
                }
                if (dropQueue.contains(ship)) { //remove from queue
                    dropQueue.remove(ship);
                }

                //create, fire event, get back params
                WarpJumpEvent.WarpJumpType type;
                if (isJump) {
                    type = WarpJumpEvent.WarpJumpType.EXIT;
                } else {
                    type = WarpJumpEvent.WarpJumpType.DROP;
                }

                WarpJumpEvent e = new WarpJumpEvent(ship,type,ship.getSector(new Vector3i()),sectorF);
                StarLoader.fireEvent(e, true);

                if (isJump) {
                    SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPEXIT, 0, new ArrayList<String>());
                } else {
                    //is a speeddrop (drop bc to slow)
                    SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPDROP,0, new ArrayList<String>());
                }


                if (e.canceled) {
                    cancel();
                    return;
                }
                //-- event

                if (isJump) {
                    //empty warpdrive
                    emptyWarpdrive(ship);
                }
                //queue sector switch
                doSectorSwitch(ship, sectorF,true);
                ship.sendControllingPlayersServerMessage(Lng.astr("Dropping out of warp"), ServerMessage.MESSAGE_TYPE_INFO);
                //TODO add visual effects
                //TODO drop station to random pos
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

        //set entry process to true/happening
        SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPENTRY,1, new ArrayList<String>());
        ship.sendControllingPlayersServerMessage(Lng.astr("Jumpdrive charging up"), ServerMessage.MESSAGE_TYPE_INFO);

        new StarRunnable() {
            @Override
            public void run() {
                if (GameServerState.isShutdown() || GameServerState.isFlagShutdown()) {
                    cancel();
                }
                if (entryQueue.contains(ship)) { //remove from queue
                    entryQueue.remove(ship);
                }

                //create, fire event, get back params
                WarpJumpEvent.WarpJumpType  type = WarpJumpEvent.WarpJumpType.ENTRY;

                WarpJumpEvent e = new WarpJumpEvent(ship,type,ship.getSector(new Vector3i()),sector);
                StarLoader.fireEvent(e, true);

                //for all attached players send travel update, bc drop is over
                SendPlayerWarpSituation(ship, WarpProcessController.WarpProcess.JUMPENTRY,0, new ArrayList<String>());

                if (e.canceled) {
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
        return sector;
    }
    private static void handleStation(SegmentController station) {
        //currently unused.
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
        }
    }
    /**
     * Check if a ship is allowed to enter the warp
     * @param ship segmentcontroller to check
     * @return boolean, true if allowed entry, false if interdicted or can fire warpdrive
     */
    public static boolean isAllowedEntry(SegmentController ship) {
        if (isInterdicted(ship,WarpManager.GetWarpSpacePos(ship.getSector(new Vector3i()))) || !canExecuteWarpdrive(ship)) {
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
        if (isInterdicted(ship,WarpManager.GetRealSpacePos(ship.getSector(new Vector3i()))) || !canExecuteWarpdrive(ship)) {
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

    /**
     * check if this ship is/would be interdicted at specified position.
     * @param ship ship
     * @param position positon to check from
     * @return
     */
    public static boolean isInterdicted(SegmentController ship, Vector3i position) {
        //TODO add interdiction check for target sector
        JumpAddOn warpdrive;
        if(ship instanceof ManagedSegmentController<?>) {
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
        try {
            shipReactorLvl = ((ManagedSegmentController<?>)ship).getManagerContainer().getPowerInterface().getActiveReactor().getLevel();
        } catch (Exception e) {
            e.printStackTrace();
            DebugFile.log("managercontainer null: " + (((ManagedSegmentController<?>)ship).getManagerContainer() == null));
            DebugFile.log("powerinterface null: " + (((ManagedSegmentController<?>)ship).getManagerContainer().getPowerInterface() == null));
            DebugFile.log("activereactor null: " + (((ManagedSegmentController<?>)ship).getManagerContainer().getPowerInterface().getActiveReactor() == null));
            return true;
        }

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
                    inhibitorStrength = neighbourSector.getRemoteSector().getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_STRENGTH, 1); //TODO change apply value to 300K?
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
     * @param ship
     * @return
     */
    public static boolean isInterdicted(SegmentController ship) {
        return isInterdicted(ship,ship.getSector(new Vector3i()));
    }

    /**
     * send hud update to specific player client computer
     * @param p playerstate player
     * @param s process thats happening
     * @param v value of process
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
     */
    public static void SendPlayerWarpSituation(SegmentController sc, WarpProcessController.WarpProcess process, Integer processValue, List<String> processArray) {
            if ((sc instanceof PlayerControllable && !((PlayerControllable)sc).getAttachedPlayers().isEmpty()))
            {
                for (PlayerState p: ((PlayerControllable)sc).getAttachedPlayers()) {
                    SendPlayerWarpSituation(p,process,processValue, processArray);
                }
            }
    }

}
