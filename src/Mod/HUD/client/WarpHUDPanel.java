package Mod.HUD.client;

import Mod.WarpMain;
import Mod.WarpManager;
import api.ModPlayground;
import api.common.GameClient;
import api.listener.events.gui.HudCreateEvent;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;

import javax.vecmath.Vector3f;
import java.util.Vector;

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
        DebugChatEvent.textElement = textEl;
        ev.addElement(textEl);
        setPosition(1565,1038); //needs to run in loop to autoadjust for changed textsize
        startLoop();
    }
    public void setPosition(int x, int y){
        textEl.getPos().set(new float[]{x,y,0});
    }
    public void setTextEl(String text) {
        textEl.text = text;
    }
    //TODO only calculate other sector if sector changed. dont calculate once per tick.
    private void startLoop() {

        new StarRunnable() {
            Vector3i oldPos = new Vector3i(getPlayerSector());
            Vector3i newPos;
            String text = "empty";
            Integer i = 0;
            @Override
            public void run() {
                if (GameServerState.isFlagShutdown()) {
                    cancel();
                }
                if (i == 0 || !(oldPos.equals(getPlayerSector()))) {
                    oldPos = new Vector3i(getPlayerSector());

                    if (WarpManager.IsInWarp(getPlayerSector())) {
                        newPos = WarpManager.GetRealSpacePos(getPlayerSector());
                    } else {
                        newPos = WarpManager.GetWarpSpacePos(getPlayerSector());
                    }
                    Vector<Integer> vec = new Vector<Integer>();
                    vec.add(newPos.x);
                    vec.add(newPos.y);
                    vec.add(newPos.z);
                    int xi = 0;
                    text = "";

                    for (Integer i: vec) {
                        //get length of int
                        int length = (int) (Math.log10(Math.abs(i)) + 1);
                        //turn to string
                        if (i < 0) {
                            length ++;
                        }
                        String s = "";
                        for (int i1 = 0; i1 < (5 - length); i1 ++) {
                            s += "x"; //add spaces for continous size
                        }
                        s += i.toString();
                        if (xi < 2) {
                            s += "|";
                        }
                        xi ++;
                        text += s;
                    }
                    i++;
                }
                //
                setTextEl(text);
            }
        }.runTimer(WarpMain.instance,5);
    }
    private static Vector3i getPlayerSector() {
        return GameClient.getClientPlayerState().getCurrentSector();
    }
}
