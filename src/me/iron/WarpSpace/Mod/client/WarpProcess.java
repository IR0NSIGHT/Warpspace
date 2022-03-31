package me.iron.WarpSpace.Mod.client;

import api.ModPlayground;
import api.network.packets.PacketUtil;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.network.PacketHUDUpdate;
import org.lwjgl.Sys;
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
    JUMPDROP,
    JUMPEXIT,
    JUMPENTRY,
    JUMPPULL,
    TRAVEL,

    SECTOR_NOEXIT,
    SECTOR_NOENTRY,
    PARTNER_NOEXIT,
    PARTNER_NOENTRY,
    IS_IN_WARP,
    IS_INHIBITED,
    WARP_STABILITY,
    HAS_JUMPED;
    private static LinkedList<WarpProcess> changedValues = new LinkedList<>();
    //holds wp values for each player
    private static HashMap<PlayerState, long[]> player_to_processArr = new HashMap<>();
    private static StarRunnable updater;
    public static void setProcess(SegmentController sc, WarpProcess wp, long value) {
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
                for (PlayerState p: player_to_processArr.keySet())
                    if (GameServerState.instance.getPlayerStatesByName().containsKey(p.getName()))
                        synchToClient(p);

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

        //handle values that are always updated
        setProcess(p,IS_IN_WARP, WarpManager.isInWarp(p.getCurrentSector())?1:0);
        //System.out.println("synch client"+p.getName());
        if (GameClientState.instance != null && GameClientState.instance.getPlayer().equals(p)) {
            //local host -> client, skip network
            update(player_to_processArr.get(p));
        } else { //server machine->network->client
            PacketHUDUpdate packet = new PacketHUDUpdate(player_to_processArr.get(p));
            PacketUtil.sendPacket(p, packet);
        }
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
            values()[i].setCurrentValue(arr[i]); //auto adds process to changed values if value is different

        for (WarpProcess wp : changedValues) {
            //System.out.println("Value changed: "+wp);
            for (WarpProcessListener l : wp.listeners)
                l.onValueChange(wp);
        changedValues.clear();
        }
    }

    private long currentValue = 0; //TODO is byte sufficient, maybe use long instead?
    private long previousValue = 0;
    private LinkedList<WarpProcessListener> listeners = new LinkedList();

    public long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(long currentValue) {
        if (currentValue != this.currentValue) {
            changedValues.add(this);
            previousValue = this.currentValue;
            this.currentValue = currentValue;
        }
    }

    public long getPreviousValue() {
        return previousValue;
    }

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
}
