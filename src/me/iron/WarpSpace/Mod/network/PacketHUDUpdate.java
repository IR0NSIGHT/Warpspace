package me.iron.WarpSpace.Mod.network;

import api.DebugFile;
import me.iron.WarpSpace.Mod.HUD.client.HUD_core;
import me.iron.WarpSpace.Mod.HUD.client.WarpProcessController;
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
     * what process
     */
    private WarpProcessController.WarpProcess warpProcess;

    /**
     * value given process has: 1 happening, 0 not happening
     */
    private Integer processValue;
    private List<String> processArray; //for more detailed info to display (inhibitor name f.e.)

    //TODO allow multiple HUD updates in one packet
    /**
     * constructor
     * @param warpProcess enum describing what warpjump state the player is in.
     * @param processValue what value process has (1: active, 0: inactive)
     */
    public PacketHUDUpdate(WarpProcessController.WarpProcess warpProcess, Integer processValue, List<String> processArray) {
        this.warpProcess = warpProcess;
        this.processValue = processValue;
        this.processArray = processArray;
        //DebugFile.log("sending HUD package to client with " + this.toString());
    }

    @Override
    public String toString() {
        return "PacketHUDUpdate{" +
                "warpProcess=" + warpProcess +
                ", processValue=" + processValue +
                ", processArray=" + processArray.toString() +
                '}';
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
        processArray = buf.readStringList();
        //DebugFile.log("packet reading" + this.toString());
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeInt(warpProcess.ordinal());
        buf.writeInt(processValue);
        buf.writeStringList(processArray);
        //DebugFile.log("packet writing" + this.toString());
    }

    @Override
    public void processPacketOnClient() {
        //DebugFile.log("packet class process packet on client " + this.toString());
        HUD_core.HUD_processPacket(warpProcess,processValue, processArray);
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}