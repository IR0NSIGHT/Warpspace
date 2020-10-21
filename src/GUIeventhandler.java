import api.element.gui.custom.examples.BasicInfoGroup;
import api.element.gui.custom.examples.BasicInfoPanel;
import api.element.gui.custom.examples.CurrentEntityReactorBar;
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
                BasicInfoPanel bip = new BasicInfoPanel(hudCreateEvent);
                BasicInfoGroup bar = new BasicInfoGroup(hudCreateEvent);
                CurrentEntityReactorBar currentEntityReactorBar= new CurrentEntityReactorBar();
                hudCreateEvent.addElement(currentEntityReactorBar);
            }
        });
    }
}
