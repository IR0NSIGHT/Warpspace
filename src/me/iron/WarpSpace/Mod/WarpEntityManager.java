package me.iron.WarpSpace.Mod;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 16:42
 */

import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import java.util.HashMap;

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
    private static HashMap<Integer, InWarpRunnable> warpentities_to_runners = new HashMap<>();

    /**
     * declare ship a warpentity and start warp mechanics for ship.
     * !does not check if ship is already registered!
     * @param entity Segmentcontroller to be registered
     */
    public static void DeclareWarpEntity(SimpleTransformableSendableObject entity) {
        if (warpentities_to_runners.containsKey(entity.getId())) {
            return;
        }
        warpentities_to_runners.put(entity.getId(), new InWarpRunnable(entity));
    }
    public static void RemoveWarpEntity(int shipId) {
        InWarpRunnable r = warpentities_to_runners.get(shipId);
        if (r != null)
            r.doStop();
        warpentities_to_runners.remove(shipId);
    }

    /**
     * check if an entity is registered as a warp entity in the WarpEntityManager.shipsInWarp list.
     * @param ship segmentcontroller for ship
     * @return boolean, true if a warp entity, false if not.
     */
    public static boolean isWarpEntity(SimpleTransformableSendableObject ship) {
        if (warpentities_to_runners.containsKey(ship.getId())) {
            return true;
        }
        return false;
    }
}
