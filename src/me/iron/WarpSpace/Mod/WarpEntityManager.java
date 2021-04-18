package me.iron.WarpSpace.Mod;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 16:42
 */

import me.iron.WarpSpace.Mod.server.InWarpLoop;
import org.schema.game.common.controller.SegmentController;

import java.util.ArrayList;
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
    public static List<SegmentController> shipsInWarp = new ArrayList<>();

    /**
     * declare ship a warpentity and start warp mechanics for ship.
     * !does not check if ship is already registered!
     * @param ship Segmentcontroller to be registered
     */
    public static void DeclareWarpEntity(SegmentController ship) {
        if (WarpEntityManager.isWarpEntity(ship)) {
            return;
        }
        shipsInWarp.add(ship);
        InWarpLoop.startLoop(ship);
    }
    public static void RemoveWarpEntity(SegmentController ship) {
        if (shipsInWarp.contains(ship)) {
            shipsInWarp.remove(ship);
        }
    }

    /**
     * check if an entity is registered as a warp entity in the WarpEntityManager.shipsInWarp list.
     * @param ship segmentcontroller for ship
     * @return boolean, true if a warp entity, false if not.
     */
    public static boolean isWarpEntity(SegmentController ship) {
        if (shipsInWarp.contains(ship)) {
            return true;
        }
        return false;
    }
}
