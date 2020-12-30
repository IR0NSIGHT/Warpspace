package Mod.server.interdiction;

import Mod.HUD.client.WarpProcessController;
import org.luaj.vm2.ast.Str;

import java.util.HashMap;
import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.12.2020
 * TIME: 20:04
 */
public class SectorManager {
    public enum InterdictionState {
        noEntry(0),
        noExit(1),
        pull(2);

        private final int value;
        private static Map map = new HashMap<>();
        InterdictionState(int value) {
            this.value = value;
        }

        static { //map enum value to int keys for reconstruction int -> enumvalue
            for (SectorManager.InterdictionState s: SectorManager.InterdictionState.values()) {
                map.put(s.getValue(),s);
            }
        }

        public static SectorManager.InterdictionState valueOf(int k) {
            return (SectorManager.InterdictionState) map.get(k);
        }

        public int getValue() {
            return value;
        }
        public String toString() {
            switch (this) {
                case pull: return "sector is pulling from other dimension / inhibition-pull";
                case noExit: return "sector can not be exited / inhibition-locked";
                case noEntry: return "sector can not be entered / inihibition-shield";
            }
            return "error :(";
        }
    }
    public static HashMap<Integer, Integer[]> map = new HashMap<>();

    public static void SetSectorStatus(Integer sectorID, InterdictionState sectorInterdictionState, int value) {
        Integer[] interdictionStateArray = map.get(sectorID);
        if (interdictionStateArray == null) {
            interdictionStateArray = new Integer[InterdictionState.map.size()]; //index in array = value of enum
        }
        for (int i = 0; i < interdictionStateArray.length; i ++) {
            if (interdictionStateArray[i] == null) {
                interdictionStateArray[i] = 0;
            }
        }
        //get value of given state
        int index = sectorInterdictionState.getValue();
        interdictionStateArray[index] = value;
        map.put(sectorID, interdictionStateArray);
    }
    public static boolean GetSectorStatus(Integer sectorID, InterdictionState state) {
        Integer[] valueArr = map.get(sectorID);
        int idx = state.value;
        assert valueArr[idx] != null : "null value for interdiction state";
        int value = valueArr[idx];
        assert (value == 1 || value == 0) : "illegal value for interdiction state";

        return  (value == 1);
    }

    /**
     * returns an array of entrysets for hashmaps
     * @return single level array of entrysets
     */
    public static Object[] toArray() {
        return map.entrySet().toArray();
    }

    /**
     * overwrites map with given array of entrysets
     * for synching between server and client
     * @param entries
     */
    public static void toMap(Object[] entries) {
        for (Object ix: entries) {
            Map.Entry<Integer,Integer[]> entry = (Map.Entry<Integer, Integer[]>) ix;
            assert entry != null;
            SectorManager.map.put(entry.getKey(),entry.getValue());
        }
    }
}
