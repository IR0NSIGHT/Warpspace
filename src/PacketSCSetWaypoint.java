/*
import api.common.GameClient;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;

public class PacketSCSetWaypoint extends Packet {
    private boolean toWarp;
    public PacketSCSetWaypoint(boolean toWarp) {
        this.toWarp = toWarp;
    }
    public PacketSCSetWaypoint(){
        //why the empty constructor?
    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        toWarp = buf.readBoolean();
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeBoolean(toWarp);
    }

    @Override
    public void processPacketOnClient() {
        //calculate new waypoint
        Vector3i waypoint = navigationHelper.switchWaypoint(GameClient.getClientController().getClientGameData().getWaypoint(), toWarp)
        //overwrite waypoint
        GameClient.getClientController().getClientGameData().setWaypoint(waypoint);
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}  */