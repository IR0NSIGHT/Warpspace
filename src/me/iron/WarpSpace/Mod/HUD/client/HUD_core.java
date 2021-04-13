package me.iron.WarpSpace.Mod.HUD.client;

import api.DebugFile;
import api.common.GameClient;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
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

    public static List<HUD_element> elementList = new ArrayList(); //TODO get rid of compiler warning for raw usage
    public static HashMap<SpriteList, Integer> drawList = new HashMap<>();

    /**
     * some general HUD element placements to use a position references. any element built with these will move + scale with them
     */
    public static HUD_element console = new HUD_element(
            new Vector3f((float)1435/1920,(float)975/1080,0.01f),
            new Vector3f((float)0.25/1080,(float)0.25/1080,(float)1/1080),
            new Vector3f( 1,1,1),
            SpriteList.CONSOLE_HUD1024,
            HUD_element.ElementType.BACKGROUND);
    public static HUD_element spaceIndicator = new HUD_element(
            new Vector3f((float)1622/1920,(float)928/1080,0.01f),
            new Vector3f((float)(0.32/1080),(float)(0.32/1080),(float)1/1080),
            new Vector3f(1,1,1),
            SpriteList.RSP_ICON,
            HUD_element.ElementType.INDICATOR); //1625,929 - 512x512
    public static HUD_element interdictionBox = new HUD_element(new Vector3f ((float) 1700/1920,(float) 928/1080,0), new Vector3f((float)0.314/1080,(float)0.314/1080,(float)1/1080), new Vector3f(1,1,1),SpriteList.CONSOLE_HUD1024,HUD_element.ElementType.INFO_RIGHT);

    //TODO maybe split up in placement + available sprites?
    //TODO move to json
    //TODO get rid of elementlist, directly put into drawlist.
    /**
     * initialize the list of hud elements, add all entries into the drawList.
     */
    public static void initList() {
        elementList.add(new HUD_element(console,SpriteList.CONSOLE_HUD1024, HUD_element.ElementType.BACKGROUND));
        elementList.add(new HUD_element(console,SpriteList.CONSOLE_HUD1024_SCREEN, HUD_element.ElementType.BACKGROUND));
        elementList.add(new HUD_element(console,SpriteList.CONSOLE_HUD1024_BOTTOM, HUD_element.ElementType.BACKGROUND));

        elementList.add(new HUD_element(console, SpriteList.WARP_ICON, HUD_element.ElementType.INDICATOR));
        elementList.add(new HUD_element(console, SpriteList.RSP_ICON, HUD_element.ElementType.INDICATOR));
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_RSP_TRAVEL,HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_WARP_TRAVEL,HUD_element.ElementType.UPPER_Bar)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_RSP_INACTIVE,HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_WARP_INACTIVE,HUD_element.ElementType.UPPER_Bar)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_RSP_BLOCKED, HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_WARP_BLOCKED, HUD_element.ElementType.UPPER_Bar)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_SECTOR_LOCKED_DOWN, HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_SECTOR_LOCKED_UP, HUD_element.ElementType.UPPER_Bar)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_TO_RSP, HUD_element.ElementType.LOWER_BAR)); //1625,929)
        elementList.add(new HUD_element(console, SpriteList.ICON_OUTLINE_TO_WARP, HUD_element.ElementType.UPPER_Bar)); //1625,929)

        elementList.add(interdictionBox);
        for (HUD_element e : elementList) {
            drawList.put(e.enumValue,0);
        }
    }

    /**
     * is called by me.iron.WarpSpace.Mod.network.PacketHUDUpdate, used to transfer information from the server to the client about what a player is currently doing related to warp.
     * Sets the received info to WarpProcessController to the ProcessMap, allows additional info in processarray. dependent on what process is updated.
     * @param value x
     * @param process x
     * @param processArray x
     */
    public static void HUD_processPacket(WarpProcessController.WarpProcess process, Integer value, List<String> processArray) {
        //TODO add method to get more precise data like time till warpdrop/jump etc.
        playerWarpState = process;
        WarpProcessController.WarpProcessMap.put(process, value);
        UpdateSituation();
    }

    public static WarpProcessController.WarpProcess playerWarpState = WarpProcessController.WarpProcess.TRAVEL;
    public static void HUDLoop() {
        new StarRunnable() {
            PlayerState player = GameClientState.instance.getPlayer();

            int tenthSeconds = 0;
            long lastTime = System.currentTimeMillis();
            @Override
            public void run() {
                /**
                 * this method checks for static variables like "is in warp" and decides what elements to draw on the HUD and which to disable.
                 */
            //    UpdateSituation(); //TODO make 100% event based? -> new package from server triggers GUI update
                if (player == null || player.getCurrentSector() == null) { //nullpointer check to avoid drawing before player spawns.
                   // DebugFile.log("playerstate is null or playersector is null");
                    player = GameClientState.instance.getPlayer();
                } else {
                    if (GameServerState.isShutdown()) {
                        cancel();
                    }
                    SimpleTransformableSendableObject playerShip = player.getFirstControlledTransformableWOExc();

                    //turn of HUD if player is not controlling a ship
                    if (null == playerShip || !playerShip.isSegmentController() || GameClientState.instance.isInAnyBuildMode()) {
                        for (HUD_element.ElementType type: HUD_element.ElementType.values()) {
                            HUDElementController.drawType(type,0);
                        }
                        return;
                    }


                    //draw decision making method

                    //not server situation dependent, 100% passive
                    HUDElementController.drawType(HUD_element.ElementType.BACKGROUND,1);
                    boolean isInWarp = WarpManager.IsInWarp(player.getCurrentSector());
                    if (isInWarp) {
                        HUDElementController.drawElement(SpriteList.WARP_ICON,true);
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_WARP_TRAVEL,true);
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_RSP_INACTIVE,true);
                    } else {
                        isDropping = false;
                        HUDElementController.drawElement(SpriteList.RSP_ICON,true);
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_RSP_TRAVEL,true);
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_WARP_INACTIVE,true);
                    }

                    //TODO make prettier check for processes
                    if ((isDropping || isExit) && ((tenthSeconds % 8) <= 4)) {
                        //do blinking drop icon
                        HUDElementController.drawElement(SpriteList.ICON_OUTLINE_TO_RSP,true);
                    }

                    if (isEntry && ((tenthSeconds % 8) <= 4)) {
                      HUDElementController.drawElement(SpriteList.ICON_OUTLINE_TO_WARP,true);
                    }

                    if (isWarpSectorBlocked) {
                        if (isInWarp) { //sector locked down
                            HUDElementController.drawElement(SpriteList.ICON_OUTLINE_SECTOR_LOCKED_DOWN,true);
                            HUDElementController.drawElement(SpriteList.ICON_OUTLINE_SECTOR_LOCKED_UP,true);
                        } else { //no jump upwards
                            HUDElementController.drawElement(SpriteList.ICON_OUTLINE_WARP_BLOCKED,true);
                        }
                    }

                    if (isRSPSectorBlocked) {
                        if (isInWarp) {
                            HUDElementController.drawElement(SpriteList.ICON_OUTLINE_RSP_BLOCKED,true);
                        } else {
                            HUDElementController.drawElement(SpriteList.ICON_OUTLINE_SECTOR_LOCKED_DOWN,true);
                            HUDElementController.drawElement(SpriteList.ICON_OUTLINE_SECTOR_LOCKED_UP,true);
                        }
                    }

                //    //move HUD elements.
                //    if (isRSPSectorBlocked || isWarpSectorBlocked) {
                //        console.setPos(onInhibition);
                //        //TODO name of interdicting ship/general info
                //        interdictionBox.getTextElement().text= "interdicted by: EvilEnemyShipThatsVeryEvIL /r next line?";
                //        HUDElementController.drawElement(SpriteList.INFO_RIGHT,true);
                //    } else {
                //        console.setPos(noInhibition);
                //        interdictionBox.getTextElement().text = "";
                        HUDElementController.clearType(HUD_element.ElementType.INFO_RIGHT);
                //    }
                }

                //precise timer handling (not super precise but better than serverticks)
                if (System.currentTimeMillis() - lastTime > 100) {
                    //0.1 second passed
                    tenthSeconds++;
                    lastTime = System.currentTimeMillis();
                }
                if (tenthSeconds > 1000) {
                    tenthSeconds = 0;
                }
            }
        }.runTimer(WarpMain.instance,1);
    }

    private static boolean isDropping = false;
    private static boolean isEntry;
    private static boolean isExit;
    private static boolean isRSPSectorBlocked;
    private static boolean isWarpSectorBlocked;
    private static Vector3f noInhibition = new Vector3f((float)1622/1920,(float)928/1080,0.01f);
    private static Vector3f onInhibition = new Vector3f((float) 1460/1920,(float) 928/1080,0f);
    /**
     * update player situation fields from WarpProcessMap
     */
    private static void UpdateSituation() {
        //DebugFile.log("updating warp situation from WarpProcessMap: ");

        isDropping = ( WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPDROP) == 1);

        isExit = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPEXIT) == 1);

        isEntry = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPENTRY) == 1);

        isRSPSectorBlocked = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.RSPSECTORBLOCKED) == 1);

        isWarpSectorBlocked = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.WARPSECTORBLOCKED) == 1);
    }


}
