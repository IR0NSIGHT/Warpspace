import api.DebugFile;
import api.ModPlayground;
import api.common.GameClient;
import api.element.gui.custom.examples.BasicInfoGroup;
import api.element.gui.custom.examples.BasicInfoPanel;
import api.element.gui.custom.examples.CurrentEntityReactorBar;
import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.mod.StarLoader;
import org.newdawn.slick.Game;
import org.schema.common.util.linAlg.Vector3i;
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
                whp.setPosition(200,200);
            }

        },main.instance);
    }

}
