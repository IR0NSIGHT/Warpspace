package Mod.HUD.client;

import Mod.WarpJumpEvent;
import Mod.WarpMain;
import Mod.WarpManager;
import api.DebugFile;
import api.utils.StarRunnable;
import com.google.gson.Gson;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.network.client.ClientState;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.12.2020
 * TIME: 19:23
 */
public class HUD_core {

    public static List<HUD_element> elementList = new ArrayList();
    public static HashMap<SpriteList, Integer> drawList = new HashMap<>();

    public enum WarpSituation {
        WARPSECTORBLOCKED(0),
        RSPSECTORBLOCK(1),
        JUMPDROP(2),
        JUMPEXIT(3),
        JUMPENTRY(4),
        JUMPPULL(5),
        TRAVEL(6);

        private final int value;
        private static Map map = new HashMap<>();
        private WarpSituation(int value) {
            this.value = value;
        }

        static { //map enum value to int keys for reconstruction int -> enumvalue
            for (WarpSituation s: WarpSituation.values()) {
                map.put(s.getValue(),s);
            }
        }

        public static WarpSituation valueOf(int k) {
            return (WarpSituation) map.get(k);
        }

        public int getValue() {
            return value;
        }

    };

    public static HUD_element console = new HUD_element(new Vector3f(0.8572f,0.8611f,0),new Vector3f((float)1/1080,(float)1/1080,(float)1/1080),SpriteList.CONSOLE);
    public static HUD_element spaceIndicator = new HUD_element(new Vector3f((float)1622/1920,(float)928/1080,0),new Vector3f((float)(0.42/1080),(float)(0.42/1080),(float)1/1080),SpriteList.RSP_ICON); //1625,929 - 512x512

    //TODO maybe split up in placement + available sprites?
    //TODO move to json
    public static void initList() {
        //elementList.add(console);
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.WARP_ICON));
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.RSP_ICON));
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_RSP_TRAVEL)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_WARP_TRAVEL)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_RSP_INACTIVE)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_WARP_INACTIVE)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_RSP_BLOCKED)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_WARP_BLOCKED)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_SECTOR_LOCKED)); //1625,929)

        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_TO_RSP)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_TO_WARP)); //1625,929)

        DebugFile.log("init list ran.");
        for (HUD_element e : elementList) {
            drawList.put(e.enumValue,0);
        }
    }

    /**
     * what sprite to draw, accessed by CustomHUDImage on each draw() method.
     */
    public static void HUD_processPacket(WarpSituation s) {
        //TODO add behaviour for each enum value
        //TODO add method to get more precise data like time till warpdrop/jump etc.
        //priority: jump>drop>travel
        //travel kann nur
        playerWarpState = s;
        DebugFile.log("set player warpsituation to " + s.toString());
    }

    public static WarpSituation playerWarpState = WarpSituation.TRAVEL;
    public static void HUDLoop() {
        new StarRunnable() {
            PlayerState player = GameClientState.instance.getPlayer();

            int i = 0;

            @Override
            public void run() {
                if (player == null || player.getCurrentSector() == null) {
                    DebugFile.log("playerstate is null or playersector is null");
                    player = GameClientState.instance.getPlayer();
                } else {
                    if (GameServerState.isShutdown()) {
                        cancel();
                    }
                    if (i == 0) {
                        player = GameClientState.instance.getPlayer();
                    }
                    if (WarpManager.IsInWarp(player.getCurrentSector())) {
                        //player is in warp
                        drawList.put(SpriteList.RSP_ICON,0);
                        drawList.put(SpriteList.WARP_ICON,1);
                    } else {
                        drawList.put(SpriteList.RSP_ICON,1);
                        drawList.put(SpriteList.WARP_ICON,0);
                    }

                    if (playerWarpState.equals(WarpSituation.JUMPDROP) || playerWarpState.equals(WarpSituation.JUMPEXIT)) {
                        //do blinking drop icon
                        if (i % 5 == 0) { //once a second
                            if ((i/5) % 2 == 0) //gerade zahlen
                            {
                                drawList.put(SpriteList.ICON_OUTLINE_TO_RSP,1);
                                drawList.put(SpriteList.ICON_OUTLINE_RSP_INACTIVE,0);
                            } else {    //ungerade
                                drawList.put(SpriteList.ICON_OUTLINE_TO_RSP,0);
                                drawList.put(SpriteList.ICON_OUTLINE_RSP_INACTIVE,1);
                            }
                            drawList.put(SpriteList.ICON_OUTLINE_WARP_TRAVEL,1);
                        }
                    } else {
                        drawList.put(SpriteList.ICON_OUTLINE_TO_RSP,0);
                    }

                    if (playerWarpState.equals(WarpSituation.JUMPENTRY) ) {
                        //do blinking jump icon
                        if (i % 5 == 0) { //once a second
                            if (i/5 % 2 == 0) //gerade zahlen
                            {
                                drawList.put(SpriteList.ICON_OUTLINE_TO_WARP,1);
                                drawList.put(SpriteList.ICON_OUTLINE_WARP_INACTIVE,0);
                            } else {    //ungerade
                                drawList.put(SpriteList.ICON_OUTLINE_TO_WARP,0);
                                drawList.put(SpriteList.ICON_OUTLINE_WARP_INACTIVE,1);
                            }
                        }
                        drawList.put(SpriteList.ICON_OUTLINE_RSP_TRAVEL,1);
                    } else {
                        drawList.put(SpriteList.ICON_OUTLINE_TO_WARP,0);
                    }

                    if (playerWarpState.equals(WarpSituation.TRAVEL)) {
                        if (WarpManager.IsInWarp(player.getCurrentSector())) {
                            drawList.put(SpriteList.ICON_OUTLINE_WARP_TRAVEL,1);
                            drawList.put(SpriteList.ICON_OUTLINE_RSP_INACTIVE,1);

                            drawList.put(SpriteList.ICON_OUTLINE_RSP_TRAVEL,0);
                            drawList.put(SpriteList.ICON_OUTLINE_WARP_INACTIVE,0);
                        } else {
                            drawList.put(SpriteList.ICON_OUTLINE_WARP_TRAVEL,0);
                            drawList.put(SpriteList.ICON_OUTLINE_RSP_INACTIVE,0);

                            drawList.put(SpriteList.ICON_OUTLINE_RSP_TRAVEL,1);
                            drawList.put(SpriteList.ICON_OUTLINE_WARP_INACTIVE,1);
                        }
                    } else {
                    /*    drawList.put(SpriteList.ICON_OUTLINE_WARP_TRAVEL,0);
                        drawList.put(SpriteList.ICON_OUTLINE_RSP_INACTIVE,0);

                        drawList.put(SpriteList.ICON_OUTLINE_RSP_TRAVEL,0);
                        drawList.put(SpriteList.ICON_OUTLINE_WARP_INACTIVE,0); */
                    }


                }

                i ++;
                if (i > 1000) {
                    i = 0;
                }
            }
        }.runTimer(WarpMain.instance,1);
    }
}
class HUD_element {
    public Vector3f pos;
    public Vector3f scale;
    public SpriteList enumValue;
    public HUD_element(Vector3f pos, Vector3f scale, SpriteList enumValue) {
        this.enumValue = enumValue;
        this.pos = pos;
        this.scale = scale;
    }
    //TODO debug tostring
}
