package testing;

import Mod.HUD.client.TextElement;
import Mod.WarpMain;
import Mod.server.interdiction.EnvironmentManager;
import Mod.server.interdiction.SectorManager;
import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerState;

import javax.vecmath.Vector2d;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 21.12.2020
 * TIME: 14:27
 */

/**
 * runs clientside for HUD arranging on runtime
 */
public class DebugChatEvent {
    public static void addDebugChatListener() {

        DebugFile.log("player chat event debug eventhandler added");
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent e) {
                DebugFile.log("playerchat event"); //FIXME debug
                if (!e.isServer()) {
                    ModPlayground.broadcastMessage("is not on server");
                    return;
                }
                //get player vessel
                PlayerState player = GameServer.getServerState().getPlayerFromNameIgnoreCaseWOException(e.getMessage().sender);
                if (player == null) {
                    ModPlayground.broadcastMessage("player is null!");
                }

                if (e.getText().contains("movehud")) { //movehud 1,-1
                    String s = e.getText();
                    HUDElementStringOp(s); //move hud element after factoring string
                }

                if (e.getText().contains("inhibit")) {
                    ModPlayground.broadcastMessage("sector " + player.getCurrentSector() + " has Id" + SectorManager.SectorToID(player.getCurrentSector()) + " with inihibition:");
                    Long id = SectorManager.SectorToID(player.getCurrentSector());
                    for (SectorManager.InterdictionState state: SectorManager.InterdictionState.values()) {
                        ModPlayground.broadcastMessage(state.toString() + ": " + SectorManager.GetSectorStatus(id,state));
                    }
                }
                if (e.getText().contains("natureboy")) {
                    //get sector type

                    String s = "natural inhibition of sector -> void:" +  EnvironmentManager.IsVoidInhibition(player.getCurrentSector());
                }

            }

        }, WarpMain.instance);
    }
    public static TextElement textElement;

    private static void HUDElementStringOp(String s) {
        //reduce string to first int

        s = s.replace("movehud ",""); //remove movehud
        s = s.replace(" ",""); //remove space
        DebugFile.log("after removing, string is: " + s);
        String[] parts = s.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        DebugFile.log("x and y are:" + new Vector2d(x,y).toString());
        moveHUDElement(x,y);
    }
    private static void moveHUDElement(int x, int y) {
        textElement.setPos(textElement.getPos().x + x,textElement.getPos().y + y,0);
        ModPlayground.broadcastMessage("text el is now at: " + textElement.getPos().toString());
        DebugFile.log("text el is now at: " + textElement.getPos().toString());

    }
}
