package Mod.HUD.client;

import Mod.WarpMain;
import api.DebugFile;
import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.mod.StarLoader;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.10.2020
 * TIME: 22:44
 */
public class GUIeventhandler {
    public static void addHUDDrawListener() {

        DebugFile.log("method called, registering HUDCreateEvent listener");
        StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>() {
            @Override
            public void onEvent(HudCreateEvent hudCreateEvent) {
                WarpHUDPanel whp = new WarpHUDPanel(hudCreateEvent);

            }

        }, WarpMain.instance);
    }

}
