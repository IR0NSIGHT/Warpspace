import api.common.GameClient;
import api.element.gui.custom.CustomHudText;
import api.element.gui.custom.examples.PilotElement;
import api.entity.StarEntity;
import api.entity.StarPlayer;
import org.newdawn.slick.Color;
import org.newdawn.slick.UnicodeFont;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 26.10.2020
 * TIME: 21:02
 * blatantly stolen from pilotelement of star api
 */
public class TextElement extends GUITextOverlay {
    public TextElement(UnicodeFont unicodeFont, InputState inputState) {
        super(100, 20, unicodeFont, Color.green, inputState);
    }


    public String text = "";
    @Override
    public void onInit() {
        super.onInit();
        setTextSimple(new Object(){
            @Override
            public String toString() {
                return text;
            }
        });
    }

    @Override
    public void draw() {
     //   text = "system time " + System.currentTimeMillis();
        super.draw();
    }
}
