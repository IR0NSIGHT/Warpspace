package Mod;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 16:42
 */

import Mod.server.InWarpLoop;
import api.DebugFile;
import org.schema.game.common.controller.SegmentController;

import java.util.List;

/**
 * has list of all entities in warp
 * starts loop for them
 * manages list
 * gets info from checkloop
 */
public class WarpEntityManager {
    /**
     * List that holds references to every single ship currently in warp. Is filled/cleaned regularly by the warpcheckloop.
     */
    public static List<SegmentController> shipsInWarp;

    /**
     * declare ship a warpentity and start warp mechanics for ship.
     * !does not check if ship is already registered!
     * @param ship
     */
    public static void DeclareWarpEntity(SegmentController ship) {
        //TODO write method
        if (WarpEntityManager.isWarpEntity(ship)) {
            DebugFile.log("ship already is registered with warploop");
            return;
        }
        DebugFile.log("added ship to warpentities: " + ship.getName());
        shipsInWarp.add(ship);
        InWarpLoop.startLoop(ship);


        //add to list
        //start ship loop
        //handle pilots
    }
    public static void RemoveWarpEntity(SegmentController ship) {
        if (shipsInWarp.contains(ship)) {
            shipsInWarp.remove(ship);
        }
    }

    /**
     * check if an entity is registered as a warp entity in the WarpEntityManager.shipsInWarp list.
     * @param ship
     * @return
     */
    public static boolean isWarpEntity(SegmentController ship) {
        if (shipsInWarp.contains(ship)) {
            return true;
        }
        return false;
    }
}
