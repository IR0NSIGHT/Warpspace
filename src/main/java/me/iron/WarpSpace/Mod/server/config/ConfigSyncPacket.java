package me.iron.WarpSpace.Mod.server.config;

import java.io.IOException;

import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import me.iron.WarpSpace.Mod.WarpManager;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.09.2022
 * TIME: 20:50
 */
public class ConfigSyncPacket extends Packet {
    /**
     * default constructor required by starlaoder DO NOT DELETE!
     */
    public ConfigSyncPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        for (int i = 0; i < ConfigManager.ConfigEntry.values().length; i++) {
            String path = packetReadBuffer.readString();
            ConfigManager.ConfigEntry e = ConfigManager.ConfigEntry.getEntryByPath(path);
            float value = packetReadBuffer.readFloat();
            if (e.isOverwriteClient())
                e.setValue(value);
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        for (ConfigManager.ConfigEntry e: ConfigManager.ConfigEntry.values()) {
                packetWriteBuffer.writeString(e.getPath());
                packetWriteBuffer.writeFloat(e.getValue());
        }
    }

    @Override
    public void processPacketOnClient() {
        int value = (int)ConfigManager.ConfigEntry.warp_to_rsp_ratio.getValue();
        WarpManager.getInstance().setScale(value);
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {

    }

    public void sendToAllClients() {
        for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values())
            PacketUtil.sendPacket(p, this);
    }
}
