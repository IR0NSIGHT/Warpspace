package me.iron.WarpSpace.Mod.HUD.client.glossar;

import api.ModPlayground;
import me.iron.WarpSpace.Mod.HUD.client.GlossarInit;
import org.schema.game.client.view.gui.GUIButton;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;
import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.12.2021
 * TIME: 21:11
 */
public class GlossarPageList extends GUIAncor {
    public GlossarPageList(float v, float v1, GUIResizableElement guiElement, InputState inputState) {
        super(inputState, v, v1);

    }
    public static void addGlossarCat(GlossarCatergory cat) {
        synchronized (categories) {
            categories.add(cat);
        }
    }

    private static final ArrayList<GlossarCatergory> categories = new ArrayList<>();
    private static GlossarEntry selected;
    public static GlossarEntry getSelected() {
        return selected;
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

    boolean init = true;
    @Override
    public void onInit() {
        if (init) {
            init = false;
            //initCats();
        }
        categories.clear();
        GlossarInit.addEntries();
        if (selected == null)
            selected = categories.get(0).getEntries().get(0);

        int border = (int)(getWidth() * 0.005f);

        //scrollable clickable list of mod pages
        GUIScrollablePanel pageOverview = new GUIScrollablePanel(getWidth()*0.275f-2*border,getHeight()-border, getState());
        pageOverview.setPos(border,border,0);
    //   GUIColoredRectangle dim = new GUIColoredRectangle(getState(),getWidth(),getHeight(),new Vector4f(0,1,0,1));
    //   attach(dim);
    //   ModPlayground.broadcastMessage("dim has x:" + dim.getWidth() + " y:" + dim.getHeight() + " pos: " + dim.getPos().toString()) ;
        GUIElementList pageList = new GUIElementList(getState());
        for (GlossarCatergory cat: categories) {

            //each category
            GUITextOverlay text = new GUITextOverlay((int) pageOverview.getWidth(),-1,GlossarControlManager.titleFont,getState());
            text.setTextSimple(cat.getName());
            text.updateTextSize();
            text.setHeight(text.getTextHeight());
            pageList.add(new GUIListElement(text,getState()));

            //add all entries of this cat
            for (final GlossarEntry kitten: cat.getEntries()) {
                GUIColoredRectangle back = new GUIColoredRectangle(getState(),pageOverview.getWidth(),10, new Vector4f(1,0,0,0));

                GUITextOverlay textX = new GUITextOverlay((int) pageOverview.getWidth(),-1,GlossarControlManager.textFont,getState());
                textX.setTextSimple("   "+kitten.getTitle());
                textX.updateTextSize();
                textX.setHeight(text.getTextHeight());
                back.setHeight(textX.getHeight());
                back.attach(textX);


                GUITextButton b= new GUITextButton(getState(), (int)back.getWidth(),(int)back.getHeight(), "", new GUICallback() {
                    @Override
                    public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                        if (mouseEvent.pressedLeftMouse()) {
                            selected = kitten;
                        }
                        //    ModPlayground.broadcastMessage(kitten.getTitle());
                    }

                    @Override
                    public boolean isOccluded() {
                        return false;
                    }
                });
                b.setPos(0,-0.5f*(back.getHeight()-textX.getTextHeight()),0);
                b.setColorPalette(GUITextButton.ColorPalette.TRANSPARENT);
                back.attach(b);
                pageList.add(new GUIListElement(back,getState()));
            }
        }

        pageList.updateDim();
        pageOverview.setContent(pageList);

        pageOverview.setScrollable(GUIScrollablePanel.SCROLLABLE_VERTICAL);

        GUIColoredRectangle pageOverviewBack = new GUIColoredRectangle(getState(),pageOverview.getWidth(),pageOverview.getHeight(),pageOverview,new Vector4f(1,0,0,0));
        pageOverviewBack.setPos(pageOverview.getPos());
        attach(pageOverviewBack);
        attach(pageOverview);

        //currently selected page with title and scrolalble textbox
        GlossarPage testpage = new GlossarPage(getState(),
                (int)(getWidth()-pageOverview.getWidth()-3*border), //TODO scroll bar occludes text behind it
                (int)getHeight()-border,
                "WarpSpace","");
        testpage.setPos(pageOverview.getWidth()+2*border,border,0);
        attach(testpage);
        super.onInit();

    }

    private void initCats() {
        categories.clear();
        for (int i = 0; i < 10; i++) {
            GlossarCatergory cat = new GlossarCatergory("CATEGORY " + i);
            categories.add(cat);
            for (int j = 0; j < 10; j++) {
                cat.addEntry(new GlossarEntry("TOPIC " + j,"owo"));
            }
        }
    }
}
