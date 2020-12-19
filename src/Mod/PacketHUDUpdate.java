package Mod;

import Mod.HUD.client.HUD_core;
import api.common.GameClient;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;

/**
 * packet to send a vector3i to a client and set it as the clients navigation waypoint
 * send all important information from server to client
 * edited by Ir0nsight
 * made by jake
 */
public class PacketHUDUpdate extends Packet {
    private HUD_core.WarpSituation situation;

    //TODO allow multiple HUD updates in one packet
    /**
     * constructor
     * @param situation enum describing what warpjump state the player is in.
     */
    public PacketHUDUpdate(HUD_core.WarpSituation situation) {
        this.situation = situation;
    }
    public PacketHUDUpdate(){

    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        situation = HUD_core.WarpSituation.valueOf(buf.readInt());
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeInt(situation.ordinal());
    }

    @Override
    public void processPacketOnClient() {
        HUD_core.HUD_processPacket(situation);
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}