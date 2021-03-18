package me.iron.WarpSpace.Mod.HUD.client;

import api.DebugFile;
import me.iron.WarpSpace.Mod.WarpMain;
import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.mod.StarLoader;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;

import javax.vecmath.Vector3f;

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

                //register all HUD elements
                Vector3f moveStep = new Vector3f(1,1,0); //placehold. is synched between all elements of same type
                for (HUD_element el: HUD_core.elementList) {
                    if (el.enumValue != null) {
                        //DebugFile.log("GUI eventhandler creating CustomHUDImages for element: " + el.toString());
                        hudCreateEvent.addElement(new CustomHudImage(hudCreateEvent.getInputState(),el));
                    };
                    el.setTextElement(new TextElement(FontLibrary.getBlenderProHeavy20(), hudCreateEvent.getInputState()));
                    hudCreateEvent.addElement(el.getTextElement());
                    //el.getTextElement().text = "";
                }
                HUD_core.interdictionBox.getTextElement().text = "I AM INTERDICTION!";
                //DebugFile.log("inhFaction text el has text: " + HUD_core.interdictionFaction.getTextElement().text);
            }

        }, WarpMain.instance);
    }

}
