package me.iron.WarpSpace.Mod.beacon;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.ModSkeleton;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import api.mod.config.SimpleSerializerWrapper;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import org.lwjgl.Sys;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 25.10.2021
 * TIME: 12:05
 */
public class BeaconManager extends SimpleSerializerWrapper {
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
            return manager;
        } catch (Exception ex) {
            System.out.println("BEACONMANAGER FAILED TO LOAD FOR WARPSPACE");
            throw ex;
        }
    }

    private HashMap<Vector3i,ArrayList<BeaconObject>> sectorToBeaconMap = new HashMap<>();
    private Random random;
    public BeaconManager() {
        addBeacon(new BeaconObject(
                new Vector3i(2,2,2),
                true,
                "TEST BEACON UID",
                -1,
                100,
                SimpleTransformableSendableObject.EntityType.ASTEROID,
                true,
                "UWU EMPIRE NOOB TRAP",
                "UWU EMPIRE"
        ));
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

        print();
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

    private void updateBeacon(BeaconObject beacon) {
        if (beacon == null)
            return;
        beacon.update();
        if (beacon.isFlagForDelete()) {
            removeBeacon(beacon);
        }
    }

    public void addBeacon(BeaconObject beacon) {
        Vector3i warpPos = WarpManager.GetWarpSpacePos(beacon.getPosition());
        ArrayList<BeaconObject> list = sectorToBeaconMap.get(warpPos);
        if (list == null) {
            list = new ArrayList<>();
            sectorToBeaconMap.put(warpPos,list);
        }
        if (!list.contains(beacon)) {
            list.add(beacon);
        }
    }

    public void removeBeacon(BeaconObject beacon) {
        Vector3i warpPos = WarpManager.GetWarpSpacePos(beacon.getPosition());
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
    /*    try {
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
        } */
    }

    @Override
    public void onSerialize(PacketWriteBuffer packetWriteBuffer) {
    /*    try {
            //collect ALL beacons in one big list
            ArrayList<BeaconObject> all = new ArrayList<>();
            packetWriteBuffer.writeInt(sectorToBeaconMap.values().size());
            for (ArrayList<BeaconObject> sector: sectorToBeaconMap.values()) {
                all.addAll(sector);
            }
            packetWriteBuffer.writeInt(all.size());
            for (BeaconObject beaconObject : all) {
                packetWriteBuffer.writeObject(beaconObject);
            }

        } catch (IOException e) {
            System.out.println("BEACONMANAGER BUFFER WRITE ERROR");
            e.printStackTrace();
        }
    */
    }

    //DEBUG STUFF
    private void print() {
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
