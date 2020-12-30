package Mod.server.interdiction;

import api.DebugFile;
import api.ModPlayground;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2020
 * TIME: 14:08
 */
public class EnvironmentManager {
    public static void AddStarsToInhibition() {
        //StellarSystem.isStarSystem()
    }

    /**
     * check if this sector has a natural cause for inhibition
     * write interdiction states to sectormanager map
     * @return
     */
    public static SectorManager.InterdictionState SetNaturalInhibition(Vector3i sector) {
        //star inhibition
        //blackhole inhibition
        //storm inhibition
        //void inhibition
        if (true) { //IsVoidInhibition(sector) //FIXME debug method remove
            //DebugFile.log("sector is void -> inhibited");
            SectorManager.SetSectorStatus(SectorManager.SectorToID(sector), SectorManager.InterdictionState.noEntry,1);
            SectorManager.SetSectorStatus(SectorManager.SectorToID(sector), SectorManager.InterdictionState.noExit,1);
            //return SectorManager.InterdictionState.noEntry;
        }
        return null;
    }

    /**
     * void systems cant be accessed through warp -> need star or blackhole to navigate to it (dev) //TODO void inhibition
     * @param sector
     * @return boolean
     */
    public static boolean IsVoidInhibition(Vector3i sector) {
        DebugFile.log("check for void inhibition");
        //get distance to system star
        try {

            StellarSystem sys = GameServerState.instance.getUniverse().getStellarSystemFromSecPos (sector);
            if (sys.getCenterSectorType().equals(SectorInformation.SectorType.VOID)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            DebugFile.log("void inhibiton check failed. ---- " + e.toString());
        }
        return false;
    }

    public static boolean IsSunInhibition(Vector3i sector) {
        return false;
    }
    //loop that writes natural inhibiton for all sectors

    /**
     * set up natural inhibition for sectors around 000 with specified range (box).
     * SUPER SLOW DONT USE
     * @param range sectors +/- from origin
     */
    public static void initNaturalInhibition(int range) {
        Vector3i sector = new Vector3i(1,1,1);
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    sector.x = x;
                    sector.y = y;
                    sector.z = z;
                    DebugFile.log("checking for sector " + sector.toString() + " has code " + sector.code());
                    SetNaturalInhibition(sector);
                }
            }
        }
    }


}
