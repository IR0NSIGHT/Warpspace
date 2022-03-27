package me.iron.WarpSpace.Mod.beacon;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
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
import org.schema.game.common.controller.SegmentController;
import org.schema.game.mod.Mod;
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
            System.out.println("BEACONMANAGER FAILED TO LOAD FOR WARPSPACE");
            throw ex;
        }
    }

    transient private HashMap<Vector3i,ArrayList<String>> beacons_by_sector = new HashMap(){
        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public Object remove(Object key) {
            return super.remove(key);
        }
    };
    transient private HashMap<String ,BeaconObject> beacons_by_UID = new HashMap<>();
    transient private Random random;
    public BeaconManager() {

    }

    public void activateAll() {
        for (BeaconObject b: beacons_by_UID.values()) {
            SegmentController sc = GameServerState.instance.getSegmentControllersByName().get(b.getUID());
            if (sc == null)
                continue;
            b.activateAddon(sc);
        }
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
                    BeaconObject b = beacons_by_UID.get(event.getController().getUniqueIdentifier());
                    if (b == null)
                        return;
                    //FIXME infinite loop? b.activateAddon(event.getController());
                    updateBeacon(b);
                }
            }, WarpMain.instance);

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
        beacons_by_sector.clear();
        beacons_by_UID.clear();
        synchAll();
    }

    private void updateAllBeacons() {
        if (GameServerState.instance == null)
            return;

        ArrayList<BeaconObject> temp = new ArrayList<>(beacons_by_UID.values());
        for (BeaconObject b : temp) {
            if (b == null)
                continue;
            updateBeacon(b);

        }
    //    print();
    }

    private BeaconObject getBeaconByUID(String UID) {
        return beacons_by_UID.get(UID);
    }

    /**
     * will modify the given droppoint with existing beacon positions.
     * @param warpPos
     * @param dropPos
     */
    public BeaconObject modifyDroppoint(Vector3i warpPos, Vector3i dropPos) {
        ArrayList<String> beacons = beacons_by_sector.get(warpPos);
        if (beacons == null || beacons.size() == 0 || getBeaconByUID(beacons.get(0))==null)
            return null;
        BeaconObject pulling = getBeaconByUID(beacons.get(0));
        dropPos.set(pulling.getPosition());
        return pulling;
    }

    /**
     * will return the strongest beacon that affects this warppos. will update and validate beacons in the process.
     * @param warpPos
     * @return null if no beacon.
     */
    public void updateStrongest(Vector3i warpPos) {
        //sort the list of beacons by their strength.
        ArrayList<String> list = beacons_by_sector.get(warpPos);
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

                if (b1.getStrength()==b2.getStrength()) {
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
        ArrayList<String> list = beacons_by_sector.get(warpPos);
        if (list == null) {
            list = new ArrayList<>();
            beacons_by_sector.put(warpPos,list);
        }
        if (!list.contains(beacon.getUID())) {
            list.add(beacon.getUID());
        }
        beacons_by_UID.put(beacon.getUID(),beacon);
        if (isServer && GameServerState.instance != null) {
        //    ModPlayground.broadcastMessage("added beacon: " + beacon.getName());
            updateStrongest(warpPos);
            synchAll();
        }
    }

    public void removeBeacon(BeaconObject beacon) {

        Vector3i warpPos = WarpManager.getWarpSpacePos(beacon.getPosition());
        ArrayList<String> list = beacons_by_sector.get(warpPos);
        if (list == null) {
            return;
        }
        list.remove(beacon.getUID());
        beacons_by_UID.remove(beacon.getUID());
        ModPlayground.broadcastMessage("REMOVING BEACON "+beacon.getUID());
        if (isServer) {
        //    ModPlayground.broadcastMessage("removed beacon: " + beacon.getName());

            updateStrongest(warpPos);
            synchAll();
        }
    }

    //TODO use linkedlist
    public ArrayList<String> getBeacons(Vector3i warpPos) {
        return beacons_by_sector.get(warpPos);
    }

    public Collection<Vector3i> getBeaconSectors() {
        return beacons_by_sector.keySet();
    }

    public void synchAll() {
        if (!WarpMain.instance.beaconManagerServer.equals(this))
            return;

        new BeaconUpdatePacket().sendToAll();
    }

    /**
     * used for loading and reading server sent packets.
     * @param buffer
     */
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
            if (!isServer) //clientside
                WarpMain.instance.dropPointMapDrawer.flagForUpdate();

        } catch (Exception e) {
            System.out.println("BEACONMANAGER BUFFER READ ERROR");
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialize(PacketWriteBuffer packetWriteBuffer) {
        if (GameServerState.instance==null) //client doesnt need that.
            return;

        try {
            //collect ALL beacons in one big list
            ArrayList<BeaconObject> all = new ArrayList<>(beacons_by_UID.values());

            packetWriteBuffer.writeInt(all.size());

            for (BeaconObject beaconObject : all) {
                packetWriteBuffer.writeObject(beaconObject);
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
        for (BeaconObject beacon: beacons_by_UID.values()) {
            b.append(beacon.getName()).append("[").append(beacon.getStrength()).append("] : ").append(beacon.getPosition().toStringPure());
            if (beacon.isGodMode())
                b.append("  (god)");
            b.append("\n");
        }
        ModPlayground.broadcastMessage(b.toString());
    }
}
