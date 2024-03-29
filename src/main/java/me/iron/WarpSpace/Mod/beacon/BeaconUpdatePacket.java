package me.iron.WarpSpace.Mod.beacon;

import java.io.IOException;

import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import me.iron.WarpSpace.Mod.WarpMain;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 26.10.2021
 * TIME: 21:58
 */
public class BeaconUpdatePacket extends Packet {
    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        BeaconManager client = WarpMain.instance.beaconManagerClient;
        if (client == null)
            return;
        client.onDeserialize(packetReadBuffer);
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        WarpMain.instance.beaconManagerServer.onSerialize(packetWriteBuffer);
    }

    @Override
    public void processPacketOnClient() {
        //handled in read data
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
    }

    public void sendToAll() {
        for (PlayerState playerState: GameServerState.instance.getPlayerStatesByName().values()) {
            PacketUtil.sendPacket(playerState,this);
        }
    }
}
