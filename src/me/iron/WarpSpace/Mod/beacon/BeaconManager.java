package me.iron.WarpSpace.Mod.beacon;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.listener.events.player.PlayerSpawnEvent;
import api.mod.ModSkeleton;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import api.mod.config.SimpleSerializerWrapper;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import me.iron.WarpSpace.Mod.TimedRunnable;
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
            return manager;
        } catch (Exception ex) {
            DebugFile.err("BEACONMANAGER FAILED TO LOAD FOR WARPSPACE");
            throw ex;
        }
    }

    transient final private HashMap<Vector3i,LinkedList<String>> beaconUIDs_by_sector = new HashMap<Vector3i,LinkedList<String>>(){
        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public LinkedList<String> remove(Object key) {
            return super.remove(key);
        }
    };
    transient final private HashMap<String ,BeaconObject> beacon_by_UID = new HashMap<>();
    transient private Random random;
    public BeaconManager() {
        //owo
    }

    public void onInit() {
        random = new Random(420);
        if (GameServerState.instance != null)
            isServer = true;

        if (isServer) {
            //listener to send new joined players the existing beacons so they can be displayed on their map
            StarLoader.registerListener(PlayerSpawnEvent.class, new Listener<PlayerSpawnEvent>() {
                @Override
                public void onEvent(PlayerSpawnEvent event) {
                    synchAll(); //!this will fire on respawn too, dont put single use things in here!
                }
            },WarpMain.instance);

            //listener to activate the beacon addon for ships that get loaded and are logged as beacons.
            StarLoader.registerListener(SegmentControllerFullyLoadedEvent.class, new Listener<SegmentControllerFullyLoadedEvent>() {
                @Override
                public void onEvent(SegmentControllerFullyLoadedEvent event) {
                    BeaconObject b = beacon_by_UID.get(event.getController().getUniqueIdentifier());
                    if (b == null)
                        return;
                    //FIXME infinite loop? b.activateAddon(event.getController());
                    updateBeacon(b);
                }
            }, WarpMain.instance);

            new TimedRunnable(5000, WarpMain.instance, -1) {
                @Override
                public void onRun() {
                    try {
                        updateAllBeacons();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        }
    }

    public void clearBeacons() {
        beaconUIDs_by_sector.clear();
        beacon_by_UID.clear();
        synchAll();
    }

    private void updateAllBeacons() {
        if (GameServerState.instance == null)
            return;

        ArrayList<BeaconObject> temp = new ArrayList<>(beacon_by_UID.values());
        for (BeaconObject b : temp) {
            if (b == null)
                continue;
            updateBeacon(b); //TODO collect beacons that need synch
        }
        synchAll();
    //    print();
    }

    public BeaconObject getBeaconByUID(String UID) {
        return beacon_by_UID.get(UID);
    }

    public Collection<BeaconObject> getBeacons() {
        return beacon_by_UID.values();
    }

    /**
     * will modify the given droppoint with existing beacon positions.
     * @param warpPos 3d warp position
     * @param dropPos sector position in realspace
     */
    public BeaconObject modifyDroppoint(Vector3i warpPos, Vector3i dropPos) {
        LinkedList<String> beacons = beaconUIDs_by_sector.get(warpPos);
        if (beacons == null || beacons.size() == 0 || getBeaconByUID(beacons.get(0))==null)
            return null;
        BeaconObject pulling = getBeaconByUID(beacons.get(0));
        if (pulling.isActive())
            dropPos.set(pulling.getPosition()); //FIXME handle inactive beacons proplery, this is a hotfix
        return pulling;
    }

    /**
     * will return the strongest beacon that affects this warppos. will update and validate beacons in the process.
     * @param warpPos position in warp
     */
    public void updateStrongest(Vector3i warpPos) {
        //sort the list of beacons by their strength.
        LinkedList<String> list = beaconUIDs_by_sector.get(warpPos);
        if (list == null)
            return;
        BeaconObject b;
        for (String uid: list) {
            b = getBeaconByUID(uid);
            if (b == null)
                continue;
           updateBeacon(b);
        }
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                BeaconObject b1 = getBeaconByUID(o1);
                BeaconObject b2 = getBeaconByUID(o2);

                if (b1.getStrength()-(b1.isActive()?0:10)==b2.getStrength()-(b2.isActive()?0:10)) { //inactive = -10 strength penalty, -> order between inactive beacons not destroyed
                    random.setSeed(b1.getPosition().code());
                    return random.nextInt();
                }
                return b1.getStrength()-b2.getStrength();
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
        //FIXME decline
        Vector3i warpPos = WarpManager.getWarpSpacePos(beacon.getPosition());
        LinkedList<String> list = beaconUIDs_by_sector.get(warpPos);
        if (list == null) {
            list = new LinkedList<>();
            beaconUIDs_by_sector.put(warpPos,list);
        }
        if (!list.contains(beacon.getUID())) {
            list.add(beacon.getUID());
        }
        beacon_by_UID.put(beacon.getUID(),beacon);
        if (isServer && GameServerState.instance != null) {
            updateStrongest(warpPos);
            synchAll();
        }
    }

    public void removeBeacon(BeaconObject beacon) {
        Vector3i warpPos = WarpManager.getWarpSpacePos(beacon.getPosition());
        Collection<String> list = beaconUIDs_by_sector.get(warpPos);
        if (list == null) {
            return;
        }
        list.remove(beacon.getUID());
        if (list.isEmpty()) {
            beaconUIDs_by_sector.remove(warpPos);
        }
        beacon_by_UID.remove(beacon.getUID());
        if (isServer) {
            updateStrongest(warpPos);
            synchAll();
        }
    }

    //TODO use linkedlist
    public Collection<String> getBeacons(Vector3i warpPos) {
        return beaconUIDs_by_sector.get(warpPos);
    }

    public Collection<Vector3i> getBeaconSectors() {
        return beaconUIDs_by_sector.keySet();
    }

    public boolean hasActiveBeacon(Vector3i warppos) {
        if (!beaconUIDs_by_sector.containsKey(warppos))
            return false;
        Collection<String> uids = this.beaconUIDs_by_sector.get(warppos);
        if (uids.size() == 0)
            return false;
        assert beacon_by_UID.containsKey(uids.iterator().next());
        return beacon_by_UID.get(uids.iterator().next()).isActive();
    }

    public void synchAll() {
        if (!WarpMain.instance.beaconManagerServer.equals(this))
            return;

        new BeaconUpdatePacket().sendToAll();
    }

    /**
     * used for loading and reading server sent packets.
     * @param buffer packet read buffer
     */
    @Override
    public void onDeserialize(PacketReadBuffer buffer) {

        try {
            LinkedList<BeaconObject> beacons = new LinkedList<>();
            int totalSize = buffer.readInt();
            for (int i = 0; i < totalSize; i ++) {
                BeaconObject beacon = buffer.readObject(BeaconObject.class);
                if (beacon == null)
                    continue;
                beacons.add(beacon);
            }

            if (!isServer) {//clientside => received synch from server
                for (BeaconObject b: beacons) {
                    if (beacon_by_UID.containsKey(b.getUID())) {
                        //beacon exists, update from dummy
                        beacon_by_UID.get(b.getUID()).synchFromDummy(b);
                    } else {
                        //add new beacon if it doesnt exist yet
                        addBeacon(b);
                    }
                    //delete flagged beacons
                    if (b.isFlagForDelete()) {
                        removeBeacon(b);
                    }
                }
                WarpMain.instance.dropPointMapDrawer.flagForUpdate();
            }
            else {
                //serverside => loading from persistence
                beaconUIDs_by_sector.clear();
                beacon_by_UID.clear();
                for (BeaconObject b: beacons) {
                    addBeacon(b);
                }
            }
        } catch (Exception e) {
            DebugFile.err("BEACONMANAGER BUFFER READ ERROR");
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialize(PacketWriteBuffer packetWriteBuffer) {
        if (GameServerState.instance==null) //client doesnt need that.
            return;
        try {
            //collect ALL beacons in one big list
            ArrayList<BeaconObject> all = new ArrayList<>(beacon_by_UID.values());

            packetWriteBuffer.writeInt(all.size());

            for (BeaconObject beaconObject : all) {
                packetWriteBuffer.writeObject(beaconObject);
            }

        } catch (Exception e) {
            DebugFile.err("BEACONMANAGER BUFFER WRITE ERROR");
            e.printStackTrace();
        }

    }

    //DEBUG STUFF
    public String print() {
        StringBuilder b = new StringBuilder();
        b.append("BeaconManager:\n");
        for (BeaconObject beacon: beacon_by_UID.values()) {
            b.append(beacon).append("\n");
        }
        return  b.toString();
    }
}
