package Mod.HUD.client;

import Mod.WarpMain;
import Mod.WarpManager;
import api.common.GameClient;
import api.listener.events.gui.HudCreateEvent;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 26.10.2020
 * TIME: 20:55
 */

/**
 * creates a text field HUD element and draws it (?)
 */
public class WarpHUDPanel {
    TextElement textEl;
    public WarpHUDPanel(HudCreateEvent ev) {
        textEl = new TextElement(FontLibrary.getBlenderProMedium16(), ev.getInputState());
        textEl.text = "hello space!";
        ev.addElement(textEl);
        startLoop();
    }
    public void setPosition(int x, int y){
        textEl.getPos().set(new float[]{x-20, y+6,1});
    }
    public void setTextEl(String text) {
        textEl.text = text;
    }
    //TODO only calculate other sector if sector changed. dont calculate once per tick.
    private void startLoop() {
        setPosition(1700,300);
        new StarRunnable() {
            @Override
            public void run() {
                if (GameServerState.isFlagShutdown()) {
                    cancel();
                }
                String text = "empty";
                if (WarpManager.IsInWarp(getPlayerSector())) {
                    text = "RSP " + WarpManager.GetRealSpacePos(getPlayerSector());
                } else {
                    text = "WARP " + WarpManager.GetWarpSpacePos(getPlayerSector());
                }
                setTextEl(text);

            }
        }.runTimer(WarpMain.instance,1);
    }
    private static Vector3i getPlayerSector() {
        return GameClient.getClientPlayerState().getCurrentSector();
    }
}
