import api.common.GameClient;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;

/**
 * packet to send a vector3i to a client and set it as the clients navigation waypoint
 * made by jake
 */
public class PacketSCSetWaypoint extends Packet {
    private Vector3i waypoint;

    public PacketSCSetWaypoint(Vector3i waypoint) {
        this.waypoint = waypoint;
    }
    public PacketSCSetWaypoint(){

    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        waypoint = buf.readVector();
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeVector(waypoint);
    }

    @Override
    public void processPacketOnClient() {
        GameClient.getClientController().getClientGameData().setWaypoint(waypoint);
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}