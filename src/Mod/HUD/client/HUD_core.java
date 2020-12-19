package Mod.HUD.client;

import Mod.WarpMain;
import Mod.WarpManager;
import api.DebugFile;
import api.utils.StarRunnable;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

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
    public static HashMap<SpriteList, Integer> drawList = new HashMap<>();

    /**
     * some general HUD element placements to use a position references
     */
    public static HUD_element console = new HUD_element(new Vector3f(0.8572f,0.8611f,0),new Vector3f((float)1/1080,(float)1/1080,(float)1/1080),SpriteList.CONSOLE);
    public static HUD_element spaceIndicator = new HUD_element(new Vector3f((float)1622/1920,(float)928/1080,0),new Vector3f((float)(0.42/1080),(float)(0.42/1080),(float)1/1080),SpriteList.RSP_ICON); //1625,929 - 512x512

    //TODO maybe split up in placement + available sprites?
    //TODO move to json
    //TODO get rid of elementlist, directly put into drawlist.
    /**
     * initialize the list of hud elements, add all entries into the drawList.
     */
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
     * is called by PacketHUDUpdate, used to transfer information from the server to the client about what a player is currently doing related to warp.
     * Sets the received info to WarpProcessController to the ProcessMap
     */
    public static void HUD_processPacket(WarpProcessController.WarpProcess s, Integer key) {
        //TODO add behaviour for each enum value
        //TODO add method to get more precise data like time till warpdrop/jump etc.
        //priority: jump>drop>travel
        //travel kann nur
        playerWarpState = s;
        WarpProcessController.WarpProcessMap.put(s, key);
        DebugFile.log("set player warpsituation to " + s.toString() + "value: " + key.toString());
    }

    public static WarpProcessController.WarpProcess playerWarpState = WarpProcessController.WarpProcess.TRAVEL;
    public static void HUDLoop() {
        new StarRunnable() {
            PlayerState player = GameClientState.instance.getPlayer();

            int i = 0;

            @Override
            public void run() {
                UpdateSituation();
                if (player == null || player.getCurrentSector() == null) { //nullpointer check to avoid drawing before player spawns.
                    DebugFile.log("playerstate is null or playersector is null");
                    player = GameClientState.instance.getPlayer();
                } else {
                    if (GameServerState.isShutdown()) {
                        cancel();
                    }

                    //draw decision making method

                    //not server situation dependent, 100% passive
                    if (WarpManager.IsInWarp(player.getCurrentSector())) {
                        //player is in warp
                        drawList.put(SpriteList.RSP_ICON,0);
                        drawList.put(SpriteList.WARP_ICON,1);
                    } else {
                        drawList.put(SpriteList.RSP_ICON,1);
                        drawList.put(SpriteList.WARP_ICON,0);
                    }

                    //TODO make prettier check for processes
                    if (isDropping || isExit) {
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

                    if (isEntry) {
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

                    if (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.TRAVEL) == 1) {
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

    private static boolean isDropping = false;
    private static boolean isEntry;
    private static boolean isExit;
    private static boolean isSectorLocked;

    /**
     * update player situation fields from WarpProcessMap
     */
    private static void UpdateSituation() {
        DebugFile.log("updating warp situation from WarpProcessMap: ");

        isDropping = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPDROP) == 1);
        DebugFile.log("is Dropping: " + isDropping);
        isExit = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPEXIT) == 1);

        DebugFile.log("is exiting" + isExit);
        isEntry = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPENTRY) == 1);

        DebugFile.log("is entering" + isEntry);
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
