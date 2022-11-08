import static org.junit.Assert.*;

import javax.vecmath.Vector3f;

import org.junit.Test;
import org.schema.common.util.linAlg.Vector3i;

import me.iron.WarpSpace.Mod.WarpManager;

public class WarpManagerTest {
    @Test
    public void preciseDroppoint() {
        float sectorSize = 12000;
        WarpManager manager = new WarpManager(
                sectorSize,
                10,
                150,
                1000
        );
        Vector3i trueRspSector = new Vector3i(13,25,-35);
        Vector3i roundedRspSector = new Vector3i(10,30,-30);

        Vector3i warpPos = manager.getWarpSpaceSector(trueRspSector);
        assertEquals(roundedRspSector,manager.getRealSpaceBySector(warpPos));

        assertEquals(
                manager.getRealSpaceBySector(warpPos),
                manager.getRealSpacePosPrecise(warpPos, new Vector3f(0,0,0)));

        Vector3i nextWarppos = new Vector3i(warpPos);
        nextWarppos.add(1,1,1);

        assertEquals(
                manager.getRealSpaceBySector(nextWarppos),
                manager.getRealSpacePosPrecise(warpPos,new Vector3f(sectorSize,sectorSize,sectorSize))
        );
    }

    @Test
    public void sectorCalculation() {
        int offset = 150;
        int sectorSize = 10000;
        WarpManager manager = new WarpManager(
                sectorSize,
                10,
                offset,
                1000
        );

        Vector3i rspPos = new Vector3i(127,-128,129);
        Vector3f origin = WarpManager.getInstance().getWarpOrigin(rspPos);
        Vector3i warpPos = WarpManager.getInstance().getWarpSpaceSector(rspPos);

        Vector3i drop = WarpManager.getInstance().getRealSpacePosPrecise(warpPos, origin);
        assertEquals(rspPos, drop);

        warpPos.set(10,-20+offset,30);
        origin.set(5000,-5000,4500);
        assertEquals(new Vector3i(105,-205,305), WarpManager.getInstance().getRealSpacePosPrecise(
                warpPos,
                origin
        ));

        //rounded up to 5000
        origin.set(5000,-5000,4501);
        assertEquals(new Vector3i(105,-205,305), WarpManager.getInstance().getRealSpacePosPrecise(
                warpPos,
                origin
        ));

        //rounded down to 4000s
        origin.set(5000,-5000,4499);
        assertEquals(new Vector3i(105,-205,304), WarpManager.getInstance().getRealSpacePosPrecise(
                warpPos,
                origin
        ));
    }

    @Test
    public void warpSectorCalculation() {
        int offset = 100;
        int sectorSize = 1;
        WarpManager manager = new WarpManager(
                sectorSize,
                10,
                offset,
                0
        );
        Vector3i rspPos = new Vector3i(-5, -14, -15);
        Vector3f origin = WarpManager.getInstance().getWarpOrigin(rspPos);
        Vector3i warpPos = WarpManager.getInstance().getWarpSpaceSector(rspPos);

        assertEquals(new Vector3f(-0.5f, -0.4f, -0.5f), origin);
        assertEquals(new Vector3i(0, -1+offset, -1), warpPos);

        rspPos.set(36, 1004, -4);
        System.out.println("warp " + WarpManager.getInstance().getWarpSpaceSector(rspPos));
        System.out.println("origin " + WarpManager.getInstance().getWarpOrigin(rspPos));

        rspPos.set(36, 1005, -4);
        System.out.println("warp " + WarpManager.getInstance().getWarpSpaceSector(rspPos));
        System.out.println("origin " + WarpManager.getInstance().getWarpOrigin(rspPos));

    }

