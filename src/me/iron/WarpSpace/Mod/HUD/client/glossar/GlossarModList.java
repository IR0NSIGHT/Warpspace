package me.iron.WarpSpace.Mod.HUD.client.glossar;

import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIResizableElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.12.2021
 * TIME: 20:55
 */
public class GlossarModList extends GUIColoredRectangle {
    public GlossarModList(InputState inputState, float v, float v1, Vector4f vector4f) {
        super(inputState, v, v1, vector4f);

    }

    public GlossarModList(InputState inputState, float v, float v1, GUIResizableElement guiResizableElement, Vector4f vector4f) {
        super(inputState, v, v1, guiResizableElement, vector4f);
    }

    private boolean firstDraw = true;
    @Override
    public void draw() {
        if (firstDraw) {
            onInit();
            firstDraw = false;
        }
        super.draw();
    }

    @Override
    public void onInit() {
        super.onInit();
        //make text
        GUITextOverlay textOverlay = new GUITextOverlay((int) getWidth(),(int)getHeight(),getState());
        textOverlay.setTextSimple("i am a text simple textoverlay he he");

        //make scrollable panel
        GUIScrollablePanel panel = new GUIScrollablePanel((int)getWidth(),(int)getHeight()*0.9f,this,getState());
        panel.setContent(textOverlay);

        //attach everything to the tab/background
        this.attach(panel);
    }

}
