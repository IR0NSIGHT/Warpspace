package me.iron.WarpSpace.Mod.network;

import me.iron.WarpSpace.Mod.client.HUD_core;
import me.iron.WarpSpace.Mod.client.WarpProcessController;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;
import java.util.List;

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
    private byte[] arr;

    //TODO allow multiple HUD updates in one packet
    /**
     * constructor
     * @param warpProcess enum describing what warpjump state the player is in.
     * @param processValue what value process has (1: active, 0: inactive)
     * @param processArray String list that allows input of extra info to be displayed. currently not used.
     */
    public PacketHUDUpdate(int owo) {
        //DebugFile.log("sending HUD package to client with " + this.toString());
    }

    @Override
    public String toString() {
        return "PacketHUDUpdate{" +
                "arr=" + arr +
                '}';
    }

    /**
     * default constructor required by starlaoder DO NOT DELETE!
     */
    public PacketHUDUpdate() {

    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        byte[] arr = buf.readByteArray();

        //DebugFile.log("packet reading" + this.toString());
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeByteArray(arr);
        //DebugFile.log("packet writing" + this.toString());
    }

    @Override
    public void processPacketOnClient() {
        //DebugFile.log("packet class process packet on client " + this.toString());
        HUD_core.HUD_processPacket(arr);
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}