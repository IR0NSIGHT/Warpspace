package me.iron.WarpSpace.Mod.HUD.client.glossar;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.listener.events.player.PlayerSpawnEvent;
import api.mod.ServerModInfo;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import api.utils.game.PlayerUtils;
import api.utils.gui.GUIControlManager;
import api.utils.gui.GUIMenuPanel;
import api.utils.gui.ModGUIHandler;
import me.iron.WarpSpace.Mod.WarpMain;
import org.newdawn.slick.UnicodeFont;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.network.server.ServerMessage;

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
        addAdvertisement();
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

                GlossarPageList list = new GlossarPageList(w,h,modGlossar.getContent(0),getState());
                modGlossar.setContent(0,list);
            }
        };

        panel.onInit();
        panel.recreateTabs();
        return panel;
    }

    private void addKeyListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if (event.getText().contains("!warp")) {
                    for (GUIControlManager manager: ModGUIHandler.getAllModControlManagers()) {
                        manager.setActive(false);
                    }
                    setActive(true);
                    if (panel != null)
                        panel.recreateTabs();
                    event.setCanceled(true);
                }
            }
        }, WarpMain.instance);
    }
    private void addAdvertisement() {
        final String ad = "type !warp to open the glossar.";
            new StarRunnable(){
                long last = System.currentTimeMillis()-1000*60*30 + 10*1000;
                @Override
                public void run() {
                    if (last + 1000*60*30<System.currentTimeMillis()) {
                        last = System.currentTimeMillis();
                        sendClientMssg(ad);
                    }
                }
            }.runTimer(WarpMain.instance,10);
    }
    private void sendClientMssg(String mssg) {
        GameClientState.instance.getServerMessages().add(new ServerMessage(Lng.astr(mssg),ServerMessage.MESSAGE_TYPE_SIMPLE));

    }
}
