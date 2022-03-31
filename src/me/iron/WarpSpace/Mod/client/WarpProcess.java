package me.iron.WarpSpace.Mod.client;

import api.network.packets.PacketUtil;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.WarpMain;
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
            setProcess(p, wp, value); //TODO switch to use long
    }

    public static void initUpdateLoop() {
        updater = new StarRunnable() {
            final int wait = 500; //millis
            long last;
            @Override
            public void run() {
                if (System.currentTimeMillis()<= last+wait) { //once every x millis
                    return;
                }

                //garbage collection
                if (!this.equals(updater))
                    cancel();

                //is server(implicit)
                for (PlayerState p: player_to_processArr.keySet())
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
        if (player_to_processArr.containsKey(p)) {
            arr = new long[values().length];
            player_to_processArr.put(p, arr);
        } else {
            arr = player_to_processArr.get(p);
        }
        arr[wp.ordinal()] = value;
    }

    /**
     * send update with all
     *
     * @param p playerstate player
     */
    public static void synchToClient(PlayerState p) {
        //make packet with current warpprocess values of player, send it to players client
        if (!player_to_processArr.containsKey(p))
            return;
        PacketHUDUpdate packet = new PacketHUDUpdate(player_to_processArr.get(p));
        PacketUtil.sendPacket(p, packet);
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
        assert arr.length == values().length;
        for (int i = 0; i < arr.length; i++)
            values()[i].setCurrentValue(arr[i]);

        for (WarpProcess wp : changedValues)
            for (WarpProcessListener l : wp.listeners)
                l.onValueChange(wp);
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
}
