package me.iron.WarpSpace.Mod.HUD.client;

import api.config.BlockConfig;
import api.mod.config.PersistentObjectUtil;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.iron.WarpSpace.Mod.HUD.client.glossar.GlossarControlManager;
import me.iron.WarpSpace.Mod.WarpMain;
import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.WarpSpace.Mod.beacon.BeaconManager;
import me.iron.WarpSpace.Mod.beacon.BeaconObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;

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
                if (e.getText().contains("hud")) {
                    GlossarControlManager.textFont = FontLibrary.getBlenderProHeavy20();
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
