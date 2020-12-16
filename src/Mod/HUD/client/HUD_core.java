package Mod.HUD.client;

import api.DebugFile;
import com.google.gson.Gson;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.12.2020
 * TIME: 19:23
 */
public class HUD_core {

    public static List<HUD_element> elementList = new ArrayList();

    public static HUD_element console = new HUD_element(SpriteList.CONSOLE.getSprite(),new Vector3f(0.8572f,0.8611f,0),new Vector3f(1,1,1));
    public static HUD_element warpIndicator = new HUD_element(SpriteList.RSP_ICON.getSprite(),new Vector3f(0.8464f,0.86012f,0),new Vector3f(0.112f,0.112f,1)); //1625,929
    //TODO maybe split up in placement + available sprites?
    //TODO move to json
    public static void initList() {
        elementList.add(console);
        elementList.add(warpIndicator);
        DebugFile.log("init list ran.");
    }

}
class HUD_element {
    public Sprite sprite;
    public Vector3f pos;
    public Vector3f scale;
    public HUD_element(Sprite sprite, Vector3f pos, Vector3f scale) {
        this.sprite = sprite;
        this.pos = pos;
        this.scale = scale;
    }
    //TODO debug tostring
}
