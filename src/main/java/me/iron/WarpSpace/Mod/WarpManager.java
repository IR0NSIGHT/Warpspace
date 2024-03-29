package me.iron.WarpSpace.Mod;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.10.2020
 * TIME: 15:29
 */

import api.DebugFile;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.Galaxy;

import javax.vecmath.Vector3f;
import java.util.Random;

/**
 * defines mechanics in warp, hold settings of the warp like its position.
 */
public class WarpManager {
    private static Random random = new Random();

    /**
     * Galaxy size * System size * 64 + Galaxy size * System size * 64 / scale + System size * 2
     * 64 galaxies realspace + 64 galaxies warpspace + 2 systems buffer.
     */
    public static int universeSize = Galaxy.size * 16 * 8;

    /**
     * the offset of warpspace to the realspace sector on the y axis. Use a number outside of the galaxy: empty space
     */
    public static int offset = (int)(universeSize * (1 + 1f / getScale())) + 16 * 2; //offset in sectors

    /**
     *  minimum speed required to stay in warp
     */
    public static int minimumSpeed = 50;

    /**
     * check if an objects positon is in warpspace
     * @param object segmentcontroller to check
     * @return boolean, true if segmentcontrollers position is in warp
     */
    public static boolean isInWarp(SimpleTransformableSendableObject object) {
        if (object == null) {
            DebugFile.err("isInWarp called with null object");
            return false;
        }
        if (object.getSector(new Vector3i()) == null) {
            DebugFile.err("isInWarp object has no sector:"+object.getName());
            return false;
        }
        return isInWarp(object.getSector(new Vector3i()));
    }

    /**
     * check if an objects positon is in warpspace
     * @param pos  position to check
     * @return boolean, true if position is in warp
     */
    public static boolean isInWarp(Vector3i pos) {
        if (pos != null && pos.y >= offset - (universeSize / getScale()) && pos.y <= offset + (universeSize / getScale())) {
            return true;
        }
        return false;
    }

    /**
     * Calculate the Warpspace position from a realworld position, will round to closest point on scale: -5->0<-+4 at scale = 10
     * @param rspPos sector in realspace
     * @return correlating sector in warpspace
     */
    public static Vector3i getWarpSpacePos(Vector3i rspPos) {
        if (isInWarp(rspPos))
            return rspPos;
        Vector3i warpPos;
        Vector3f realPosF = rspPos.toVector3f();
        realPosF.x = Math.round(realPosF.x / getScale());
        realPosF.y = Math.round(realPosF.y / getScale());
        realPosF.z = Math.round(realPosF.z / getScale());
        realPosF.y += offset; //offset sectors to up (y axis)
        warpPos = new Vector3i(realPosF.x,realPosF.y,realPosF.z);

        return warpPos;
    }

    /**
     * Calculate the realspace position from a warpspace position
     * @param warpSpacePos sector in warpspace
     * @return correlating sector in realspace
     */
    public static Vector3i getRealSpacePos(Vector3i warpSpacePos) {
        if (!isInWarp(warpSpacePos))
            return warpSpacePos;
        Vector3i realPos;
        Vector3f warpPosF = warpSpacePos.toVector3f();
        warpPosF.y -= offset; //offset sectors to up (y axis)
        random.setSeed(warpSpacePos.code());
        warpPosF.x = Math.round(((random.nextBoolean()?(-1):1)* random.nextFloat()* ConfigManager.ConfigEntry.droppoint_random_offset.getValue())+warpPosF.x * getScale());
        warpPosF.y = Math.round(((random.nextBoolean()?(-1):1)* random.nextFloat()*ConfigManager.ConfigEntry.droppoint_random_offset.getValue())+warpPosF.y * getScale());
        warpPosF.z = Math.round(((random.nextBoolean()?(-1):1)* random.nextFloat()*ConfigManager.ConfigEntry.droppoint_random_offset.getValue())+warpPosF.z * getScale());
        realPos = new Vector3i(warpPosF.x,warpPosF.y,warpPosF.z);

        return realPos;
    }

    /**
     *  the scale of realspace to warpspace in sectors.
     *  defined by config value warp_to_rsp_ratio
     */
    public static int getScale() {
        return (int) ConfigManager.ConfigEntry.warp_to_rsp_ratio.getValue();
    }
}
