package me.iron.WarpSpace.Mod.client;

import api.ModPlayground;
import api.network.packets.PacketUtil;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.Interdiction.ExtraEventLoop;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.network.PacketHUDUpdate;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * enum containing available processes that can happen to a player like jumping to warp.
 * has means to set values for players on multiplayer, and synch those values to the matching clients.
 * on client, listeners can be added to each process, that will fire when the process' value was changed.
 */
public enum WarpProcess {
    //TODO add flag of process: autoreset after send
    WARPSECTORBLOCKED,
    RSPSECTORBLOCKED,
    JUMPDROP(true,false), //will drop,
    JUMPEXIT,
    JUMPENTRY,
    JUMPPULL,
    DROPPOINTSHIFTED(false,true),

    TRAVEL,
    SECTOR_NOEXIT,
    SECTOR_NOENTRY,
    PARTNER_NOEXIT,
    PARTNER_NOENTRY,
    IS_IN_WARP(false,true),
    IS_INHIBITED,
    WARP_STABILITY,
    HAS_JUMPED(true,false);

    protected boolean resetAfterUpdate; //reset to zero after updated.
    protected boolean clientOnly; //ignore server input
    WarpProcess() {}
    WarpProcess(boolean resetAfterUpdate, boolean clientOnly) {
        this.resetAfterUpdate = resetAfterUpdate;
        this.clientOnly = clientOnly;
    }

    private static LinkedList<WarpProcess> changedValues = new LinkedList<>();

    //holds wp values for each player
    private static HashMap<PlayerState, long[]> player_to_processArr = new HashMap<>();
    private static StarRunnable updater;
    public static void setProcess(SegmentController sc, WarpProcess wp, long value) {
        if (!(sc instanceof  PlayerControllable))
            return;
        Collection<PlayerState> attached = ((PlayerControllable) sc).getAttachedPlayers();
        for (PlayerState p : attached)
            setProcess(p, wp, value);
    }

    public static void initUpdateLoop() {
        updater = new StarRunnable() {
            final int wait = 500; //millis
            long last = System.currentTimeMillis() + 10000;
            @Override
            public void run() {
                if (GameServerState.instance == null || System.currentTimeMillis()<= last+wait) { //once every x millis
                    return;
                }
                last = System.currentTimeMillis();

                //garbage collection
                if (!this.equals(updater))
                    cancel();
                //System.out.println("Server-Client synch for WarpProcess");
                //is server(implicit)
                LinkedList<PlayerState> disconnected = new LinkedList<>();
                for (PlayerState p: player_to_processArr.keySet())
                    if (GameServerState.instance.getClients().containsKey(p.getClientId()) &&
                     GameServerState.instance.getPlayerStatesByName().containsKey(p.getName()))
                        synchToClient(p);
                    else {
                        disconnected.add(p);
                    }

                for (PlayerState p: disconnected) {
                    player_to_processArr.remove(p);
                }
            }
        };
        updater.runTimer(WarpMain.instance,1);
    }

    /**
     * set process to this value for this player. safe usage, autocreates entries if needed. DOES NOT AUTO SYNCH!
     *
     * @param p
     * @param wp
     * @param value
     */
    public static void setProcess(PlayerState p, WarpProcess wp, long value) {
        long[] arr;
        if (!player_to_processArr.containsKey(p)) {
            arr = new long[values().length];
            player_to_processArr.put(p, arr);
        } else {
            arr = player_to_processArr.get(p);
        }
        if (arr[wp.ordinal()]!=value) {
            arr[wp.ordinal()] = value;
            ModPlayground.broadcastMessage("set process "+wp.name() + " to " + value + " for player "+ p.getName());
        }

    }

