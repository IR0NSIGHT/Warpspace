package Mod.HUD.client;

import Mod.WarpMain;
import api.DebugFile;
import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.mod.StarLoader;
import org.schema.game.client.data.GameClientState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.10.2020
 * TIME: 22:44
 */
public class GUIeventhandler {
    public static void addHUDDrawListener() {
                StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>() {
            @Override
            public void onEvent(HudCreateEvent hudCreateEvent) {
                WarpHUDPanel whp = new WarpHUDPanel(hudCreateEvent);


                //register all HUD elements

                for (HUD_element el: HUD_core.elementList) {
                    hudCreateEvent.addElement(new CustomHudImage(hudCreateEvent.getInputState(),el.pos,el.scale,el));
                }

            }

        }, WarpMain.instance);
    }

}
