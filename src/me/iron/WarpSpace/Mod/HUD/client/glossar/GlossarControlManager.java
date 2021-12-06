package me.iron.WarpSpace.Mod.HUD.client.glossar;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.gui.GUIControlManager;
import api.utils.gui.GUIMenuPanel;
import api.utils.gui.ModGUIHandler;
import me.iron.WarpSpace.Mod.WarpMain;
import org.newdawn.slick.UnicodeFont;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;

import javax.vecmath.Vector4f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.12.2021
 * TIME: 20:56
 */
public class GlossarControlManager extends GUIControlManager {
    private String currentMod;
    private GUIMenuPanel panel;
    public static UnicodeFont titleFont = FontLibrary.getBlenderProHeavy30();
    public static UnicodeFont textFont = FontLibrary.getBlenderProHeavy20();

    public GlossarControlManager(GameClientState gameClientState) {
        super(gameClientState);
        addKeyListener();
    }

    @Override
    public GUIMenuPanel createMenuPanel() {
        panel = new GUIMenuPanel(getState(),"glossarPanel", (int)(GLFrame.getWidth()*0.75f),(int)(GLFrame.getHeight()*0.75f)) {
            @Override
            public void recreateTabs() {
                guiWindow.getTabs().clear();
                guiWindow.setResizable(false);
                int w = guiWindow.getInnerWidth();
                int h = guiWindow.getInnerHeigth();

              //glossar for actual pages
              GUIContentPane modGlossar = guiWindow.addTab(new Object(){
                  @Override
                  public String toString() {
                      return currentMod==null?"glossar":currentMod;
                  }
              });
                ModPlayground.broadcastMessage("guiWindow has: w" + w + " ,h" + h);
                ModPlayground.broadcastMessage("guiWindowTAB has: bottom" + guiWindow.getInnerCornerBottomDistY() + " ,top" + guiWindow.getInnerCornerTopDistY());

                GlossarPageList list = new GlossarPageList(w,h,modGlossar.getContent(0),getState());
                modGlossar.setContent(0,list);
            }
        };
        if (panel == null) {
            new NullPointerException("no panel? :(");
        }
        panel.onInit();
        panel.recreateTabs();
        return panel;
    }

    private void addKeyListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if (event.getText().contains("gls")) {
                    for (GUIControlManager manager: ModGUIHandler.getAllModControlManagers()) {
                        manager.setActive(false);
                    }
                    setActive(true);
                    if (panel != null)
                        panel.recreateTabs();
                }
            }
        }, WarpMain.instance);
    }
}
