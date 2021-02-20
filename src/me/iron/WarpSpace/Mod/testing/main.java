package me.iron.WarpSpace.Mod.testing;

import me.iron.WarpSpace.Mod.Interdiction.SectorManager;
import org.lwjgl.Sys;

import javax.vecmath.Vector3f;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.12.2020
 * TIME: 17:28
 */
public class main {
    //TODO check with dev branch which way is better
    private static List<WarpSector> sectorList = new ArrayList<>();
    private static HashMap<Integer,Integer> sectorMap = new HashMap<>();
    public static void main(String[] args) {
        Vector3f a = new Vector3f(2,2,2);
        Vector3f b = new Vector3f(0.5f,2,1);
        a.scale(1,b);
        System.out.println(a.toString());
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
