package testing;

import Mod.server.interdiction.SectorManager;
import org.schema.common.util.linAlg.Vector3i;

import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.12.2020
 * TIME: 17:28
 */
public class main {
    private static List<WarpSector> sectorList = new ArrayList<>();
    private static HashMap<Integer,Integer> sectorMap = new HashMap<>();
    public static void main(String[] args) {
        Vector3i sector = new Vector3i(2,2,2);
        Long id = SectorManager.SectorToID(sector);
        Vector3i sector1 = SectorManager.IDToSector(id);
        System.out.println("sector: " + sector.toString() + "id:" + id + " out " + sector1.toString());
    }

    //test how fast 1000 sectors can be searched for their effects

    /**
     * generate number of sectors with random properties
     * @param number
     */
    private static void GenerateSectors(int number) {
        Random r = new Random();

        for (int i = 0; i < number; i++) {
            WarpSector sector = new WarpSector(i + 1, r.nextBoolean());
            sectorList.add(sector);
            sectorMap.put(sector.id,sector.inhibitedInt);
        //    System.out.println("i = " + i + sector.toString());
        }
    }

    private static WarpSector FindSectorInList(int id) {
        for (WarpSector s: sectorList) {
            if (s.id == id) {
                return s;
            }
        }
        return null;
    }
    private static WarpSector FindSectorInMap(int id) {
        int blockedInt = sectorMap.get(id);
        boolean blocked = (blockedInt == 1);
        WarpSector s = new WarpSector(id,blocked);
        return s;
    }
}

/**
 * wrapper class holding a sectors identifier and if its inhibited or not (blocked)
 */
class WarpSector {
    public int id;
    public boolean inhibited;
    public int inhibitedInt;

    public enum inhibitEffect {
        RSP_NOENTRY,
        RSP_NOEXIT
    }
    public WarpSector(int id, boolean inhibited) {
        this.id = id;
        this.inhibited = inhibited;
        if (inhibited) {
            inhibitedInt = 1;
        } else {
            inhibitedInt = 0;
        }
    }
    public String toString() {
        return "WS " + id + " b: " + inhibited;
    }
}
