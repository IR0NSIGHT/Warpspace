package me.iron.WarpSpace.Mod.network;

import java.io.IOException;

import org.schema.game.common.data.player.PlayerState;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import me.iron.WarpSpace.Mod.client.HUD_core;
import me.iron.WarpSpace.Mod.client.WarpProcess;

/**
 * packet to send a vector3i to a client and set it as the clients navigation waypoint
 * send all important information from server to client
 * edited by Ir0nsight
 * made by jake
 */
public class PacketHUDUpdate extends Packet {
    /**
     * value given process has: 1 happening, 0 not happening
     */
    private long[] arr;

    /**
     * constructor
     * @param processArray String list that allows input of extra info to be displayed. currently not used.
     */
    public PacketHUDUpdate(long[] processArray) {
        arr = processArray;
    }

    /**
     * default constructor required by starlaoder DO NOT DELETE!
     */
    public PacketHUDUpdate() {

    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeInt(arr.length);
        for (long l: arr) {
            buf.writeLong(l);
        }
    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        int length = buf.readInt();
        arr = new long[length];
        for (int i = 0; i < length; i++) {
            arr[i]=buf.readLong();
        }
    }



    @Override
    public void processPacketOnClient() {
        //set players process "map" (enum)
        WarpProcess.update(arr);
        HUD_core.UpdateHUD();
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {
        //not intended in this direction.
    }

    @Override
    public String toString() {
        return "PacketHUDUpdate{" +
                "arr=" + arr +
                '}';
    }
}