package Mod.HUD.client;

import java.util.HashMap;
import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.12.2020
 * TIME: 18:10
 */
public class WarpProcessController {
    /**
     * map that contains all warp related processes of this player with their current state: 0 not happening, 1 happening.
     * Example: warpEntry 1 -> currently charging drive trying to enter into warp.
     */
    public static HashMap<WarpProcess,Integer> WarpProcessMap = new HashMap<>();
    public static void initMap() {
        //TODO call init
        for (WarpProcess s: WarpProcess.values()) {
            WarpProcessMap.put(s,0);
        }
    }

    /**
     * enum containing available processes that can happen to a player like jumping to warp
     */
    public enum WarpProcess {
        WARPSECTORBLOCKED(0),
        RSPSECTORBLOCK(1),
        JUMPDROP(2),
        JUMPEXIT(3),
        JUMPENTRY(4),
        JUMPPULL(5),
        TRAVEL(6),

        SECTOR_NOEXIT(7),
        SECTOR_NOENTRY(8),
        PARTNER_NOEXIT(9),
        PARTNER_NOENTRY(10);

        private final int value;
        private static Map map = new HashMap<>();
        private WarpProcess(int value) {
            this.value = value;
        }

        static { //map enum value to int keys for reconstruction int -> enumvalue
            for (WarpProcess s: WarpProcess.values()) {
                map.put(s.getValue(),s);
            }
        }

        public static WarpProcess valueOf(int k) {
            return (WarpProcess) map.get(k);
        }

        public int getValue() {
            return value;
        }

    }
}
