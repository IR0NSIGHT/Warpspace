package me.iron.WarpSpace.Mod.HUD.client;

import org.newdawn.slick.Color;
import org.newdawn.slick.UnicodeFont;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 26.10.2020
 * TIME: 21:02
 * blatantly stolen from pilotelement of star api
 */
public class TextElement extends GUITextOverlay {
    public TextElement(UnicodeFont unicodeFont, InputState inputState) {
        super(50, 30, unicodeFont, Color.green, inputState);
    }


    public String text = "";
    public HUD_element parent;
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
        if (parent == null) {
            return;
        }
        setPos(parent.getTextElementPxPos());
        text = parent.getText();
        super.draw();
    }
}
