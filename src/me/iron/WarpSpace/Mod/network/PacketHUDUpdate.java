package me.iron.WarpSpace.Mod.network;

import me.iron.WarpSpace.Mod.HUD.client.HUD_core;
import me.iron.WarpSpace.Mod.HUD.client.WarpProcessController;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;

/**
 * packet to send a vector3i to a client and set it as the clients navigation waypoint
 * send all important information from server to client
 * edited by Ir0nsight
 * made by jake
 */
public class PacketHUDUpdate extends Packet {
    /**
     * what process
     */
    private WarpProcessController.WarpProcess warpProcess;

    /**
     * value given process has: 1 happening, 0 not happening
     */
    private Integer processValue;

    //TODO allow multiple HUD updates in one packet
    /**
     * constructor
     * @param warpProcess enum describing what warpjump state the player is in.
     */
    public PacketHUDUpdate(WarpProcessController.WarpProcess warpProcess, Integer processValue) {
        this.warpProcess = warpProcess;
        this.processValue = processValue;
    }

    /**
     * default constructor required by starlaoder DO NOT DELETE!
     */
    public PacketHUDUpdate() {

    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        warpProcess = WarpProcessController.WarpProcess.valueOf(buf.readInt());
        processValue = buf.readInt();
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeInt(warpProcess.ordinal());
        buf.writeInt(processValue);
    }

    @Override
    public void processPacketOnClient() {
        HUD_core.HUD_processPacket(warpProcess,processValue);
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}