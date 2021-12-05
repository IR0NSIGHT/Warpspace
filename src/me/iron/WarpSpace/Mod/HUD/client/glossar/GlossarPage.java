package me.iron.WarpSpace.Mod.HUD.client.glossar;

import org.newdawn.slick.UnicodeFont;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.12.2021
 * TIME: 20:53
 */
public class GlossarPage extends GUIColoredRectangle {
    private String title;
    private String text;

    private GUITextOverlay titleText;
    private GUITextOverlay contentText;

    public GlossarPage(InputState inputState, GUIElement autoWrap, int width, int height, String title, String text) {
        super(inputState, width, height, new Vector4f(0,0,0,0));
        this.text = text;
        this.title = title;
        onInit();
    }

    @Override
    public void onInit() {
        super.onInit();
        GUIColoredRectangle background = new GUIColoredRectangle(getState(),(int)(getWidth()*0.98f),(int)(getHeight()-getWidth()*0.02f),new Vector4f(0,0,0,0));
        background.setPos(getWidth()*0.01f,getWidth()*0.01f,0);

        titleText = new GUITextOverlay((int)getWidth(),5,GlossarControlManager.titleFont,getState());
        titleText.setTextSimple(title);
        titleText.autoHeight = true;
        titleText.updateTextSize();
        titleText.autoWrapOn = background;

        background.attach(titleText);

        contentText = new GUITextOverlay((int)getWidth(),5,GlossarControlManager.textFont,getState());

        contentText.setTextSimple(text);
        contentText.updateTextSize();
        contentText.autoHeight = true;
        contentText.autoWrapOn = background;
        contentText.setPos(0,GlossarControlManager.titleFont.getLineHeight() *1.2f,0);
        background.attach(contentText);

        this.attach(background);

    }

    @Override
    public float getHeight() {
        if (titleText == null ||contentText == null)
            return 5;
        return titleText.getHeight() + contentText.getHeight();
    }
}
