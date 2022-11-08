package me.iron.WarpSpace.Mod;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 15:29
 */

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;


/**
 * defines mechanics in warp, hold settings of the warp like its position.
 */
public class WarpManager {
    private static WarpManager instance;

    public static WarpManager getInstance() {
        return instance;
    }

    public WarpManager(float sectorSize, int scale, int offset, int warpDimensions) {
        //make sure that warpable space and warpspace do not intersect
        if (warpDimensions/scale>=offset)
            throw new IllegalArgumentException("realspace and warpspace intersect");

        this.sectorSize = sectorSize;
        //64 systems per galaxy, 16 sectors per system (hardcoded) //TODO assert these values are hardcoded
        this.scale = scale;
        halfScale = new Vector3i(0.5f*scale,0.5f*scale, 0.5f*scale);
        this.offset = offset;

        realspaceMin = new Vector3i(-warpDimensions, -warpDimensions, -warpDimensions);
        realspaceMax = new Vector3i(warpDimensions, warpDimensions, warpDimensions);
        instance = this;
    }

    private Random random = new Random();

    private int scale;
    private Vector3i halfScale = null;
    public float sectorSize;

    /**
     * the offset of warpspace to the realspace sector on the y axis. Use a number outside of the galaxy: empty space
     */
    public int offset;

    private Vector3i realspaceMin;
    private Vector3i realspaceMax;
    /**
     * check if an objects positon is in warpspace
     * @param object segmentcontroller to check
     * @return boolean, true if segmentcontrollers position is in warp
     */
    public boolean isInWarp(SimpleTransformableSendableObject object) {
        return isInWarp(object.getSector(new Vector3i()));
    }


    /**
     * check if an objects positon is in warpspace
     * @param pos  position to check
     * @return boolean, true if position is in warp
     */
    public boolean isInWarp(Vector3i pos) {
        return isInRealSpace(getRealSpaceBySector(pos));
    }

    public boolean isInRealSpace(Vector3i sector) {
        return sector.betweenIncl(realspaceMin, realspaceMax);
    }


    /**
     * Calculate the Warpspace position from a realworld position, will round to closest point on scale: -5->0<-+4 at scale = 10
     *
     * @param rspPos sector in realspace
     * @return correlating sector in warpspace
     */
    public Vector3i getWarpSpaceSector(Vector3i rspPos) {
        Vector3f origin = getWarpOrigin(rspPos);
        metersToSector(origin);

        Vector3f warpPos = rspPos.toVector3f();
        warpPos.scale(1f / getScale());

        warpPos.sub(origin);
        warpPos.y += offset;
        return new Vector3i(
                Math.round(warpPos.x),
                Math.round(warpPos.y),
                Math.round(warpPos.z)
        );
    }

    /**
     * calculate the warp-in-sector position relevant to this realspace sector
     * @return origin in sector
     */
    public Vector3f getWarpOrigin(Vector3i rspPos) {
        Vector3i mutate = new Vector3i(rspPos);

        //round to -5 .. +4: 11 -> 10 + 1, 9 -> -1, 14 -> 10 + 4
        mutate.add(halfScale);
        mutate = new Vector3i(mod(mutate.x,getScale()), mod(mutate.y, getScale()), mod(mutate.z,getScale()));
        mutate.sub(halfScale);
        Vector3f warpOrigin = mutate.toVector3f();
        sectorsToMeter(warpOrigin);
        warpOrigin.scale(1f/getScale());
        return warpOrigin;
    }

    public StellarPosition getWarpSpacePosition(Vector3i realSpaceSector) {
        Vector3f origin = getWarpOrigin(realSpaceSector);
        Vector3f mutate = new Vector3f(origin);
        metersToSector(mutate);

        Vector3f warpPos = realSpaceSector.toVector3f();
        warpPos.scale(1f / getScale());

        warpPos.sub(mutate);
        warpPos.y += offset;
        Vector3i warpSector = new Vector3i(
                Math.round(warpPos.x),
                Math.round(warpPos.y),
                Math.round(warpPos.z)
        );
        return new StellarPosition(warpSector, origin);
    }

    public Vector3i getRealSpacePosPrecise(Vector3i warpSpacePos, Vector3f transformOrigin) {
        Vector3i rspSector = getRealSpaceBySector(warpSpacePos);

        transformOrigin = new Vector3f(transformOrigin);
        metersToSector(transformOrigin);
        transformOrigin.scale(getScale());
        rspSector.add((int)Math.round(transformOrigin.x), (int) Math.round(transformOrigin.y), (int) Math.round(transformOrigin.z));
        return rspSector;
    }

    /**
     * Calculate the realspace position from a warpspace position
     * @param warpSpacePos sector in warpspace
     * @return correlating sector in realspace
     */
    public Vector3i getRealSpaceBySector(Vector3i warpSpacePos) {
        Vector3i realPos;
        Vector3f warpPosF = warpSpacePos.toVector3f();

        warpPosF.y = warpPosF.y - offset; //offset sectors to up (y axis)
        warpPosF.x = Math.round(warpPosF.x * getScale());
        warpPosF.y = Math.round(warpPosF.y * getScale());
        warpPosF.z = Math.round(warpPosF.z * getScale());
        realPos = new Vector3i(warpPosF.x,warpPosF.y,warpPosF.z);
        return realPos;
    }

    public void metersToSector(Vector3f origin) {
        origin.scale(1/sectorSize);
    }

    public void sectorsToMeter(Vector3f origin) {
        origin.scale(sectorSize);
    }



    private int mod(int a, int b) {
        return (a % b + b) % b;
    }

    /**
     *  the scale of realspace to warpspace in sectors.
     *  defined by config value warp_to_rsp_ratio
     */
    public int getScale() {
        return scale;
    }

    public Vector3f getSectorCenter() {
        return new Vector3f(sectorSize*0.5f,sectorSize*0.5f,sectorSize*0.5f);
    }

    public Vector3f getClientTransformOrigin() {
        try {
            return new Vector3f(GameClientState.instance.getPlayer().getFirstControlledTransformableWOExc().getWorldTransform().origin);
        } catch(NullPointerException ex) {
            return new Vector3f();
        }
    }

    public static class StellarPosition {
        private Vector3i sector;
        private Vector3f origin;

        public StellarPosition(Vector3i sector, Vector3f origin) {
            this.sector = sector;
            this.origin = origin;
        }

        public Vector3f getPositionAdjustedFor(Vector3i sectorB) {
            Vector3f sectorBf = sectorB.toVector3f();
            Vector3f sectorAf = sector.toVector3f();

            sectorAf.sub(sectorBf);
            WarpManager.getInstance().sectorsToMeter(sectorAf);
            sectorAf.add(origin);

            return sectorAf;
            //sectorB, 000 adjust for me => me, 111

        }

        public Vector3f getFromTo(Vector3i sector, Vector3f origin) {
            Vector3i sectorOffset = new Vector3i(this.sector);
            sectorOffset.sub(sector);
            sectorOffset.scale((int) WarpManager.getInstance().sectorSize);

            Vector3f originOffset = new Vector3f(this.origin);
            originOffset.sub(origin);
            originOffset.add(sectorOffset.toVector3f());
            return originOffset;
        }

        public Vector3i getSector() {
            return sector;
        }

        public Vector3f getOrigin() {
            return origin;
        }

        @Override
        public String toString() {
            return "sector: " + sector.toString() + " origin: " + origin.toString();
        }
    }
}