    @Test
    public void stellarPos() {
        int offset = 0;
        int sectorSize = 100;
        WarpManager manager = new WarpManager(
                sectorSize,
                10,
                1000,
                offset
        );

        //same sector at zero coords => offset = origin
        Vector3i mySector = new Vector3i(0, 0, 0);

        Vector3i sectorB = new Vector3i(0, 0, 0);
        Vector3f origin = new Vector3f(5, 5, 5);

        WarpManager.StellarPosition p;
        p = new WarpManager.StellarPosition(sectorB, origin);
        assertEquals(new Vector3f(5, 5, 5), p.getPositionAdjustedFor(mySector));

        //with positive offset
        sectorB.set(1, 1, 1);
        p = new WarpManager.StellarPosition(sectorB, origin);
        assertEquals(new Vector3f(sectorSize + 5, sectorSize + 5, sectorSize + 5), p.getPositionAdjustedFor(mySector));

        //with negative offset
        sectorB.set(-1, 1, 1);
        p = new WarpManager.StellarPosition(sectorB, origin);
        assertEquals(new Vector3f(-sectorSize + 5, sectorSize + 5, sectorSize + 5), p.getPositionAdjustedFor(mySector));

        //with "big" offset (as big as it is expected to get in starmade)
        sectorB.set(-3, 3, 3);
        p = new WarpManager.StellarPosition(sectorB, origin);
        assertEquals(new Vector3f(-3 * sectorSize + 5, 3 * sectorSize + 5, 3 * sectorSize + 5), p.getPositionAdjustedFor(mySector));


        //same sector non zero coords => offset = origin
        mySector = new Vector3i(10, -10, 1000);
        sectorB = new Vector3i(10, -10, 1000);
        origin = new Vector3f(0, 7.8910f, -5);
        p = new WarpManager.StellarPosition(sectorB, origin);

        assertEquals(new Vector3f(origin), p.getPositionAdjustedFor(mySector));
    }

    //@Test
    public void multipleWarpPositions() {
        int offset = 0;
        WarpManager manager = new WarpManager(
                100,
                10,
                1000,
                offset
        );

        Vector3i rspSector = new Vector3i(-10, 10, 100);
        Vector3i warpSector = WarpManager.getInstance().getWarpSpaceSector(rspSector);
        for (int i = 0; i < 20; i++) {
            Vector3i rspSectorI = new Vector3i(rspSector);
            rspSectorI.set(i, 0, 0);
            WarpManager.StellarPosition p = manager.getWarpSpacePosition(rspSectorI);
            System.out.println(p);
            System.out.println(p.getPositionAdjustedFor(warpSector));
        }
    }

    @Test
    public void isInWarpSpace() {
        //test calculation if a sector is realspace
        int dimension = 1000;
        WarpManager manager = new WarpManager(
                1234567,
                10,
                5000,
                dimension
        );

        assertTrue(manager.isInRealSpace(new Vector3i(0,0,0)));
        //positive edge
        assertTrue(manager.isInRealSpace(new Vector3i(dimension, 0,0)));
        assertTrue(manager.isInRealSpace(new Vector3i(0, dimension,0)));
        assertTrue(manager.isInRealSpace(new Vector3i(0, 0,dimension)));
        assertTrue(manager.isInRealSpace(new Vector3i(dimension,dimension,dimension)));

        //negative edge
        assertTrue(manager.isInRealSpace(new Vector3i(-dimension, 0,0)));
        assertTrue(manager.isInRealSpace(new Vector3i(0, -dimension,0)));
        assertTrue(manager.isInRealSpace(new Vector3i(0, 0,-dimension)));
        assertTrue(manager.isInRealSpace(new Vector3i(-dimension,-dimension,-dimension)));

        //outside of positive dimension
        assertFalse(manager.isInRealSpace(new Vector3i(dimension+1, 0,0)));
        assertFalse(manager.isInRealSpace(new Vector3i(0, dimension+1,0)));
        assertFalse(manager.isInRealSpace(new Vector3i(0, 0,dimension+1)));

        //outside of neg dimension
        assertFalse(manager.isInRealSpace(new Vector3i(-dimension-1, 0,0)));
        assertFalse(manager.isInRealSpace(new Vector3i(0, -dimension-1,0)));
        assertFalse(manager.isInRealSpace(new Vector3i(0, 0,-dimension-1)));

        //if in rsp => warppos in warp
        assertTrue(manager.isInWarp(manager.getWarpSpaceSector(new Vector3i(0,0,0))));
        assertTrue(manager.isInWarp(manager.getWarpSpaceSector(new Vector3i(dimension, dimension, dimension))));

        //if not rsp => warppos is not warp
        assertFalse(manager.isInWarp(manager.getWarpSpaceSector(new Vector3i(dimension+manager.getScale(), 0,0))));

    }

    @Test (expected = IllegalArgumentException.class)
    public void rspAndWarpIntersect() {
        WarpManager manager = new WarpManager(
                1235,
                1,
                10,
                100
        );

        manager = new WarpManager(
                1235,
                10,
                10,
                100
        );
    }

}
