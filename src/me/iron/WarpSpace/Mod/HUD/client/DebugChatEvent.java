package me.iron.WarpSpace.Mod.HUD.client;

import me.iron.WarpSpace.Mod.WarpMain;
import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3f;

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

        //DebugFile.log("player chat event debug eventhandler added");
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent e) {
               //DebugFile.log("playerchat event"); //FIXME debug
               if (!e.isServer()) {
                   ModPlayground.broadcastMessage("is not on server");
                   return;
               }
               if (e.getText().contains("domove")) { //move to absolute pixelpos
                   Integer[] integers = parseText(e.getText(),"domove",",");
                   if (integers.length < 4) {
                       return;
                   }
                   Vector3f newPos = ScreenHelper.pixelPosToRelPos(new Vector3f(integers[0],integers[1],integers[2]),false);
                   switch (integers[3]) {
                       case 0:  //pos
                           HUD_core.interdictionShip.setTextElementOffset(newPos,false);
                            break;
                       case 1:  //scale
                           HUD_core.interdictionFaction.setTextElementOffset(newPos,false);
                           break;
                       case 2:  //text offset
                           HUD_core.interdictionSector.setTextElementOffset(newPos,false);
                           break;

                   }
                   //DebugFile.log("newPos: " + newPos.toString());
                   ModPlayground.broadcastMessage("newPos" + newPos.toString());
                   //HUD_element[] arr = new HUD_element[] {HUD_core.interdictionBox}; //position groups
                   //for (HUD_element element : arr) {
                   //    element.setPos(newPos);
                   //    element.toString();
                   //}
               }

            }

        }, WarpMain.instance);
    }
    public static TextElement textElement;

    public static Integer[] parseText(String text, String keyword, String separator) {
        if (!text.contains(keyword)) {
            DebugFile.log("text does not contain keyword.");
            return null;
        }

        String s = text;
        s = s.replace(keyword,""); //remove keyword
        s = s.replace(" ",""); //remove space
        //DebugFile.log("pasreText: after removing keyword " + keyword + ", string is: " + s);
        String[] parts = s.split(separator);
        Integer[] arr = new Integer[parts.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(parts[i]);
        }
        //DebugFile.log("integer[] is: " + arr.toString());
        return arr;
    }
}