    /**
     * send update with all warpprocess values of this player to the players machine
     * auto handles localhost updating the hostplayer
     * @param p playerstate player
     */
    public static void synchToClient(PlayerState p) {
        if (!player_to_processArr.containsKey(p))
            return;
        //FIXME PacketUtil.getServerProcessor throws nullpointer here sometimes
        preSynchServer(p);

        //System.out.println("synch client"+p.getName());
        if (GameClientState.instance != null && GameClientState.instance.getPlayer().equals(p)) {
            //local host -> client, skip network
            update(player_to_processArr.get(p));
            postSynchServer(player_to_processArr.get(p));
        } else { //server machine->network->client
            PacketHUDUpdate packet = new PacketHUDUpdate(player_to_processArr.get(p));
            PacketUtil.sendPacket(p, packet);
        }
    }

    private static void preSynchServer(PlayerState p) {
        //handle values that are always updated
        ExtraEventLoop.updatePlayer(p);
    }

    public static long[] toArray() {
        long[] out = new long[values().length];
        for (int i = 0; i < values().length; i++) {
            out[i] = values()[i].getCurrentValue();
        }
        return out;
    }

    /**
     * update the "map" with values from these arrays, will auto fire events AFTER ALL values were set.
     */
    public static void update(long[] arr) {
        //System.out.println("Update");
        assert arr.length == values().length;
        for (int i = 0; i < arr.length; i++)
            if (!values()[i].clientOnly) //ignore some stuff thats handled by the client
                values()[i].setCurrentValue(arr[i]); //auto adds process to changed values if value is different
        postSynchClient();
        for (WarpProcess wp : changedValues) {
            //System.out.println("Value changed: "+wp);
            for (WarpProcessListener l : wp.listeners) {
                l.onValueChange(wp);
            }

        }
        changedValues.clear();

    }

    public static void postSynchServer(long[] arr) {
        //rest values that have the "resetAfterUpdate" flag
        for (WarpProcess wp: values())
            if (wp.resetAfterUpdate && arr[wp.ordinal()] != 0)
                arr[wp.ordinal()] = 0;
    }

    public static void postSynchClient() {
        //test if beacon is affecting player position, beacon synch is handeled separately.
        boolean droppointShifted = (WarpJumpManager.isDroppointShifted(WarpManager.getWarpSpacePos(GameClientState.instance.getPlayer().getCurrentSector())));
        System.out.println("DROPPOINT IS SHIFTED: " + droppointShifted);
        WarpProcess.DROPPOINTSHIFTED.setCurrentValue(droppointShifted?1:0);
        WarpProcess.IS_IN_WARP.setCurrentValue(WarpManager.isInWarp(GameClientState.instance.getPlayer().getCurrentSector())?1:0);

    }

    private long currentValue = 0; //TODO is byte sufficient, maybe use long instead?
    private long previousValue = 0;
    private LinkedList<WarpProcessListener> listeners = new LinkedList();

    public long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(long currentValue) {
        previousValue = this.currentValue;
        if (currentValue != this.currentValue) {
            changedValues.add(this);
            System.out.println("[CLIENT] set process "+this.name()+" from " + this.currentValue + " to " + currentValue);
            this.currentValue = currentValue;
        }
    }

    public long getPreviousValue() {
        return previousValue;
    }

    //TODO allow listener that reacts to ALL

    public void addListener(WarpProcessListener listener) {
        listeners.add(listener);
    }

    public void removeListener(WarpProcessListener listener) {
        listeners.remove(listener);
    }

    /**
     * jank way to convert current value to bool
     * @return
     */
    public boolean isTrue() {
        return currentValue==1;
    }

    /**
     * jank way to convert previous value to bool
     * @return
     */
    public boolean wasTrue() {
        return previousValue == 1;
    }

    @Override
    public String toString() {
        return "WarpProcess{" +
                "name="+ this.name() +
                ", currentValue=" + currentValue +
                ", previousValue=" + previousValue +
                '}';
    }

    /*
    class PlayerProcessMap {
        long[] process = new long[WarpProcess.values().length];
        boolean[] changed = new boolean[WarpProcess.values().length];

        private void setValue(WarpProcess wp, long value) {
            process[wp.ordinal()] = value;
            changed[wp.ordinal()] = true;
        }

        public void synchToClient() {

            //after synch
            changed = new boolean[WarpProcess.values().length];
        }
    } */
}
