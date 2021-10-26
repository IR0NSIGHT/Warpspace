package me.iron.WarpSpace.Mod.beacon;

import api.DebugFile;
import api.ModPlayground;
import api.mod.ModSkeleton;
import api.mod.config.PersistentObjectUtil;
import api.mod.config.SimpleSerializerWrapper;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import javax.jdo.annotations.Persistent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 25.10.2021
 * TIME: 12:05
 */
public class BeaconManager extends SimpleSerializerWrapper {
    private String s;
    public static BeaconManager getSavedOrNew(ModSkeleton skeleton) {
        try {
            ArrayList<Object> objs = PersistentObjectUtil.getObjects(skeleton,BeaconManager.class);
            BeaconManager manager;
            if (objs == null || objs.size() == 0) {
                manager = new BeaconManager();
                PersistentObjectUtil.addObject(skeleton,manager);
            } else {
                manager = (BeaconManager)objs.get(0);
            }

            PersistentObjectUtil.removeObject(WarpMain.instance.getSkeleton(), manager);

            PersistentObjectUtil.addObject(WarpMain.instance.getSkeleton(), manager);
            return manager;
        } catch (Exception ex) {
            System.out.println("BEACONMANAGER FAILED TO LOAD FOR WARPSPACE");
            throw ex;
        }
    }

    transient private HashMap<Vector3i,ArrayList<BeaconObject>> sectorToBeaconMap = new HashMap(){
        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public Object remove(Object key) {
            return super.remove(key);
        }
    };
    transient private Random random;
    public BeaconManager() {
   //     addBeacon(new BeaconObject(
   //             new Vector3i(2,2,2),
   //             true,
   //             "TEST BEACON UID",
   //             -1,
   //             100,
   //             SimpleTransformableSendableObject.EntityType.ASTEROID,
   //             true,
   //             "UWU EMPIRE NOOB TRAP",
   //             "UWU EMPIRE"
   //     ));
    }

    public void onInit() {
        random = new Random(420);
    }

    /**
     * will modify the given droppoint with existing beacon positions.
     * @param warpPos
     * @param dropPos
     */
    public BeaconObject modifyDroppoint(Vector3i warpPos, Vector3i dropPos) {
        BeaconObject strongest = getStrongestBeaconAt(warpPos);
        if (strongest != null)
            dropPos.set(strongest.getPosition());

        //print();
        return strongest;
    }

    /**
     * will return the strongest beacon that affects this warppos. will update and validate beacons in the process.
     * @param warpPos
     * @return null if no beacon.
     */
    public BeaconObject getStrongestBeaconAt(Vector3i warpPos) {
        ArrayList<BeaconObject> list = sectorToBeaconMap.get(warpPos);
        if (list == null)
            return null;
        ArrayList<BeaconObject> beacons = new ArrayList<>(list);
        //loop over all beacons registered for this sector, pick strongest.
        BeaconObject strongest = null;
        for (BeaconObject beacon: beacons) {
            updateBeacon(beacon);
            if (beacon.isFlagForDelete())
                continue;
            if (strongest == null || beacon.getStrength()>strongest.getStrength() || (strongest.getStrength()==beacon.getStrength()&&random.nextBoolean()))
                strongest = beacon;
        }

        return strongest;
    }

    public void updateBeacon(BeaconObject beacon) {
        if (beacon == null)
            return;
        beacon.update();
        if (beacon.isFlagForDelete()) {
            removeBeacon(beacon);
        }
    }

    public void addBeacon(BeaconObject beacon) {
        Vector3i warpPos = WarpManager.getWarpSpacePos(beacon.getPosition());
        ArrayList<BeaconObject> list = sectorToBeaconMap.get(warpPos);
        if (list == null) {
            list = new ArrayList<>();
            sectorToBeaconMap.put(warpPos,list);
        }
        if (!list.contains(beacon)) {
            list.add(beacon);
        }

        print();
    }

    public void removeBeacon(BeaconObject beacon) {
        Vector3i warpPos = WarpManager.getWarpSpacePos(beacon.getPosition());
        ArrayList<BeaconObject> list = sectorToBeaconMap.get(warpPos);
        if (list == null) {
            return;
        }
        list.remove(beacon);
    }

    public ArrayList<BeaconObject> getBeacons(Vector3i warpPos) {
        return sectorToBeaconMap.get(warpPos);
    }

    @Override
    public void onDeserialize(PacketReadBuffer buffer) {
        try {
            int totalSize = buffer.readInt();
            for (int i = 0; i < totalSize; i ++) {
                BeaconObject beacon = buffer.readObject(BeaconObject.class);
                if (beacon == null)
                    continue;
                addBeacon(beacon);
            }
        } catch (Exception e) {
            System.out.println("BEACONMANAGER BUFFER READ ERROR");
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialize(PacketWriteBuffer packetWriteBuffer) {
        try {
            //collect ALL beacons in one big list
            ArrayList<BeaconObject> all = new ArrayList<>();
            for (ArrayList<BeaconObject> sector: sectorToBeaconMap.values()) {
                all.addAll(sector);
            }


            packetWriteBuffer.writeInt(all.size());
            DebugFile.log("buffer wrote int : " +all.size());

            for (BeaconObject beaconObject : all) {
                packetWriteBuffer.writeObject(beaconObject);
                DebugFile.log("buffer wrote obj : " +beaconObject.toString());
            }

        } catch (Exception e) {
            System.out.println("BEACONMANAGER BUFFER WRITE ERROR");
            e.printStackTrace();
        }

    }

    //DEBUG STUFF
    public void print() {
        s ="uwu im the runtime manager";

        StringBuilder b = new StringBuilder();
        b.append("BeaconManager:\n");
        for (ArrayList<BeaconObject> beaconObjects: sectorToBeaconMap.values()) {
            for (BeaconObject beacon: beaconObjects) {
                b.append(beacon.getName()).append("[").append(beacon.getStrength()).append("] : ").append(beacon.getPosition().toStringPure());
                if (beacon.isGodMode())
                    b.append("  (god)");
                b.append("\n");
            }
        }
        ModPlayground.broadcastMessage(b.toString());
    }
}
