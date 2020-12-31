package Mod.server.interdiction;

import Mod.HUD.client.WarpProcessController;
import Mod.WarpManager;
import api.DebugFile;
import it.unimi.dsi.fastutil.Hash;
import org.luaj.vm2.ast.Str;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementCollection;

import java.util.HashMap;
import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.12.2020
 * TIME: 20:04
 */
public class SectorManager {
    /**
     * all available kinds of interdiction to a sector
     */
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

        /**
         * translate into enum from int
         * @param k
         * @return
         */
        public static SectorManager.InterdictionState valueOf(int k) {
            return (SectorManager.InterdictionState) map.get(k);
        }

        /**
         * translate into int value from enum
         * @return
         */
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
    public static HashMap<Long, Integer[]> map = new HashMap<>();

    /**
     * set the interdiction state of specified sector
     * @param sectorID sector
     * @param sectorInterdictionState interdiction state
     * @param value value (1 for on, 0 for off)
     */
    public static void SetSectorStatus(Long sectorID, InterdictionState sectorInterdictionState, int value) {
        DebugFile.log("setting inhibition for sector " + IDToSector(sectorID).toString() + " to " + sectorInterdictionState + ": " + value);
        if (value != 1 && value != 0) {
            DebugFile.log("tried setting illegal value to interdiction map.");
            return;
        }

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

    /**
     * set the interdiction state of specified sector
     * @param sectorID sector
     * @param sectorInterdictionState interdiction state
     * @param value value on or off
     */
    public static void SetSectorStatus(Long sectorID, InterdictionState sectorInterdictionState, boolean value) {
        int val;
        if (value) {val = 1;} else {val = 0;};
        SetSectorStatus(sectorID,sectorInterdictionState,val);
    }

    /**
     * get if the interdiction state of specified sector is active or not
     * @param sectorID sector
     * @param state interdiction state
     * @return true (active) or false (inactive)
     */
    public static boolean GetSectorStatus(long sectorID, InterdictionState state) {

        Integer[] valueArr = map.get(sectorID);
        if (valueArr == null) { //not written to map -> no interdiction
            return false;
        }
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
            Map.Entry<Long,Integer[]> entry = (Map.Entry<Long, Integer[]>) ix;
            assert entry != null;
            SectorManager.map.put(entry.getKey(),entry.getValue());
        }
    }

    public static long SectorToID(Vector3i sector) {
        DebugFile.log("translating " + sector.toString() +" to ID" +sector.code());
        //Vector3i to long:
        long pos = ElementCollection.getIndex(sector);
        return pos;
    }

    public static Vector3i IDToSector(long sectorID) {
        //long to Vector3i:
        Vector3i pos = ElementCollection.getPosFromIndex(sectorID, new Vector3i());
        return pos;
    }

    public static void UpdateSectorInhibition(Vector3i sector) {
        DebugFile.log("updating sector " + sector.toString());
        //create needed variables
        Long sectorID = SectorToID(sector);
        //write to map for this sector
        //natural causes
        if (EnvironmentManager.IsVoidInhibition(sector) && !WarpManager.IsInWarp(sector)) {
            DebugFile.log("sector was updated, is void: no entry, no exit");
            SetSectorStatus(sectorID,InterdictionState.noEntry,true);
            SetSectorStatus(sectorID,InterdictionState.noExit,true);
        }
        //TODO add more inhibition checks
    }

    public static void UpdateSectorInhibition(Long sectorID) {
        UpdateSectorInhibition(IDToSector(sectorID));
    }
}
