import static org.junit.Assert.assertEquals;

import javax.vecmath.Vector3f;

import org.junit.Test;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.Galaxy;

import me.iron.WarpSpace.Mod.WarpManager;
public class WarpManagerTest {
    @Test
    public void preciseDroppoint() {
        float sectorSize = 12000;
        WarpManager manager = new WarpManager(
                sectorSize,
                Galaxy.size,
                10,
                0
        );
        Vector3i trueRspSector = new Vector3i(13,25,-35);
        Vector3i roundedRspSector = new Vector3i(10,30,-30);

        Vector3i warpPos = manager.getWarpSpaceSector(trueRspSector);
        assertEquals(new Vector3i(roundedRspSector),manager.getRealSpaceBySector(warpPos));

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
        int offset = 2500;
        WarpManager manager = new WarpManager(
                10000,
                Galaxy.size,
                10,
                offset
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
    public void idkDude() {
        int offset = 0;
        WarpManager manager = new WarpManager(
                1,
                Galaxy.size,
                10,
                offset
        );
        Vector3i rspPos = new Vector3i(-5, -14, -15);
        Vector3f origin = WarpManager.getInstance().getWarpOrigin(rspPos);
        Vector3i warpPos = WarpManager.getInstance().getWarpSpaceSector(rspPos);

        assertEquals(new Vector3f(-0.5f, -0.4f, -0.5f), origin);
        assertEquals(new Vector3i(0, -1, -1), warpPos);

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
                Galaxy.size,
                10,
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
                Galaxy.size,
                10,
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
}
