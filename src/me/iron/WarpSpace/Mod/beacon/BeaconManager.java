package me.iron.WarpSpace.Mod.beacon;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerSpawnEvent;
import api.mod.ModSkeleton;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import api.mod.config.SimpleSerializerWrapper;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.GameServerState;

import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 25.10.2021
 * TIME: 12:05
 */
public class BeaconManager extends SimpleSerializerWrapper {
    private boolean isServer;
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

    }

    public void onInit() {
        random = new Random(420);
        if (GameServerState.instance != null)         {
            isServer = true;
            StarLoader.registerListener(PlayerSpawnEvent.class, new Listener<PlayerSpawnEvent>() {
                @Override
                public void onEvent(PlayerSpawnEvent event) {
                    synchAll();
                }
            },WarpMain.instance);

            new StarRunnable() {
                long next = System.currentTimeMillis();
                @Override
                public void run() {
                    if (next<System.currentTimeMillis()) {
                        next = System.currentTimeMillis()+1000;
                        try {
                            updateAllBeacons();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.runTimer(WarpMain.instance,10);
        }
    }

    public void clearBeacons() {
        sectorToBeaconMap.clear();
        synchAll();
    }

    private void updateAllBeacons() {
        ArrayList<BeaconObject> temp = new ArrayList();
        for (ArrayList<BeaconObject> bs: sectorToBeaconMap.values()) {
            if (bs == null || bs.size() == 0)
                continue;
            temp.clear();
            temp.addAll(bs);
            for (BeaconObject b: temp) {
                updateBeacon(b);
            }
        }

        print();
    }
    /**
     * will modify the given droppoint with existing beacon positions.
     * @param warpPos
     * @param dropPos
     */
    public BeaconObject modifyDroppoint(Vector3i warpPos, Vector3i dropPos) {
        ArrayList<BeaconObject> beacons = sectorToBeaconMap.get(warpPos);
        if (beacons == null || beacons.size() == 0)
            return null;
        dropPos.set(beacons.get(0).getPosition());

        return beacons.get(0);
    }

    /**
     * will return the strongest beacon that affects this warppos. will update and validate beacons in the process.
     * @param warpPos
     * @return null if no beacon.
     */
    public void updateStrongest(Vector3i warpPos) {
        //sort the list of beacons by their strength.
        ArrayList<BeaconObject> list = sectorToBeaconMap.get(warpPos);
        if (list == null)
            return;
        for (BeaconObject o: list) {
            o.update();
        }
        Collections.sort(list, new Comparator<BeaconObject>() {
            @Override
            public int compare(BeaconObject o1, BeaconObject o2) {
                if (o1.getStrength()==o2.getStrength()) {
                    random.setSeed(o1.getPosition().code());
                    return random.nextInt();
                }
                return o1.getStrength()-o2.getStrength();
            }
        });
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
        if (isServer) {
            updateStrongest(warpPos);
            synchAll();
        }
    }

    public void removeBeacon(BeaconObject beacon) {
        Vector3i warpPos = WarpManager.getWarpSpacePos(beacon.getPosition());
        ArrayList<BeaconObject> list = sectorToBeaconMap.get(warpPos);
        if (list == null) {
            return;
        }
        list.remove(beacon);
        if (isServer) {
            updateStrongest(warpPos);
            synchAll();
        }
    }

    public ArrayList<BeaconObject> getBeacons(Vector3i warpPos) {
        return sectorToBeaconMap.get(warpPos);
    }

    public void synchAll() {
        if (!WarpMain.instance.beaconManagerServer.equals(this))
            return;

        new BeaconUpdatePacket().sendToAll();
    }

    @Override
    public void onDeserialize(PacketReadBuffer buffer) {
        sectorToBeaconMap.clear();
        try {
            int totalSize = buffer.readInt();
            for (int i = 0; i < totalSize; i ++) {
                BeaconObject beacon = buffer.readObject(BeaconObject.class);
                if (beacon == null)
                    continue;
                addBeacon(beacon);
            }
            if (!isServer) //clientside
                WarpMain.instance.dropPointMapDrawer.flagForUpdate();

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
