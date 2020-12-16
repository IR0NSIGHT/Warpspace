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

        DebugFile.log("method called, registering HUDCreateEvent listener");
        StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>() {
            @Override
            public void onEvent(HudCreateEvent hudCreateEvent) {
                WarpHUDPanel whp = new WarpHUDPanel(hudCreateEvent);
                DebugFile.log("trying to add sprite element to HUD"); //FIXME debug

                //register all HUD elements
                DebugFile.log("hud_core element list has: " + HUD_core.elementList.size() + "entries.");
                for (HUD_element el: HUD_core.elementList) {
                    //DebugFile.log("element" + el.sprite.getName());
                    hudCreateEvent.addElement(new CustomHudImage(hudCreateEvent.getInputState(),el.pos,el.scale,el.sprite));
                }

            }

        }, WarpMain.instance);
    }

}
