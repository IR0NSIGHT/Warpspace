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
                "WarpSpace","an alternative way to faster-than-light travel in starmade.\n" +
                "\n" +
                "This mod explores the idea to have a space in which you travel to get from point a to point b, but faster than normal flight. The mod creates a warpspace, similar to the minecraft nether in concept. Every meter travelled in the warp translates into 10 meters travelled in realspace. So instead of teleporting from a to b (vanilla FTL), you change into the warp, fly the distance which is 10 times shorter, and drop back out of warp. Some core effects are:\n" +
                "\n" +
                "    no more instant travel, longer distances take longer time.\n" +
                "    a shared space where you can meet other travellers, be attacked or attack others (actual the likelyhood is increased by factor 1000 since 1 warpsector represents 10x10x10 realspace sectors.)\n" +
                "    you can now follow warping players, as the warp behaves similar to realspace in its flight dynamics.\n" +
                "    warp entry points are created (this is a sideeffect of the downscaling by factor 10). since all ships entering warp in a 10x10x10 sector cube end up in the same warpsector, all ships exiting the warp end up in the same realspace sector. This creates warpnodes, or travel routes where each starsystem has 4 nodes. Any ship entering the starsystem through warp will end up at one of them. These nodes could be defended or used for trade, taxing, piracy etc.\n" +
                "\n" +
                "Ingame behaviour/How to use\n" +
                "\n" +
                "    the mod will notice any FTL jump a ship performs. instead of arriving at your location, you will enter the warp.\n" +
                "    Your navmarker will be changed to its warp position as well. Just follow the marker to get to the correct position in warp.\n" +
                "    to drop out of warp you can either use your FTL drive again, or slow down to below 50m/s.\n" +
                "    FTL usage will drop you out of warp instantly, slowing down will give you a 10 second countdown and show you a warning.\n" +
                "    if you spawn a spacestation in warp, it will drop out to a random sector! thats wanted behaviour to prohibit warpcamping.\n" +
                "    astronauts will not drop out of warp automatically.\n" +
                "    if you want to avoid the warp completely, you can create Warpgates. they keep their vanilla behaviour and offer a way to travel instantly, precisely and safely.\n" +
                "\n" +
                "Beacons:\n" +
                "\n" +
                "https://github.com/IR0NSIGHT/Warpspace/blob/master/src/me/iron/WarpSpace/Mod/beacon/beaconHelp.md\n" +
                "Planned features are:\n" +
                "\n" +
                "    t interdiction to work (is ignored atm) done, even better than vanilla\n" +
                "    build custom HUD done\n" +
                "    make damaged ships automatically fall out of warp\n" +
                "    create means to force-pull warping ships out, like the Star-Wars interdictors.\n" +
                "    give warp custom visual effects to highlight the difference to realspace semi done with recolored backgrounds\n" +
                "    make the thrust strength (= possible travel speed) in warp depend on the FTL drives level waiting for SM update for hook\n" +
                "    explore means of making warp more interesting and different to realspace\n" +
                "        core principles here are that warp should not be just a smaller realspace 2.0 but behave differently\n" +
                "        make station building impossible (done, autodrop)\n" +
                "        shields not working\n" +
                "        a limited time in warp, where a counter autodrops the ship back out (using speed limit, done)\n" +
                "    Make AI/fleets be able to use warp\n" +
                "\n" +
                "Gain insight to how the mod handles ingame with this showcase video (very early version): https://www.youtube.com/watch?v=0t-y4ZppfLg\n" +
                "\n" +
                "StarmadeDock mod page: https://starmadedock.net/content/warpspace.8166/\n" +
                "\n" +
                "StarmadeDock blog page: https://starmadedock.net/threads/an-alternative-ftl-system-warpspace.31607/page-2#post-380687\n" +
                "\n" +
                "Find the documentation here: https://ir0nsight.github.io/Warpspace/\n" +
                "\n" +
                "Starloader community discord for bugreports/feedback/help: https://discord.gg/hcpSphM\n" +
                "Install guide\n" +
                "\n" +
                "    use the builtin, ingame modbrowser, find WarpSpace and click \"install\".\n" +
                "    make sure the mod is activated.\n" +
                "\n" +
                "If you have the suspicion that something is not working or the mod is not running at all, contact me or the starloader discord. Since Starloader is a community project and still in developement, its likely that it will break mods when it updates. I do my best to fix this fast, but it can take a couple days. Let me know if its broken, so i can update it.");
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
