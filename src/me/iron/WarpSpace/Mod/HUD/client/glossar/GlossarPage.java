package me.iron.WarpSpace.Mod.HUD.client.glossar;

import api.ModPlayground;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

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

    public GlossarPage(InputState inputState, int width, int height, String title, String text) {
        super(inputState, width, height, new Vector4f(0,0,0,0));
        ModPlayground.broadcastMessage("page has: w" + getWidth() + " ,h" + getHeight());
        this.text = text;
        this.title = title;
        onInit();
    }

    @Override
    public void onInit() {
        super.onInit();
        GUIColoredRectangle background = new GUIColoredRectangle(getState(),(int)(getWidth()),(int)(getHeight()),new Vector4f(0,0,1,0));

        titleText = new GUITextOverlay((int)getWidth(),5,GlossarControlManager.titleFont,getState());
        titleText.setTextSimple(new Object(){
            @Override
            public String toString() {
                if (GlossarPageList.getSelected() != null) {
                    return GlossarPageList.getSelected().getTitle();
                }
                return "";
            }
        });
        titleText.updateTextSize();
        titleText.setHeight(titleText.getTextHeight());
        titleText.autoWrapOn = background;
        background.attach(titleText);

        Vector2f clip = new Vector2f(0,(int)(background.getHeight()-titleText.getHeight()));


        contentText = new GUITextOverlay((int)getWidth(),5,GlossarControlManager.textFont,getState());

        contentText.setTextSimple(new Object(){
            @Override
            public String toString() {
                if (GlossarPageList.getSelected() != null) {
                    return GlossarPageList.getSelected().getContent();
                }
                return "";
            }
        });
        contentText.updateTextSize();

        contentText.autoWrapOn = background;
        contentText.autoHeight = true; //doesnt impact drawing limits

        GUIScrollablePanel contentScroll = new GUIScrollablePanel(background.getWidth(),clip.y,getState());
        contentScroll.setPos(0,titleText.getHeight(),0);

        contentScroll.setContent(contentText);

        background.attach(contentScroll);
        this.attach(background);

    }
}
