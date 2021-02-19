package me.iron.WarpSpace.Mod.HUD.client;

import me.iron.WarpSpace.Mod.WarpMain;
import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;

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
                if (e.getText().contains("movehud")) { //movehud 1,-1
                    //reduce string to first int
                    String s = e.getText();
                   s = s.replace("movehud ",""); //remove movehud
                   s = s.replace(" ",""); //remove space
                   DebugFile.log("after removing, string is: " + s);
                   String[] parts = s.split(",");
                   int x = Integer.parseInt(parts[0]);
                   int y = Integer.parseInt(parts[1]);
                   DebugFile.log("x and y are:" + new Vector2d(x,y).toString());
                   moveHUDElement(x,y);
                }

            }

        }, WarpMain.instance);
    }
    public static TextElement textElement;
    private static void moveHUDElement(int x, int y) {
        textElement.setPos(textElement.getPos().x + x,textElement.getPos().y + y,0);
        ModPlayground.broadcastMessage("text el is now at: " + textElement.getPos().toString());
        DebugFile.log("text el is now at: " + textElement.getPos().toString());

    }
}
