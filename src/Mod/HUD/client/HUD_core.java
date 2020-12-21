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
    public static HUD_element console = new HUD_element(new Vector3f((float)1622/1920,(float)928/1080,0.01f),new Vector3f((float)0.314/1080,(float)0.314/1080,(float)1/1080),SpriteList.CONSOLE_HUD1024, HUD_element.ElementType.BACKGROUND);
    public static HUD_element spaceIndicator = new HUD_element(new Vector3f((float)1622/1920,(float)928/1080,0.01f),new Vector3f((float)(0.32/1080),(float)(0.32/1080),(float)1/1080),SpriteList.RSP_ICON, HUD_element.ElementType.INDICATOR); //1625,929 - 512x512

    //TODO maybe split up in placement + available sprites?
    //TODO move to json
    //TODO get rid of elementlist, directly put into drawlist.
    /**
     * initialize the list of hud elements, add all entries into the drawList.
     */
    public static void initList() {
        elementList.add(new HUD_element(console.pos, console.scale,SpriteList.CONSOLE_HUD1024, HUD_element.ElementType.BACKGROUND));
        elementList.add(new HUD_element(console.pos,console.scale,SpriteList.CONSOLE_HUD1024_SCREEN, HUD_element.ElementType.BACKGROUND));
        elementList.add(new HUD_element(console.pos, console.scale,SpriteList.CONSOLE_HUD1024_BOTTOM, HUD_element.ElementType.BACKGROUND));

        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.WARP_ICON, HUD_element.ElementType.INDICATOR));
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.RSP_ICON, HUD_element.ElementType.INDICATOR));
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_RSP_TRAVEL,HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_WARP_TRAVEL,HUD_element.ElementType.UPPER_Bar)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_RSP_INACTIVE,HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_WARP_INACTIVE,HUD_element.ElementType.UPPER_Bar)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_RSP_BLOCKED, HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_WARP_BLOCKED, HUD_element.ElementType.UPPER_Bar)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_SECTOR_LOCKED_DOWN, HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_SECTOR_LOCKED_UP, HUD_element.ElementType.UPPER_Bar)); //1625,929)

        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_TO_RSP, HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(spaceIndicator.pos,spaceIndicator.scale,SpriteList.ICON_OUTLINE_TO_WARP, HUD_element.ElementType.UPPER_Bar)); //1625,929)

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
            long lastTime = System.currentTimeMillis();
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
                    HUDElementController.drawType(HUD_element.ElementType.BACKGROUND,1);

                    if (WarpManager.IsInWarp(player.getCurrentSector())) {
                        HUDElementController.drawElement(SpriteList.WARP_ICON,true);
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_WARP_TRAVEL,true);
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_RSP_INACTIVE,true);
                    } else {
                        HUDElementController.drawElement(SpriteList.RSP_ICON,true);
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_RSP_TRAVEL,true);
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_WARP_INACTIVE,true);
                    }

                    //TODO make prettier check for processes
                    if (isDropping || isExit) {
                        //do blinking drop icon
                        if ((i % 24) <= 12) { //once a second
                            HUDElementController.drawElement(SpriteList.ICON_OUTLINE_TO_RSP,true);
                        }
                    }

                    if (isEntry) {
                        //do blinking jump icon
                        if ((i % 24) <= 12) { //once a second
                            HUDElementController.drawElement(SpriteList.ICON_OUTLINE_TO_WARP,true);
                        }
                    }
                }
                if (i % 25 == 0) {

                    DebugFile.log("starrunnable with frequency = 1 took " + (System.currentTimeMillis() - lastTime) + " millis for 25 iteration");
                    lastTime = System.currentTimeMillis();
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
        //DebugFile.log("updating warp situation from WarpProcessMap: ");

        isDropping = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPDROP) == 1);
        //DebugFile.log("is Dropping: " + isDropping);
        isExit = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPEXIT) == 1);

        //DebugFile.log("is exiting" + isExit);
        isEntry = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPENTRY) == 1);

        //DebugFile.log("is entering" + isEntry);
    }
}
class HUD_element {
    public Vector3f pos;
    public Vector3f scale;
    public SpriteList enumValue;
    public boolean playShutter = false;
    public ElementType type;
    public enum ElementType {
        LOWER_BAR,
        UPPER_Bar,
        INDICATOR,
        BACKGROUND
    }
    public HUD_element(Vector3f pos, Vector3f scale, SpriteList enumValue, ElementType type) {
        this.enumValue = enumValue;
        this.type = type;
        this.pos = pos;
        this.scale = scale;
    }
    public HUD_element(Vector3f pos, Vector3f scale, SpriteList enumValue, ElementType type, boolean playShutter) {

        this.enumValue = enumValue;
        this.pos = pos;
        this.scale = scale;
        this.playShutter = playShutter;
    }
    //TODO debug tostring
}
