package me.iron.WarpSpace.Mod.HUD.client;

import api.config.BlockConfig;
import api.mod.config.PersistentObjectUtil;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.iron.WarpSpace.Mod.WarpMain;
import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.WarpSpace.Mod.beacon.BeaconObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import javax.vecmath.Vector3f;
import java.util.ArrayList;

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
               if (e.isServer()) {
               //    ModPlayground.broadcastMessage("is on server");
                   if (e.getText().contains("beacon")) {
                       WarpMain.instance.beaconManagerServer.addBeacon(new BeaconObject(

                            new Vector3i(2,2,2),
                            true,
                            "TEST BEACON UID",
                            -1,
                            100,
                            SimpleTransformableSendableObject.EntityType.ASTEROID,
                            true,
                            "UWU EMPIRE NOOB TRAP",
                            "UWU EMPIRE"

                       ));
                   }

                   if (e.getText().contains("list")) {
                       WarpMain.instance.beaconManagerServer.print();
                   }

                   if (e.getText().contains("save")) {
                       PersistentObjectUtil.save(WarpMain.instance.getSkeleton());
                   }

                   if (e.getText().contains("key")) {
                       ElementInformation ei = ElementKeyMap.getInfo(1203);
                       Short2ObjectOpenHashMap map = ElementKeyMap.informationKeyMap;
                       int size = map.size();
                       ArrayList<ElementInformation> blocks = BlockConfig.getElements();

                       String s = "uwu";
                   }
                   return;
               }
               DebugFile.log("doing sth in chat listener");
               if (e.getText().contains("domove")) { //move to absolute pixelpos
                   ModPlayground.broadcastMessage("doing move for all HUD stuff");
                   Integer[] integers = parseText(e.getText(),"domove",",");
                   if (integers.length < 4) {
                       return;
                   }
                   Vector3f newPos = ScreenHelper.pixelPosToRelPos(new Vector3f(integers[0],integers[1],integers[2]),false);
                   DebugFile.log("newPos: " + newPos.toString());
                   if (integers[3] == 0) { //move box
                       HUD_element[] arr = new HUD_element[] {HUD_core.console}; //position groups
                       for (HUD_element element : arr) {
                           element.setPos(newPos);
                           element.toString();
                       }
                       DebugFile.log("moving element to " + newPos.toString());
                   } else { //move text
                       HUD_core.interdictionBox.setTextElementOffset(newPos,false);
                   }

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
        DebugFile.log("pasreText: after removing keyword " + keyword + ", string is: " + s);
        String[] parts = s.split(separator);
        Integer[] arr = new Integer[parts.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(parts[i]);
        }
        DebugFile.log("integer[] is: " + arr.toString());
        return arr;
    }
}
