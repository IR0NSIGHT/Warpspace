package Mod.server.interdiction;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 31.12.2020
 * TIME: 17:11
 */

import Mod.HUD.client.WarpProcessController;
import Mod.PacketHUDUpdate;
import Mod.WarpMain;
import Mod.WarpManager;
import api.DebugFile;
import api.common.GameServer;
import api.network.packets.PacketUtil;
import api.utils.StarRunnable;
import org.newdawn.slick.Game;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.RegisteredClientOnServer;

/**
 * class handles and controls the synching of information about inhibited sectors between server and client.
 * only required info is given to the client (his sector and partner sector) to avoid stealing of info through hacks.
 */
public class SynchManager {
    /**
     * creates loop that regualry sends updates to all connected clients
     */
    public static void SynchLoop() {
        new StarRunnable() {
            @Override
            public void run() {
                //get all connected clients
                //update each client with inhibition info for sector they are in.
                PlayerState p;
                for (RegisteredClientOnServer client: GameServer.getServerState().getClients().values()) {
                    p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(client.getPlayerName()); //TODO ugly, find better method
                    if (p != null) {
                        DebugFile.log("trying to update client: " + p.getName() + " with inhibition info.");
                        UpdateClient(p);
                    }
                }
            }
        }.runTimer(WarpMain.instance,15);
    }

    /**
     * collects relevent info, creates packet, sends packet.
     * send info about sector/noExit and partner/noEntry
     * @param player
     */
    private static void UpdateClient (PlayerState player) { //TODO allow client to see all inihibiton info about sector+partner
        if (player == null) {
            DebugFile.log("could not send inhibition update to client, is null");
            return;
        }
        //collect required information
        Vector3i sector = player.getCurrentSector();
        Vector3i partnerS = WarpManager.GetPartnerPos(sector);
        //get inhibition from both sectors
        SectorManager.UpdateSectorInhibition(sector);
        SectorManager.UpdateSectorInhibition(partnerS);
        //if sector noExit -> decide if inwarp or not
        Long sectorID = SectorManager.SectorToID(sector);
        Long partnerID = SectorManager.SectorToID(partnerS);
        boolean sectorNoExit = SectorManager.GetSectorStatus(sectorID, SectorManager.InterdictionState.noExit);
        boolean partnerNoEntry = SectorManager.GetSectorStatus(partnerID, SectorManager.InterdictionState.noEntry); //TODO get as boolean
        int sectorVal = 0;
        int partnerVal = 0;
        if (sectorNoExit) {
            sectorVal = 1;
        }
        if (partnerNoEntry) {
            partnerVal = 1;
        }
        DebugFile.log("partner of " + sector.toString() + " is " + partnerS.toString());
        DebugFile.log("partner inhibition noEntry is: " + partnerNoEntry);
        DebugFile.log("value in packet of noEntry: " + partnerVal);
        //create packet
        PacketHUDUpdate packetSector = new PacketHUDUpdate(WarpProcessController.WarpProcess.SECTOR_NOEXIT,sectorVal);
        PacketHUDUpdate packetPartner = new PacketHUDUpdate(WarpProcessController.WarpProcess.PARTNER_NOENTRY,partnerVal);


        PacketUtil.sendPacket(player,packetSector);
        PacketUtil.sendPacket(player,packetPartner);

        //send packet
    }
}
