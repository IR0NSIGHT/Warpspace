package me.iron.WarpSpace.Mod.HUD.client;

import api.DebugFile;
import me.iron.WarpSpace.Mod.WarpMain;
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
                StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>() {
            @Override
            public void onEvent(HudCreateEvent hudCreateEvent) {
                WarpHUDPanel whp = new WarpHUDPanel(hudCreateEvent);


                //register all HUD elements

                for (HUD_element el: HUD_core.elementList) {
                    DebugFile.log("GUI eventhandler creating CustomHUDImages for element: " + el.enumValue.name +"at" + el.pos.toString());
                    hudCreateEvent.addElement(new CustomHudImage(hudCreateEvent.getInputState(),el.pos,el.scale,el));
                }

            }

        }, WarpMain.instance);
    }

}
