package testing;

import Mod.server.interdiction.SectorManager;

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

        SectorManager.SetSectorStatus(1000, SectorManager.InterdictionState.noEntry,1);
        SectorManager.SetSectorStatus(1000, SectorManager.InterdictionState.pull,2);
        SectorManager.SetSectorStatus(1000, SectorManager.InterdictionState.noExit,3);
  //      SectorManager.SetSectorStatus(1008, SectorManager.InterdictionState.noExit,1);
   //     SectorManager.SetSectorStatus(1, SectorManager.InterdictionState.noExit,1);


        SectorManager.toMap(SectorManager.toArray());


        String string = "empty";
        for (Map.Entry<Integer,Integer[]> s: SectorManager.map.entrySet()) {
            Integer sectorID = s.getKey();
            System.out.println("sector " + sectorID);

            for (int i = 0; i < s.getValue().length; i++) {
                Integer stateValue = s.getValue()[i];
                SectorManager.InterdictionState state1 = SectorManager.InterdictionState.valueOf(i);
                assert  state1 != null : "state1 is null";

                string = state1.toString() + " : " + SectorManager.GetSectorStatus(sectorID,state1);
                System.out.println(string);
            }
        }
    //    System.out.println(string);

   /*     GenerateSectors(10000);
        System.out.println("searching " + sectorList.size() + " entries");

        long start = System.currentTimeMillis();
        //    System.out.println(sectorList.toString());
        WarpSector s = FindSectorInMap(sectorList.size());
        //System.out.println(s.toString());
        long duration = System.currentTimeMillis() - start;

        System.out.println("map: " + duration + " millis");

        start = System.currentTimeMillis();
        s = FindSectorInList(sectorList.size());
        duration = System.currentTimeMillis() - start;
        System.out.println("list: " + duration + " millis");

    */
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
